package com.musyfy.nativeapp.core.analytics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks YouTube import flow lifecycle and performance analytics events (Phase 14.1).
 * Measures the complete import journey from "Add to Library" -> "Playback Ready" (time_to_music_ms).
 */
@Singleton
class ImportAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    @ApplicationContext private val context: Context? = null
) {
    // Helper structure for tracking download sessions
    private data class DownloadSession(
        val sessionId: String,
        val startTimeMs: Long
    )

    // Data structure for complete import performance funnel
    private data class ImportPerformanceSession(
        val sessionId: String,
        val source: String,
        val addToLibraryTimeMs: Long,
        var songId: String? = null,
        var metadataStartTimeMs: Long = 0L,
        var metadataEndTimeMs: Long = 0L,
        var downloadStartTimeMs: Long = 0L,
        var firstAudioCachedTimeMs: Long = 0L,
        var playbackReadyTimeMs: Long = 0L,
        var downloadEndTimeMs: Long = 0L,
        var songTitle: String? = null,
        var artist: String? = null,
        var fileSizeBytes: Long = 0L,
        var firstAudioCachedEmitted: Boolean = false,
        var playbackReadyEmitted: Boolean = false
    )

    // Thread-safe map of songId/key -> import_session_id
    private val activeSessions = ConcurrentHashMap<String, String>()

    // Thread-safe map of sessionId -> ImportPerformanceSession
    private val activePerformanceSessions = ConcurrentHashMap<String, ImportPerformanceSession>()

    // Thread-safe mapping of songId -> sessionId
    private val songIdToSessionIdMap = ConcurrentHashMap<String, String>()

    // Thread-safe map of songId -> DownloadSession
    private val activeDownloadSessions = ConcurrentHashMap<String, DownloadSession>()

    // Monotonic clock provider for tracking durations (customizable in unit tests)
    internal var getElapsedRealtimeMs: () -> Long = { android.os.SystemClock.elapsedRealtime() }

    private fun getNetworkType(): String {
        val ctx = context ?: return "unknown"
        return try {
            val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "unknown"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork ?: return "unknown"
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "unknown"
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                    else -> "unknown"
                }
            } else {
                @Suppress("DEPRECATION")
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                when (activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI -> "wifi"
                    ConnectivityManager.TYPE_MOBILE -> "cellular"
                    ConnectivityManager.TYPE_ETHERNET -> "ethernet"
                    else -> "unknown"
                }
            }
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Milestone 1: User clicks "Add to Library" CTA button.
     * Initiates a new ImportPerformanceSession and logs add_to_library_clicked event.
     */
    fun trackAddToLibraryClicked(keyOrUrl: String, source: String = "youtube"): String {
        val sessionId = java.util.UUID.randomUUID().toString()
        val now = getElapsedRealtimeMs()
        val session = ImportPerformanceSession(
            sessionId = sessionId,
            source = source,
            addToLibraryTimeMs = now
        )
        activePerformanceSessions[sessionId] = session
        activeSessions[keyOrUrl] = sessionId
        analyticsManager.logEvent(AnalyticsEvent.addToLibraryClicked(sessionId, source))
        analyticsManager.logEvent(AnalyticsEvent.importStarted(sessionId, source))
        return sessionId
    }

    /**
     * Registers the start of an import session (legacy compatibility).
     */
    fun trackImportStarted(songId: String, source: String = "youtube") {
        if (activeSessions.containsKey(songId)) return
        val sessionId = java.util.UUID.randomUUID().toString()
        activeSessions[songId] = sessionId
        analyticsManager.logEvent(AnalyticsEvent.importStarted(sessionId, source))
    }

    /**
     * Milestone 2: Metadata extraction started.
     */
    fun trackMetadataExtractionStarted(keyOrSongId: String) {
        val sessionId = activeSessions[keyOrSongId] ?: songIdToSessionIdMap[keyOrSongId] ?: keyOrSongId
        val session = activePerformanceSessions[sessionId] ?: return
        session.metadataStartTimeMs = getElapsedRealtimeMs()
        analyticsManager.logEvent(AnalyticsEvent.metadataExtractionStarted(sessionId, keyOrSongId))
    }

    /**
     * Milestone 3: Metadata extraction completed.
     */
    fun trackMetadataExtractionCompleted(
        keyOrUrl: String,
        songId: String,
        songTitle: String,
        artist: String
    ) {
        val sessionId = activeSessions[keyOrUrl] ?: activeSessions[songId] ?: songId
        val session = activePerformanceSessions[sessionId] ?: return
        val now = getElapsedRealtimeMs()
        session.metadataEndTimeMs = now
        session.songId = songId
        session.songTitle = songTitle
        session.artist = artist
        songIdToSessionIdMap[songId] = sessionId
        activeSessions[songId] = sessionId

        val metadataDurationMs = if (session.metadataStartTimeMs > 0) {
            now - session.metadataStartTimeMs
        } else {
            now - session.addToLibraryTimeMs
        }

        analyticsManager.logEvent(
            AnalyticsEvent.metadataExtractionCompleted(
                sessionId = sessionId,
                videoId = songId,
                metadataDurationMs = metadataDurationMs,
                songTitle = songTitle,
                artist = artist
            )
        )
    }

    /**
     * Milestone 4: Download started.
     */
    fun trackDownloadStarted(songId: String) {
        val sessionId = songIdToSessionIdMap[songId] ?: activeSessions[songId]
        if (sessionId != null) {
            val session = activePerformanceSessions[sessionId]
            if (session != null && session.downloadStartTimeMs == 0L) {
                session.downloadStartTimeMs = getElapsedRealtimeMs()
            }
        }

        if (activeDownloadSessions.containsKey(songId)) return
        val downloadSessionId = sessionId ?: java.util.UUID.randomUUID().toString()
        val startTime = getElapsedRealtimeMs()
        activeDownloadSessions[songId] = DownloadSession(downloadSessionId, startTime)
        analyticsManager.logEvent(AnalyticsEvent.downloadStarted(downloadSessionId))
    }

    /**
     * Milestone 5: First audio cached (emitted only once per session).
     */
    fun trackFirstAudioCached(songId: String) {
        val sessionId = songIdToSessionIdMap[songId] ?: activeSessions[songId] ?: return
        val session = activePerformanceSessions[sessionId] ?: return
        if (session.firstAudioCachedEmitted) return
        session.firstAudioCachedEmitted = true

        val now = getElapsedRealtimeMs()
        session.firstAudioCachedTimeMs = now
        val cacheReadyDurationMs = if (session.downloadStartTimeMs > 0) {
            now - session.downloadStartTimeMs
        } else {
            now - session.addToLibraryTimeMs
        }

        analyticsManager.logEvent(
            AnalyticsEvent.firstAudioCached(
                sessionId = sessionId,
                videoId = songId,
                cacheReadyDurationMs = cacheReadyDurationMs,
                networkType = getNetworkType()
            )
        )
    }

    /**
     * Milestone 6: Playback Ready (North Star Metric: time_to_music_ms).
     * Emitted only once per import session when music starts playing for the imported track.
     * Performs terminal session cleanup after successfully logging playback_ready.
     */
    fun trackPlaybackReady(songId: String, playbackStartedFrom: String = "import") {
        val sessionId = songIdToSessionIdMap[songId] ?: activeSessions[songId] ?: return
        val session = activePerformanceSessions[sessionId] ?: return
        if (session.playbackReadyEmitted) return
        session.playbackReadyEmitted = true

        val now = getElapsedRealtimeMs()
        session.playbackReadyTimeMs = now
        val timeToMusicMs = now - session.addToLibraryTimeMs
        val networkType = getNetworkType()

        analyticsManager.logEvent(
            AnalyticsEvent.playbackReady(
                sessionId = sessionId,
                songId = songId,
                timeToMusicMs = timeToMusicMs,
                networkType = networkType,
                playbackStartedFrom = playbackStartedFrom
            )
        )

        // Terminal session cleanup after successful playback_ready event
        activePerformanceSessions.remove(sessionId)
        songIdToSessionIdMap.remove(songId)
        activeSessions.remove(songId)
    }

    /**
     * Milestone 7: Download completed.
     */
    fun trackDownloadCompleted(songId: String, fileSizeBytes: Long = 0L) {
        val session = activeDownloadSessions.remove(songId)
        val durationMs = if (session != null) getElapsedRealtimeMs() - session.startTimeMs else 0L

        val sessionId = songIdToSessionIdMap[songId] ?: activeSessions[songId]
        val downloadSessionId = session?.sessionId ?: sessionId ?: songId

        val averageSpeedMbps = if (durationMs > 0 && fileSizeBytes > 0) {
            (fileSizeBytes * 8.0 / 1_000_000.0) / (durationMs / 1000.0)
        } else 0.0

        val networkType = getNetworkType()

        analyticsManager.logEvent(
            AnalyticsEvent.downloadCompleted(
                sessionId = downloadSessionId,
                durationMs = durationMs,
                fileSizeBytes = fileSizeBytes,
                averageDownloadSpeedMbps = averageSpeedMbps,
                networkType = networkType
            )
        )

        if (sessionId != null) {
            val perfSession = activePerformanceSessions[sessionId]
            if (perfSession != null) {
                perfSession.downloadEndTimeMs = getElapsedRealtimeMs()
                perfSession.fileSizeBytes = fileSizeBytes
            }
        }
    }

    /**
     * Milestone 8: Import completed.
     * Logs import_completed event without purging the performance session so playback_ready can still be recorded.
     */
    fun trackImportCompleted(songId: String, source: String = "youtube") {
        val sessionId = songIdToSessionIdMap[songId] ?: activeSessions[songId]
        if (sessionId != null) {
            analyticsManager.logEvent(AnalyticsEvent.importCompleted(sessionId, source))
        }
    }

    /**
     * Milestone 9: Import failed (with failure stage).
     */
    fun trackImportFailedWithStage(
        keyOrSongId: String,
        failureReason: String,
        failureStage: String = AnalyticsConstants.FailureStages.UNKNOWN,
        source: String = "youtube"
    ) {
        val sessionId = activeSessions.remove(keyOrSongId)
            ?: songIdToSessionIdMap.remove(keyOrSongId)
            ?: keyOrSongId
        activePerformanceSessions.remove(sessionId)
        activeDownloadSessions.remove(keyOrSongId)

        analyticsManager.logEvent(
            AnalyticsEvent.importFailedWithStage(
                sessionId = sessionId,
                failureReason = failureReason,
                failureStage = failureStage,
                source = source,
                networkType = getNetworkType()
            )
        )
    }

    /**
     * Legacy import failed handler.
     */
    fun trackImportFailed(songId: String, failureReason: String, source: String = "youtube") {
        trackImportFailedWithStage(
            keyOrSongId = songId,
            failureReason = failureReason,
            failureStage = AnalyticsConstants.FailureStages.UNKNOWN,
            source = source
        )
    }

    /**
     * Explicitly cancel an active import session.
     */
    fun trackImportCancelled(songId: String, source: String = "youtube") {
        val sessionId = activeSessions.remove(songId) ?: songIdToSessionIdMap.remove(songId)
        if (sessionId != null) {
            activePerformanceSessions.remove(sessionId)
            analyticsManager.logEvent(AnalyticsEvent.importCancelled(sessionId, source))
        }
    }

    fun trackDownloadFailed(songId: String, failureReason: String) {
        val session = activeDownloadSessions.remove(songId) ?: return
        val durationMs = getElapsedRealtimeMs() - session.startTimeMs
        analyticsManager.logEvent(AnalyticsEvent.downloadFailed(session.sessionId, failureReason, durationMs))
    }

    fun trackDownloadCancelled(songId: String) {
        val session = activeDownloadSessions.remove(songId) ?: return
        val durationMs = getElapsedRealtimeMs() - session.startTimeMs
        analyticsManager.logEvent(AnalyticsEvent.downloadCancelled(session.sessionId, durationMs))
    }
}
