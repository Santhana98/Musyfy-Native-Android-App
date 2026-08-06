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

    @Test
    fun testImportPerformanceFunnel_timeToMusic() {
        var mockTime = 1000L
        tracker.getElapsedRealtimeMs = { mockTime }

        // 1. Add to Library clicked
        val url = "https://www.youtube.com/watch?v=video_xyz"
        val sessionId = tracker.trackAddToLibraryClicked(url)
        assertEquals(2, loggedEvents.size) // add_to_library_clicked + import_started

        // 2. Metadata extraction started
        mockTime = 1200L
        tracker.trackMetadataExtractionStarted(url)
        assertEquals(3, loggedEvents.size)

        // 3. Metadata extraction completed
        mockTime = 2000L // 800ms metadata duration
        tracker.trackMetadataExtractionCompleted(url, "video_xyz", "Test Song", "Test Artist")
        assertEquals(4, loggedEvents.size)
        val metadataEvent = loggedEvents[3]
        assertEquals(800L, metadataEvent.params[AnalyticsConstants.Params.METADATA_DURATION_MS])

        // 4. Download started & First audio cached after 350ms elapsed
        mockTime = 2200L
        tracker.trackDownloadStarted("video_xyz")
        mockTime = 2550L // 350ms elapsed during downloading
        tracker.trackFirstAudioCached("video_xyz")
        assertEquals(6, loggedEvents.size)
        val cacheEvent = loggedEvents[5]
        assertEquals(AnalyticsConstants.Events.FIRST_AUDIO_CACHED, cacheEvent.name)
        assertEquals(350L, cacheEvent.params[AnalyticsConstants.Params.CACHE_READY_DURATION_MS])

        // 5. Playback ready (Time to music)
        mockTime = 4500L // 3500ms total from click (4500 - 1000)
        tracker.trackPlaybackReady("video_xyz")
        assertEquals(7, loggedEvents.size)
        val playbackEvent = loggedEvents[6]
        assertEquals(AnalyticsConstants.Events.PLAYBACK_READY, playbackEvent.name)
        assertEquals(3500L, playbackEvent.params[AnalyticsConstants.Params.TIME_TO_MUSIC_MS])
        assertEquals("import", playbackEvent.params[AnalyticsConstants.Params.PLAYBACK_STARTED_FROM])

        // 6. Duplicate first audio cached & playback ready calls are ignored (emit only once)
        tracker.trackFirstAudioCached("video_xyz")
        tracker.trackPlaybackReady("video_xyz")
        assertEquals(7, loggedEvents.size)
    }

    @Test
    fun testPlaybackReadyFiresAfterImportCompleted() {
        var mockTime = 1000L
        tracker.getElapsedRealtimeMs = { mockTime }

        val url = "https://www.youtube.com/watch?v=video_abc"
        tracker.trackAddToLibraryClicked(url)
        mockTime = 1500L
        tracker.trackMetadataExtractionStarted(url)
        mockTime = 2000L
        tracker.trackMetadataExtractionCompleted(url, "video_abc", "Song Title", "Artist")
        mockTime = 2200L
        tracker.trackDownloadStarted("video_abc")
        mockTime = 3000L
        tracker.trackDownloadCompleted("video_abc", 5_000_000L)
        
        // Import completed fires first (e.g. at 3.1s)
        mockTime = 3100L
        tracker.trackImportCompleted("video_abc")

        // Playback starts AFTER import completed (e.g. at 5.2s)
        mockTime = 5200L // 4200ms time_to_music_ms
        tracker.trackPlaybackReady("video_abc")

        val playbackEvent = loggedEvents.last()
        assertEquals(AnalyticsConstants.Events.PLAYBACK_READY, playbackEvent.name)
        assertEquals(4200L, playbackEvent.params[AnalyticsConstants.Params.TIME_TO_MUSIC_MS])
        assertEquals("import", playbackEvent.params[AnalyticsConstants.Params.PLAYBACK_STARTED_FROM])
    }
}
