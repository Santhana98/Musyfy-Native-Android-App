package com.musyfy.nativeapp.feature.download.data

import android.content.Context
import com.musyfy.nativeapp.core.analytics.AnalyticsConstants
import com.musyfy.nativeapp.core.analytics.ImportAnalyticsTracker
import com.musyfy.nativeapp.core.validation.InputValidator
import com.musyfy.nativeapp.core.validation.ValidationResult
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.SongRepository
import com.musyfy.nativeapp.feature.download.domain.SongDownloader
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed interface YoutubeImportState {
    object Idle : YoutubeImportState
    data class ExtractingMetadata(val url: String) : YoutubeImportState
    data class MetadataReady(val url: String, val info: YoutubeVideoInfo) : YoutubeImportState
    data class Importing(val url: String, val info: YoutubeVideoInfo) : YoutubeImportState
    data class Success(val url: String, val info: YoutubeVideoInfo, val song: Song) : YoutubeImportState
    data class Error(
        val url: String,
        val message: String,
        val isMetadataError: Boolean,
        val info: YoutubeVideoInfo? = null
    ) : YoutubeImportState
}

/**
 * App-level single source of truth for YouTube metadata extraction and import operations.
 * Decouples extraction & import preparation from screen/ViewModel lifecycles so that operations
 * continue uninterrupted across navigation, recomposition, playback, and tab switching.
 */
