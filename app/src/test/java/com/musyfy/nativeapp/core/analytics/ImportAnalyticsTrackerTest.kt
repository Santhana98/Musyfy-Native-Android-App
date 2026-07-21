package com.musyfy.nativeapp.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportAnalyticsTrackerTest {

    private val loggedEvents = mutableListOf<AnalyticsEvent>()

    private val fakeAnalyticsManager = object : AnalyticsManager {
        override fun logEvent(event: AnalyticsEvent) {
            loggedEvents.add(event)
        }
        override fun logScreenView(screenName: String, className: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private val tracker = ImportAnalyticsTracker(fakeAnalyticsManager)

    @Test
    fun testImportLifecycle_success() {
        // 1. Start import
        tracker.trackImportStarted("song_123", "youtube")
        assertEquals(1, loggedEvents.size)
        val startedEvent = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.IMPORT_STARTED, startedEvent.name)
        val sessionId = startedEvent.params[AnalyticsConstants.Params.IMPORT_SESSION_ID] as? String
        assertNotNull(sessionId)
        assertEquals("youtube", startedEvent.params[AnalyticsConstants.Params.IMPORT_SOURCE])

        // 2. Complete import
        tracker.trackImportCompleted("song_123", "youtube")
        assertEquals(2, loggedEvents.size)
        val completedEvent = loggedEvents[1]
        assertEquals(AnalyticsConstants.Events.IMPORT_COMPLETED, completedEvent.name)
        assertEquals(sessionId, completedEvent.params[AnalyticsConstants.Params.IMPORT_SESSION_ID])
        assertEquals("youtube", completedEvent.params[AnalyticsConstants.Params.IMPORT_SOURCE])

        // 3. Subsequent calls (e.g. duplicate calls) are ignored (no-op)
        tracker.trackImportCompleted("song_123", "youtube")
        assertEquals(2, loggedEvents.size) // No new events logged
    }

    @Test
    fun testImportLifecycle_failed() {
        // 1. Start import
        tracker.trackImportStarted("song_456", "youtube")
        assertEquals(1, loggedEvents.size)
        val sessionId = loggedEvents[0].params[AnalyticsConstants.Params.IMPORT_SESSION_ID] as? String

        // 2. Fail import
        tracker.trackImportFailed("song_456", AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR, "youtube")
        assertEquals(2, loggedEvents.size)
        val failedEvent = loggedEvents[1]
        assertEquals(AnalyticsConstants.Events.IMPORT_FAILED, failedEvent.name)
        assertEquals(sessionId, failedEvent.params[AnalyticsConstants.Params.IMPORT_SESSION_ID])
        assertEquals(AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR, failedEvent.params[AnalyticsConstants.Params.FAILURE_REASON])

        // 3. Subsequent calls are ignored
        tracker.trackImportCancelled("song_456", "youtube")
        assertEquals(2, loggedEvents.size)
    }

    @Test
    fun testImportLifecycle_cancelled() {
        // 1. Start import
        tracker.trackImportStarted("song_789", "youtube")
        assertEquals(1, loggedEvents.size)
        val sessionId = loggedEvents[0].params[AnalyticsConstants.Params.IMPORT_SESSION_ID] as? String

        // 2. Cancel import
        tracker.trackImportCancelled("song_789", "youtube")
        assertEquals(2, loggedEvents.size)
        val cancelledEvent = loggedEvents[1]
        assertEquals(AnalyticsConstants.Events.IMPORT_CANCELLED, cancelledEvent.name)
        assertEquals(sessionId, cancelledEvent.params[AnalyticsConstants.Params.IMPORT_SESSION_ID])

        // 3. Subsequent calls are ignored
        tracker.trackImportCompleted("song_789", "youtube")
        assertEquals(2, loggedEvents.size)
    }

    @Test
    fun testNonExistentSession_ignored() {
        tracker.trackImportCompleted("non_existent", "youtube")
        assertTrue(loggedEvents.isEmpty())
    }

    @Test
    fun testDownloadLifecycle_success() {
        var mockTime = 1000L
        tracker.getElapsedRealtimeMs = { mockTime }

        // 1. Start download
        tracker.trackDownloadStarted("song_abc")
        assertEquals(1, loggedEvents.size)
        val startedEvent = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.DOWNLOAD_STARTED, startedEvent.name)
        val sessionId = startedEvent.params[AnalyticsConstants.Params.DOWNLOAD_SESSION_ID] as? String
        assertNotNull(sessionId)

        // 2. Ignore subsequent progress updates
        tracker.trackDownloadStarted("song_abc")
        assertEquals(1, loggedEvents.size)

        // 3. Complete download
        mockTime = 3500L // 2.5 seconds elapsed
        tracker.trackDownloadCompleted("song_abc")
        assertEquals(2, loggedEvents.size)
        val completedEvent = loggedEvents[1]
        assertEquals(AnalyticsConstants.Events.DOWNLOAD_COMPLETED, completedEvent.name)
        assertEquals(sessionId, completedEvent.params[AnalyticsConstants.Params.DOWNLOAD_SESSION_ID])
        assertEquals(2500L, completedEvent.params[AnalyticsConstants.Params.DOWNLOAD_DURATION_MS])

        // 4. Duplicate terminal calls are ignored
        tracker.trackDownloadCompleted("song_abc")
        assertEquals(2, loggedEvents.size)
    }

    @Test
    fun testDownloadLifecycle_failed() {
        var mockTime = 2000L
        tracker.getElapsedRealtimeMs = { mockTime }

        // 1. Start download
        tracker.trackDownloadStarted("song_def")
        val sessionId = loggedEvents[0].params[AnalyticsConstants.Params.DOWNLOAD_SESSION_ID] as? String

        // 2. Fail download
        mockTime = 4200L // 2.2 seconds elapsed
        tracker.trackDownloadFailed("song_def", AnalyticsConstants.FailureReasons.PERMISSION_ERROR)
        assertEquals(2, loggedEvents.size)
        val failedEvent = loggedEvents[1]
        assertEquals(AnalyticsConstants.Events.DOWNLOAD_FAILED, failedEvent.name)
        assertEquals(sessionId, failedEvent.params[AnalyticsConstants.Params.DOWNLOAD_SESSION_ID])
        assertEquals(2200L, failedEvent.params[AnalyticsConstants.Params.DOWNLOAD_DURATION_MS])
        assertEquals(AnalyticsConstants.FailureReasons.PERMISSION_ERROR, failedEvent.params[AnalyticsConstants.Params.FAILURE_REASON])
    }

    @Test
    fun testDownloadLifecycle_cancelled() {
        var mockTime = 3000L
        tracker.getElapsedRealtimeMs = { mockTime }

        // 1. Start download
        tracker.trackDownloadStarted("song_ghi")
        val sessionId = loggedEvents[0].params[AnalyticsConstants.Params.DOWNLOAD_SESSION_ID] as? String

        // 2. Cancel download
        mockTime = 5100L // 2.1 seconds elapsed
        tracker.trackDownloadCancelled("song_ghi")
        assertEquals(2, loggedEvents.size)
        val cancelledEvent = loggedEvents[1]
        assertEquals(AnalyticsConstants.Events.DOWNLOAD_CANCELLED, cancelledEvent.name)
        assertEquals(sessionId, cancelledEvent.params[AnalyticsConstants.Params.DOWNLOAD_SESSION_ID])
        assertEquals(2100L, cancelledEvent.params[AnalyticsConstants.Params.DOWNLOAD_DURATION_MS])
    }
}
