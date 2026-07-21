package com.musyfy.nativeapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks YouTube import flow lifecycle events.
 *
 * NOTE: Concurrent imports of the same YouTube video are not supported in this milestone
 * because the tracker maps a single songId to a single import_session_id. This assumption
 * can be easily revisited in future milestones if support for concurrent duplicate imports
 * is introduced.
 */
@Singleton
class ImportAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    // Helper structure for tracking download sessions
    private data class DownloadSession(
        val sessionId: String,
        val startTimeMs: Long
    )

    // Thread-safe map of songId -> import_session_id
    private val activeSessions = ConcurrentHashMap<String, String>()

    // Thread-safe map of songId -> DownloadSession
    private val activeDownloadSessions = ConcurrentHashMap<String, DownloadSession>()

    // Monotonic clock provider for tracking durations (customizable in unit tests)
    internal var getElapsedRealtimeMs: () -> Long = { android.os.SystemClock.elapsedRealtime() }

    /**
     * Registers the start of an import session and logs the import_started event.
     */
    fun trackImportStarted(songId: String, source: String = "youtube") {
        val sessionId = java.util.UUID.randomUUID().toString()
        activeSessions[songId] = sessionId
        analyticsManager.logEvent(AnalyticsEvent.importStarted(sessionId, source))
    }

    /**
     * Finalizes a successful import session and logs the import_completed event.
     * No-op if there is no active session matching the songId.
     */
    fun trackImportCompleted(songId: String, source: String = "youtube") {
        val sessionId = activeSessions.remove(songId) ?: return
        analyticsManager.logEvent(AnalyticsEvent.importCompleted(sessionId, source))
    }

    /**
     * Terminate an import session with a failure and logs the import_failed event.
     * No-op if there is no active session matching the songId.
     */
    fun trackImportFailed(songId: String, failureReason: String, source: String = "youtube") {
        val sessionId = activeSessions.remove(songId) ?: return
        analyticsManager.logEvent(AnalyticsEvent.importFailed(sessionId, failureReason, source))
    }

    /**
     * Explicitly cancel an active import session and logs the import_cancelled event.
     * No-op if there is no active session matching the songId.
     */
    fun trackImportCancelled(songId: String, source: String = "youtube") {
        val sessionId = activeSessions.remove(songId) ?: return
        analyticsManager.logEvent(AnalyticsEvent.importCancelled(sessionId, source))
    }

    /**
     * Registers the start of a download session and logs the download_started event.
     * Subsequent calls for an already active session (e.g. download progress updates) are ignored.
     */
    fun trackDownloadStarted(songId: String) {
        if (activeDownloadSessions.containsKey(songId)) return
        
        val sessionId = java.util.UUID.randomUUID().toString()
        val startTime = getElapsedRealtimeMs()
        activeDownloadSessions[songId] = DownloadSession(sessionId, startTime)
        analyticsManager.logEvent(AnalyticsEvent.downloadStarted(sessionId))
    }

    /**
     * Finalizes a successful download session and logs the download_completed event.
     * No-op if there is no active session matching the songId.
     */
    fun trackDownloadCompleted(songId: String) {
        val session = activeDownloadSessions.remove(songId) ?: return
        val durationMs = getElapsedRealtimeMs() - session.startTimeMs
        analyticsManager.logEvent(AnalyticsEvent.downloadCompleted(session.sessionId, durationMs))
    }

    /**
     * Terminates a download session with a failure and logs the download_failed event.
     * No-op if there is no active session matching the songId.
     */
    fun trackDownloadFailed(songId: String, failureReason: String) {
        val session = activeDownloadSessions.remove(songId) ?: return
        val durationMs = getElapsedRealtimeMs() - session.startTimeMs
        analyticsManager.logEvent(AnalyticsEvent.downloadFailed(session.sessionId, failureReason, durationMs))
    }

    /**
     * Explicitly cancels an active download session and logs the download_cancelled event.
     * No-op if there is no active session matching the songId.
     */
    fun trackDownloadCancelled(songId: String) {
        val session = activeDownloadSessions.remove(songId) ?: return
        val durationMs = getElapsedRealtimeMs() - session.startTimeMs
        analyticsManager.logEvent(AnalyticsEvent.downloadCancelled(session.sessionId, durationMs))
    }
}