@Singleton
class YoutubeImportCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository,
    private val songDownloader: SongDownloader,
    private val importAnalyticsTracker: ImportAnalyticsTracker,
    private val inputValidator: InputValidator
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _currentUrl = MutableStateFlow("")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _importState = MutableStateFlow<YoutubeImportState>(YoutubeImportState.Idle)
    val importState: StateFlow<YoutubeImportState> = _importState.asStateFlow()

    private var metadataExtractionJob: Job? = null
    private var importJob: Job? = null

    fun onUrlChanged(url: String, force: Boolean = false) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            clear()
            return
        }

        val videoId = YoutubeMetadataExtractor.extractVideoId(trimmed)
        if (videoId == null) {
            _currentUrl.value = trimmed
            _importState.value = YoutubeImportState.Error(
                url = trimmed,
                message = "Invalid YouTube URL format",
                isMetadataError = true
            )
            return
        }

        val currentState = _importState.value
        if (!force && trimmed == _currentUrl.value) {
            when (currentState) {
                is YoutubeImportState.ExtractingMetadata,
                is YoutubeImportState.MetadataReady,
                is YoutubeImportState.Importing,
                is YoutubeImportState.Success -> return // Preserve in-flight or completed state; avoid duplicate jobs/analytics
                else -> {}
            }
        }

        // Cancel previous extraction job if URL changed or force retry requested
        metadataExtractionJob?.cancel()
        _currentUrl.value = trimmed
        _importState.value = YoutubeImportState.ExtractingMetadata(trimmed)

        metadataExtractionJob = scope.launch {
            val fullUrl = if (trimmed.startsWith("http")) trimmed else "https://www.youtube.com/watch?v=$videoId"
            val info = withContext(Dispatchers.IO) {
                YoutubeMetadataExtractor.fetchVideoInfo(context, fullUrl)
            }
            if (info != null) {
                _importState.value = YoutubeImportState.MetadataReady(trimmed, info)
            } else {
                _importState.value = YoutubeImportState.Error(
                    url = trimmed,
                    message = "Unable to fetch song details. Check your connection.",
                    isMetadataError = true
                )
            }
        }
    }

    fun handleSharedUrl(url: String) {
        onUrlChanged(url)
    }

    fun startImport(
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
        source: String = "youtube"
    ) {
        val currentState = _importState.value
        val (url, info) = when (currentState) {
            is YoutubeImportState.MetadataReady -> Pair(currentState.url, currentState.info)
            is YoutubeImportState.Importing -> return // Already importing
            is YoutubeImportState.Success -> {
                onSuccess?.invoke()
                return
            }
            else -> {
                onError?.invoke("No video metadata ready to import.")
                return
            }
        }

        val validation = inputValidator.validateYoutubeUrl(url)
        val sanitizedUrl = if (validation is ValidationResult.Success) validation.sanitizedInput else url
        val canonicalUrl = if (sanitizedUrl.startsWith("http")) sanitizedUrl else "https://www.youtube.com/watch?v=${info.id}"

        // Milestone 1: Track Add to Library Clicked
        val sessionId = importAnalyticsTracker.trackAddToLibraryClicked(canonicalUrl, source = source)

        _importState.value = YoutubeImportState.Importing(url, info)

        importJob?.cancel()
        importJob = scope.launch(Dispatchers.IO) {
            // Milestone 2: Metadata Extraction Started
            importAnalyticsTracker.trackMetadataExtractionStarted(canonicalUrl)

            val existingSongs = songRepository.getSongs().first()
            val existingSong = existingSongs.find { it.id == info.id }
            if (existingSong != null && existingSong.audioPath != null && java.io.File(existingSong.audioPath).exists()) {
                withContext(Dispatchers.Main) {
                    _importState.value = YoutubeImportState.Success(url, info, existingSong)
                    onSuccess?.invoke()
                }
                return@launch
            }

            val sanitizedTitle = inputValidator.sanitize(info.title).ifEmpty { "Untitled Track" }
            val sanitizedArtist = inputValidator.sanitize(info.uploader).ifEmpty { "Unknown Artist" }

            // Milestone 3: Metadata Extraction Completed
            importAnalyticsTracker.trackMetadataExtractionCompleted(
                keyOrUrl = canonicalUrl,
                songId = info.id,
                songTitle = sanitizedTitle,
                artist = sanitizedArtist
            )

            val newSong = Song(
                id = info.id,
                title = sanitizedTitle,
                artist = sanitizedArtist,
                url = canonicalUrl,
                imageUrl = info.thumbnail,
                durationMs = (info.duration ?: 0) * 1000L
            )

            songRepository.addSong(newSong)

            // Milestone 4: Download Started
            importAnalyticsTracker.trackDownloadStarted(newSong.id)

            try {
                songDownloader.downloadSong(newSong).collect { status ->
                    when (status) {
                        is DownloadStatus.Downloading -> {
                            // Milestone 5: First Audio Cached
                            importAnalyticsTracker.trackFirstAudioCached(newSong.id)
                        }
                        is DownloadStatus.Downloaded -> {
                            // Milestone 7: Download Completed
                            val fileSize = java.io.File(status.localPath).length()
                            importAnalyticsTracker.trackDownloadCompleted(newSong.id, fileSize)

                            val persistedSong = songRepository.getSongs().first().find { it.id == newSong.id }
                            if (persistedSong != null && persistedSong.audioPath != null) {
                                importAnalyticsTracker.trackImportCompleted(newSong.id, source = source)
                            } else {
                                importAnalyticsTracker.trackImportFailedWithStage(
                                    keyOrSongId = newSong.id,
                                    failureReason = AnalyticsConstants.FailureReasons.STORAGE_ERROR,
                                    failureStage = AnalyticsConstants.FailureStages.STORAGE,
                                    source = source
                                )
                            }

                            withContext(Dispatchers.Main) {
                                val finalSong = persistedSong ?: newSong.copy(audioPath = status.localPath)
                                _importState.value = YoutubeImportState.Success(url, info, finalSong)
                                onSuccess?.invoke()
                            }
                        }
                        is DownloadStatus.Error -> {
                            val normalizedDownloadReason = normalizeDownloadErrorForDownload(status.message)
                            importAnalyticsTracker.trackDownloadFailed(newSong.id, normalizedDownloadReason)

                            val normalizedReason = normalizeDownloadError(status.message)
                            val failureStage = getFailureStageFromReason(normalizedReason)
                            importAnalyticsTracker.trackImportFailedWithStage(
                                keyOrSongId = newSong.id,
                                failureReason = normalizedReason,
                                failureStage = failureStage,
                                source = source
                            )

                            withContext(Dispatchers.Main) {
                                _importState.value = YoutubeImportState.Error(
                                    url = url,
                                    message = status.message.ifEmpty { "Download failed. Please check your connection." },
                                    isMetadataError = false,
                                    info = info
                                )
                                onError?.invoke(status.message)
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _importState.value = YoutubeImportState.Error(
                        url = url,
                        message = e.message ?: "Download failed",
                        isMetadataError = false,
                        info = info
                    )
                    onError?.invoke(e.message ?: "Download failed")
                }
            }
        }
    }

    fun retry() {
        val url = _currentUrl.value
        if (url.isNotEmpty()) {
            onUrlChanged(url, force = true)
        }
    }

    fun clear() {
        metadataExtractionJob?.cancel()
        metadataExtractionJob = null
        importJob?.cancel()
        importJob = null
        _currentUrl.value = ""
        _importState.value = YoutubeImportState.Idle
    }

    private fun normalizeDownloadErrorForDownload(raw: String): String {
        val msg = raw.lowercase()
        return when {
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") -> AnalyticsConstants.FailureReasons.NETWORK_ERROR
            msg.contains("space") || msg.contains("storage") || msg.contains("disk") -> AnalyticsConstants.FailureReasons.STORAGE_ERROR
            else -> AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR
        }
    }

    private fun normalizeDownloadError(raw: String): String {
        val msg = raw.lowercase()
        return when {
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") -> AnalyticsConstants.FailureReasons.NETWORK_ERROR
            msg.contains("space") || msg.contains("storage") || msg.contains("disk") -> AnalyticsConstants.FailureReasons.STORAGE_ERROR
            else -> AnalyticsConstants.FailureReasons.METADATA_ERROR
        }
    }

    private fun getFailureStageFromReason(reason: String): String {
        return when (reason) {
            AnalyticsConstants.FailureReasons.NETWORK_ERROR -> AnalyticsConstants.FailureStages.NETWORK
            AnalyticsConstants.FailureReasons.METADATA_ERROR -> AnalyticsConstants.FailureStages.METADATA
            AnalyticsConstants.FailureReasons.STORAGE_ERROR -> AnalyticsConstants.FailureStages.STORAGE
            else -> AnalyticsConstants.FailureStages.DOWNLOAD
        }
    }
}
