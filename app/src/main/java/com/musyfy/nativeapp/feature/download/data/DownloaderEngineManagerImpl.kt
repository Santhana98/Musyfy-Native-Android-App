package com.musyfy.nativeapp.feature.download.data

import android.content.Context
import android.util.Log
import com.google.gson.JsonParser
import com.musyfy.nativeapp.core.analytics.AnalyticsConstants
import com.musyfy.nativeapp.core.analytics.AnalyticsEvent
import com.musyfy.nativeapp.core.analytics.AnalyticsManager
import com.musyfy.nativeapp.feature.download.domain.CompatibilityErrorClassifier
import com.musyfy.nativeapp.feature.download.domain.CompatibilityFailureType
import com.musyfy.nativeapp.feature.download.domain.DownloaderEngineManager
import com.musyfy.nativeapp.feature.download.domain.YtDlpVersionComparator
import com.musyfy.nativeapp.feature.download.domain.model.EngineFailureCategory
import com.musyfy.nativeapp.feature.download.domain.model.EngineUpdateState
import com.musyfy.nativeapp.feature.download.domain.model.UpdateCheckResult
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloaderEngineManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsManager: AnalyticsManager,
    private val songDownloaderProvider: javax.inject.Provider<com.musyfy.nativeapp.feature.download.domain.SongDownloader>
) : DownloaderEngineManager {

    companion object {
        private const val TAG = "DownloaderEngineManager"
        private const val PREFS_NAME = "musyfy_engine_update"
        private const val KEY_TX_STATE = "tx_state"
        private const val KEY_BACKUP_VERSION = "backup_version"
        private const val KEY_BACKUP_VERSION_NAME = "backup_version_name"
        private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"
        private const val KEY_LAST_CHECK_VERSION = "last_check_version"

        private const val TX_STATE_IDLE = "IDLE"
        private const val TX_STATE_BACKUP_CREATED = "BACKUP_CREATED"
        private const val TX_STATE_UPDATING = "UPDATING"
        private const val TX_STATE_VALIDATING = "VALIDATING"

        private const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest"
        private const val AUTO_CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val EXPECTED_DOWNLOAD_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB
        private const val SAFETY_MARGIN_BYTES = 10 * 1024 * 1024L // 10 MB safety buffer
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val updateMutex = Mutex()

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val ytdlpAndroidPrefs by lazy {
        context.getSharedPreferences("youtubedl-android", Context.MODE_PRIVATE)
    }

    private val runtimeDir: File
        get() = File(context.noBackupFilesDir, "youtubedl-android/yt-dlp")

    private val runtimeFile: File
        get() = File(runtimeDir, "yt-dlp")

    private val backupDir: File
        get() = File(context.noBackupFilesDir, "youtubedl-android-backup")

    private val backupFile: File
        get() = File(backupDir, "yt-dlp.backup")

    private val _engineState = MutableStateFlow<EngineUpdateState>(
        EngineUpdateState.Idle(getCurrentEngineVersion())
    )
    override val engineState: StateFlow<EngineUpdateState> = _engineState.asStateFlow()

    override fun getCurrentEngineVersion(): String {
        return try {
            val v = YoutubeDL.getInstance().version(context)
            if (!v.isNullOrBlank()) v else "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    override fun isUpdateInProgress(): Boolean {
        return when (_engineState.value) {
            is EngineUpdateState.Preparing,
            is EngineUpdateState.BackingUp,
            is EngineUpdateState.Updating,
            is EngineUpdateState.Validating,
            is EngineUpdateState.RollingBack -> true
            else -> false
        }
    }

    override suspend fun checkForUpdates(force: Boolean): UpdateCheckResult = withContext(Dispatchers.IO) {
        val currentVersion = getCurrentEngineVersion()

        // 1. Check cached check timestamp if not forced
        val now = System.currentTimeMillis()
        val lastCheckTime = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
        val lastKnownLatest = prefs.getString(KEY_LAST_CHECK_VERSION, null)

        if (!force && (now - lastCheckTime in 0..AUTO_CHECK_INTERVAL_MS) && !lastKnownLatest.isNullOrBlank()) {
            return@withContext if (YtDlpVersionComparator.isNewer(lastKnownLatest, currentVersion)) {
                _engineState.value = EngineUpdateState.UpdateAvailable(currentVersion, lastKnownLatest)
                UpdateCheckResult.Available(currentVersion, lastKnownLatest)
            } else {
                _engineState.value = EngineUpdateState.UpToDate(currentVersion)
                UpdateCheckResult.Current(currentVersion)
            }
        }

        _engineState.value = EngineUpdateState.Checking(currentVersion)
        analyticsManager.logEvent(
            AnalyticsEvent.engineUpdateChecked(
                currentVersion = currentVersion,
                updateSource = if (force) "manual" else "auto_check"
            )
        )

        try {
            val request = Request.Builder()
                .url(GITHUB_LATEST_RELEASE_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Musyfy-Native-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val bodyString = response.body?.string()

            if (code == 403 || (bodyString != null && bodyString.contains("rate limit", ignoreCase = true))) {
                Log.w(TAG, "checkForUpdates: GitHub API rate limited")
                _engineState.value = EngineUpdateState.Idle(currentVersion)
                return@withContext UpdateCheckResult.RateLimited("Update check temporarily rate limited. Try again later.")
            }

            if (!response.isSuccessful || bodyString.isNullOrBlank()) {
                Log.e(TAG, "checkForUpdates: HTTP request failed with code $code")
                _engineState.value = EngineUpdateState.Idle(currentVersion)
                return@withContext UpdateCheckResult.NetworkError("Unable to reach update service (Code $code)")
            }

            val jsonObject = JsonParser.parseString(bodyString).asJsonObject
            val tagName = jsonObject.get("tag_name")?.asString?.removePrefix("v")?.trim()
                ?: jsonObject.get("name")?.asString?.removePrefix("v")?.trim()

            if (tagName.isNullOrBlank()) {
                _engineState.value = EngineUpdateState.Idle(currentVersion)
                return@withContext UpdateCheckResult.Error("Malformed release metadata", EngineFailureCategory.UNKNOWN_ERROR)
            }

            // Persist check timestamp
            prefs.edit()
                .putLong(KEY_LAST_CHECK_TIME, now)
                .putString(KEY_LAST_CHECK_VERSION, tagName)
                .apply()

            if (YtDlpVersionComparator.isNewer(tagName, currentVersion)) {
                Log.i(TAG, "checkForUpdates: Newer stable version available: $tagName (current: $currentVersion)")
                _engineState.value = EngineUpdateState.UpdateAvailable(currentVersion, tagName)
                analyticsManager.logEvent(
                    AnalyticsEvent.engineUpdateAvailable(
                        currentVersion = currentVersion,
                        targetVersion = tagName
                    )
                )
                UpdateCheckResult.Available(currentVersion, tagName)
            } else {
                Log.i(TAG, "checkForUpdates: Up to date: $currentVersion")
                _engineState.value = EngineUpdateState.UpToDate(currentVersion)
                UpdateCheckResult.Current(currentVersion)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkForUpdates: Exception during check", e)
            _engineState.value = EngineUpdateState.Idle(currentVersion)
            if (e is java.net.UnknownHostException || e is java.net.SocketTimeoutException) {
                UpdateCheckResult.NetworkError("Network unavailable. Check your connection.")
            } else {
                UpdateCheckResult.Error(e.message ?: "Failed to check for updates", EngineFailureCategory.NETWORK_ERROR)
            }
        }
    }

    fun calculateRequiredStorageBytes(binarySizeBytes: Long): Long {
        val binarySize = if (binarySizeBytes > 0L) binarySizeBytes else 3 * 1024 * 1024L
        return (binarySize * 2) + EXPECTED_DOWNLOAD_SIZE_BYTES + SAFETY_MARGIN_BYTES
    }

    override suspend fun updateEngine(triggerSource: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentVersion = getCurrentEngineVersion()
        val startTime = System.currentTimeMillis()

        updateMutex.withLock {
            if (isUpdateInProgress()) {
                Log.w(TAG, "updateEngine: Another update is already in progress")
                return@withContext Result.failure(IllegalStateException("Update already in progress"))
            }

            // Reject update if user is currently downloading music to prevent corrupting active jobs
            val hasActiveDownloads = try {
                songDownloaderProvider.get().downloadStatuses.value.values.any {
                    it is com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus.Downloading
                }
            } catch (e: Exception) {
                false
            }
            if (hasActiveDownloads) {
                Log.w(TAG, "updateEngine: Active audio download in progress. Deferring update.")
                return@withContext Result.failure(IllegalStateException("Cannot update downloader while a song download is in progress."))
            }

            _engineState.value = EngineUpdateState.Preparing(currentVersion)
            analyticsManager.logEvent(
                AnalyticsEvent.engineUpdateStarted(
                    currentVersion = currentVersion,
                    updateSource = triggerSource
                )
            )

            // 1. Dynamic Storage Calculation
            val currentBinarySize = if (runtimeFile.exists()) runtimeFile.length() else 3 * 1024 * 1024L
            val requiredStorageBytes = calculateRequiredStorageBytes(currentBinarySize)
            val usableSpace = context.noBackupFilesDir.usableSpace

            if (usableSpace < requiredStorageBytes) {
                Log.e(TAG, "updateEngine: Insufficient storage. Available: $usableSpace, Required: $requiredStorageBytes")
                _engineState.value = EngineUpdateState.InsufficientStorage(usableSpace, requiredStorageBytes)
                analyticsManager.logEvent(
                    AnalyticsEvent.engineUpdateFailed(
                        currentVersion = currentVersion,
                        failureReason = "insufficient_storage"
                    )
                )
                return@withContext Result.failure(IllegalStateException("Insufficient storage to update YouTube downloader"))
            }

            // 2. Pre-Update Backup
            _engineState.value = EngineUpdateState.BackingUp(currentVersion)
            val backupSuccess = createRollbackBackup(currentVersion)
            if (!backupSuccess) {
                Log.e(TAG, "updateEngine: Failed to create atomic backup")
                _engineState.value = EngineUpdateState.Failed(
                    currentVersion = currentVersion,
                    failureReason = "Failed to create runtime backup",
                    category = EngineFailureCategory.BACKUP_FAILED,
                    rolledBack = false
                )
                analyticsManager.logEvent(
                    AnalyticsEvent.engineUpdateFailed(
                        currentVersion = currentVersion,
                        failureReason = "backup_failed"
                    )
                )
                return@withContext Result.failure(IllegalStateException("Failed to create pre-update backup"))
            }

            // 3. Official Updater Invocation
            _engineState.value = EngineUpdateState.Updating(currentVersion, targetVersion = "latest")
            prefs.edit().putString(KEY_TX_STATE, TX_STATE_UPDATING).apply()

            var updateResultStatus: YoutubeDL.UpdateStatus? = null
            try {
                updateResultStatus = YoutubeDL.getInstance().updateYoutubeDL(context, YoutubeDL.UpdateChannel._STABLE)
                Log.i(TAG, "updateEngine: updateYoutubeDL returned: $updateResultStatus")
            } catch (e: Exception) {
                Log.e(TAG, "updateEngine: updateYoutubeDL failed", e)
                analyticsManager.logEvent(
                    AnalyticsEvent.engineUpdateFailed(
                        currentVersion = currentVersion,
                        failureReason = e.message ?: "update_failed"
                    )
                )
                rollback("Native updater failed: ${e.message}")
                return@withContext Result.failure(e)
            }

            if (updateResultStatus == YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE) {
                cleanupBackup()
                prefs.edit().putString(KEY_TX_STATE, TX_STATE_IDLE).apply()
                _engineState.value = EngineUpdateState.UpToDate(currentVersion)
                return@withContext Result.success(Unit)
            }

            // 4. Validation Stage
            _engineState.value = EngineUpdateState.Validating("latest")
            prefs.edit().putString(KEY_TX_STATE, TX_STATE_VALIDATING).apply()

            val isValid = validateEngine()
            if (!isValid) {
                Log.e(TAG, "updateEngine: Engine validation failed. Initiating rollback...")
                analyticsManager.logEvent(
                    AnalyticsEvent.engineUpdateValidationFailed(
                        currentVersion = currentVersion
                    )
                )
                rollback("Engine validation failed")
                return@withContext Result.failure(IllegalStateException("Downloaded engine failed validation tests"))
            }

            // 5. Atomic Commit
            val newVersion = getCurrentEngineVersion()
            cleanupBackup()
            prefs.edit()
                .putString(KEY_TX_STATE, TX_STATE_IDLE)
                .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
                .putString(KEY_LAST_CHECK_VERSION, newVersion)
                .apply()

            val durationMs = System.currentTimeMillis() - startTime
            analyticsManager.logEvent(
                AnalyticsEvent.engineUpdateCompleted(
                    newVersion = newVersion,
                    durationMs = durationMs
                )
            )
            _engineState.value = EngineUpdateState.Success(newVersion)
            Log.i(TAG, "updateEngine: Successfully updated and validated engine v$newVersion in ${durationMs}ms")
            return@withContext Result.success(Unit)
        }
    }

    override suspend fun validateEngine(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. File existence and size check
            if (!runtimeFile.exists() || !runtimeFile.isFile || runtimeFile.length() < 100_000) {
                Log.e(TAG, "validateEngine: Runtime binary missing or unexpectedly small: ${runtimeFile.length()} bytes")
                return@withContext false
            }

            // 2. Version check
            val versionStr = getCurrentEngineVersion()
            if (versionStr == "unknown" || versionStr.isBlank()) {
                Log.e(TAG, "validateEngine: Version string could not be resolved from SharedPrefs")
                return@withContext false
            }

            // 3. Execution capability check (yt-dlp --version)
            val versionReq = YoutubeDLRequest(emptyList()).apply {
                addOption("--version")
            }
            val versionResponse = YoutubeDL.getInstance().execute(versionReq)
            if (versionResponse.exitCode != 0 || versionResponse.out.isNullOrBlank()) {
                Log.e(TAG, "validateEngine: Execution test failed with exitCode ${versionResponse.exitCode}")
                return@withContext false
            }
            val reportedVersion = versionResponse.out?.trim().orEmpty()
            Log.i(TAG, "validateEngine: Execution test passed. Binary reported version: $reportedVersion")

            // 4. Downgrade Guard: Ensure reported version is not an unexpected downgrade
            val savedBackupVersion = prefs.getString(KEY_BACKUP_VERSION, null)
            if (!savedBackupVersion.isNullOrBlank() &&
                YtDlpVersionComparator.isValidReleaseVersion(savedBackupVersion) &&
                YtDlpVersionComparator.isValidReleaseVersion(versionStr) &&
                YtDlpVersionComparator.compare(versionStr, savedBackupVersion) < 0
            ) {
                Log.e(TAG, "validateEngine: Version downgrade detected ($versionStr < $savedBackupVersion)")
                return@withContext false
            }

            // 5. Lightweight YouTube Metadata Health Check
            val healthCheckUrl = "https://www.youtube.com/watch?v=jNQXAC9IVRw"
            val healthReq = YoutubeDLRequest(healthCheckUrl).apply {
                addOption("--dump-single-json")
                addOption("--no-download")
                addOption("--extractor-args", "youtube:player_client=android")
            }
            val healthResp = try {
                YoutubeDL.getInstance().execute(healthReq)
            } catch (e: Exception) {
                val failureType = CompatibilityErrorClassifier.classify(e)
                if (failureType == CompatibilityFailureType.NETWORK_FAILURE) {
                    Log.w(TAG, "validateEngine: Network unreachable during health check. Skipping remote verification (HEALTH_CHECK_NETWORK_FAILED).")
                    return@withContext true
                }
                Log.e(TAG, "validateEngine: Exception during remote metadata health check", e)
                return@withContext false
            }

            if (healthResp.exitCode != 0 || healthResp.out.isNullOrBlank()) {
                val combinedErr = "${healthResp.err} ${healthResp.out}"
                val failureType = CompatibilityErrorClassifier.classify(combinedErr)
                if (failureType == CompatibilityFailureType.NETWORK_FAILURE) {
                    Log.w(TAG, "validateEngine: Network unreachable in health check output. Skipping remote verification (HEALTH_CHECK_NETWORK_FAILED).")
                    return@withContext true
                }
                Log.e(TAG, "validateEngine: Remote metadata health check failed with exitCode ${healthResp.exitCode}: $combinedErr")
                return@withContext false
            }

            Log.i(TAG, "validateEngine: All validation criteria passed successfully!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "validateEngine: Unexpected exception during validation", e)
            false
        }
    }

    override suspend fun rollback(reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentVersion = getCurrentEngineVersion()
        _engineState.value = EngineUpdateState.RollingBack(currentVersion)

        try {
            if (!backupFile.exists() || backupFile.length() < 100_000) {
                Log.e(TAG, "rollback: Backup file missing or corrupt. Cannot restore backup.")
                _engineState.value = EngineUpdateState.Failed(
                    currentVersion = currentVersion,
                    failureReason = "Backup file unavailable",
                    category = EngineFailureCategory.ROLLBACK_FAILED,
                    rolledBack = false
                )
                return@withContext Result.failure(IllegalStateException("Backup file missing"))
            }

            // 1. Remove broken runtime file
            if (runtimeFile.exists()) {
                runtimeFile.delete()
            }

            // 2. Restore backup file
            runtimeDir.mkdirs()
            copyFile(backupFile, runtimeFile)

            // 3. Restore version metadata in SharedPreferences
            val savedBackupVersion = prefs.getString(KEY_BACKUP_VERSION, null)
            val savedBackupVersionName = prefs.getString(KEY_BACKUP_VERSION_NAME, null)
            if (!savedBackupVersion.isNullOrBlank()) {
                ytdlpAndroidPrefs.edit()
                    .putString("dlpVersion", savedBackupVersion)
                    .putString("dlpVersionName", savedBackupVersionName ?: "yt-dlp $savedBackupVersion")
                    .apply()
            }

            // 4. Clean backup directory
            cleanupBackup()
            prefs.edit().putString(KEY_TX_STATE, TX_STATE_IDLE).apply()

            val restoredVersion = getCurrentEngineVersion()
            Log.i(TAG, "rollback: Successfully rolled back to baseline runtime v$restoredVersion")
            analyticsManager.logEvent(
                AnalyticsEvent.engineUpdateRollback(
                    currentVersion = restoredVersion,
                    rollbackReason = reason
                )
            )
            _engineState.value = EngineUpdateState.Restored(restoredVersion)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "rollback: Failed to restore backup", e)
            _engineState.value = EngineUpdateState.Failed(
                currentVersion = currentVersion,
                failureReason = e.message ?: "Rollback failed",
                category = EngineFailureCategory.ROLLBACK_FAILED,
                rolledBack = false
            )
            Result.failure(e)
        }
    }

    override suspend fun recoverOnStartup(): Unit = withContext(Dispatchers.IO) {
        val txState = prefs.getString(KEY_TX_STATE, TX_STATE_IDLE) ?: TX_STATE_IDLE
        if (txState == TX_STATE_IDLE) {
            cleanupBackup()
            return@withContext
        }

        Log.w(TAG, "recoverOnStartup: Detected interrupted transaction with state: $txState")
        _engineState.value = EngineUpdateState.Recovering

        try {
            // Check if active binary can run
            val isValid = validateEngine()
            if (isValid) {
                Log.i(TAG, "recoverOnStartup: Active engine is valid. Committing update...")
                cleanupBackup()
                prefs.edit().putString(KEY_TX_STATE, TX_STATE_IDLE).apply()
                _engineState.value = EngineUpdateState.Idle(getCurrentEngineVersion())
            } else {
                Log.w(TAG, "recoverOnStartup: Active engine is invalid. Restoring backup...")
                if (backupFile.exists() && backupFile.length() > 100_000) {
                    rollback("Startup recovery of interrupted update")
                    val isRestoredValid = validateEngine()
                    Log.i(TAG, "recoverOnStartup: Restored engine validation: $isRestoredValid")
                } else {
                    Log.e(TAG, "recoverOnStartup: Backup file missing. Re-extracting baseline asset...")
                    runtimeDir.mkdirs()
                    YoutubeDL.getInstance().init_ytdlp(context, runtimeDir)
                    prefs.edit().putString(KEY_TX_STATE, TX_STATE_IDLE).apply()
                }
                _engineState.value = EngineUpdateState.Idle(getCurrentEngineVersion())
            }
        } catch (e: Exception) {
            Log.e(TAG, "recoverOnStartup: Exception during startup recovery", e)
            prefs.edit().putString(KEY_TX_STATE, TX_STATE_IDLE).apply()
            _engineState.value = EngineUpdateState.Idle(getCurrentEngineVersion())
        }
    }

    private fun createRollbackBackup(currentVersion: String): Boolean {
        return try {
            if (!runtimeFile.exists() || runtimeFile.length() == 0L) {
                Log.w(TAG, "createRollbackBackup: Runtime file does not exist to back up")
                return false
            }

            backupDir.mkdirs()
            val tempBackup = File(backupDir, "yt-dlp.backup.tmp")
            if (tempBackup.exists()) tempBackup.delete()

            copyFile(runtimeFile, tempBackup)
            if (backupFile.exists()) backupFile.delete()
            tempBackup.renameTo(backupFile)

            // Save previous version metadata
            val currentVerName = ytdlpAndroidPrefs.getString("dlpVersionName", "yt-dlp $currentVersion")
            prefs.edit()
                .putString(KEY_TX_STATE, TX_STATE_BACKUP_CREATED)
                .putString(KEY_BACKUP_VERSION, currentVersion)
                .putString(KEY_BACKUP_VERSION_NAME, currentVerName)
                .apply()

            Log.i(TAG, "createRollbackBackup: Successfully backed up ${runtimeFile.length()} bytes (v$currentVersion)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "createRollbackBackup: Failed to back up runtime", e)
            false
        }
    }

    private fun cleanupBackup() {
        try {
            if (backupDir.exists()) {
                backupDir.listFiles()?.forEach { it.delete() }
                backupDir.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "cleanupBackup: Failed to clean backup directory", e)
        }
    }

    private fun copyFile(src: File, dst: File) {
        FileInputStream(src).use { input ->
            FileOutputStream(dst).use { output ->
                input.copyTo(output)
            }
        }
    }
}
