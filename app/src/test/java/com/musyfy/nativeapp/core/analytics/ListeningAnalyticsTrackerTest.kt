package com.musyfy.nativeapp.core.analytics

import androidx.media3.common.Player
import com.musyfy.nativeapp.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ListeningAnalyticsTrackerTest {

    private val loggedEvents = mutableListOf<AnalyticsEvent>()
    private var mockCurrentTimeMs = 1000L

    private val fakeAnalyticsManager = object : AnalyticsManager {
        override fun logEvent(event: AnalyticsEvent) {
            loggedEvents.add(event)
        }
        override fun logScreenView(screenName: String, className: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private lateinit var tracker: ListeningAnalyticsTracker

    private val songA = Song(
        id = "song_1",
        title = "Song One",
        artist = "Artist A",
        url = "https://youtube.com/watch?v=1",
        audioPath = "path/1",
        durationMs = 200000L
    )

    private val songB = Song(
        id = "song_2",
        title = "Song Two",
        artist = "Artist B",
        url = "https://youtube.com/watch?v=2",
        audioPath = "path/2",
        durationMs = 180000L
    )

    @Before
    fun setUp() {
        loggedEvents.clear()
        mockCurrentTimeMs = 1000L
        tracker = ListeningAnalyticsTracker(fakeAnalyticsManager).apply {
            timeProvider = { mockCurrentTimeMs }
        }
    }

    @Test
    fun fullPlayback_emitsSingleSongListenedEvent() {
        // 1. Media item transition to songA
        tracker.onMediaItemTransition(songA, 200000L)
        assertTrue(loggedEvents.isEmpty())

        // 2. Playback starts (Playing & READY)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // 3. Play for 200 seconds
        mockCurrentTimeMs += 200000L

        // 4. Song ends Naturally (STATE_ENDED)
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 200000L)
        tracker.onSongCompleted()

        // 5. Verify exactly 1 song_listened event logged
        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
        val event = listenedEvents[0]
        assertEquals("song_1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Song One", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("Artist A", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals(200L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(200L, event.params[AnalyticsConstants.Params.SONG_LENGTH])
        assertEquals(100, event.params[AnalyticsConstants.Params.COMPLETION_PERCENT])
    }

    @Test
    fun partialPlayback_skipsToNext_emitsSingleSongListenedEvent() {
        // 1. Transition to songA
        tracker.onMediaItemTransition(songA, 180000L)

        // 2. Start playing
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 180000L)

        // 3. Play for 45 seconds (25% of 180s)
        mockCurrentTimeMs += 45000L

        // 4. User skips to songB
        tracker.onMediaItemTransition(songB, 180000L)

        // 5. Verify exactly 1 event emitted for songA
        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
        val event = listenedEvents[0]
        assertEquals("song_1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals(45L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(180L, event.params[AnalyticsConstants.Params.SONG_LENGTH])
        assertEquals(25, event.params[AnalyticsConstants.Params.COMPLETION_PERCENT])
    }

    @Test
    fun pausedTime_isNotCounted() {
        tracker.onMediaItemTransition(songA, 100000L)

        // Play for 10s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        mockCurrentTimeMs += 10000L

        // Pause for 50s
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_READY, durationMs = 100000L)
        mockCurrentTimeMs += 50000L

        // Resume and play for 10s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        mockCurrentTimeMs += 10000L

        // Song ends
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 100000L)
        tracker.onSongCompleted()

        // Total listening duration must be 20s (10s + 10s)
        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
        val event = listenedEvents[0]
        assertEquals(20L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(100L, event.params[AnalyticsConstants.Params.SONG_LENGTH])
        assertEquals(20, event.params[AnalyticsConstants.Params.COMPLETION_PERCENT])
    }

    @Test
    fun bufferingTime_isNotCounted() {
        tracker.onMediaItemTransition(songA, 100000L)

        // Play for 15s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        mockCurrentTimeMs += 15000L

        // Enter STATE_BUFFERING for 30s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_BUFFERING, durationMs = 100000L)
        mockCurrentTimeMs += 30000L

        // Return to STATE_READY and play for 5s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        mockCurrentTimeMs += 5000L

        // Song ends
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 100000L)
        tracker.onSongCompleted()

        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
        val event = listenedEvents[0]
        assertEquals(20L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(20, event.params[AnalyticsConstants.Params.COMPLETION_PERCENT])
    }

    @Test
    fun songSwitching_logsPreviousSongBeforeTrackingNext() {
        // Song 1
        tracker.onMediaItemTransition(songA, 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        mockCurrentTimeMs += 30000L

        // Switch to Song 2
        tracker.onMediaItemTransition(songB, 120000L)
        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
        assertEquals("song_1", listenedEvents[0].params[AnalyticsConstants.Params.SONG_ID])

        // Play Song 2 for 20s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 120000L)
        mockCurrentTimeMs += 20000L

        // Switch away
        tracker.onMediaItemTransition(null, 0L)
        val listenedEventsAfter = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(2, listenedEventsAfter.size)
        assertEquals("song_2", listenedEventsAfter[1].params[AnalyticsConstants.Params.SONG_ID])
        assertEquals(20L, listenedEventsAfter[1].params[AnalyticsConstants.Params.DURATION_SECONDS])
    }

    @Test
    fun duplicateCallbacks_doNotEmitExtraEvents() {
        tracker.onMediaItemTransition(songA, 100000L)

        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)

        mockCurrentTimeMs += 10000L

        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 100000L)
        tracker.onSongCompleted()

        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
        assertEquals(10L, listenedEvents[0].params[AnalyticsConstants.Params.DURATION_SECONDS])
    }

    @Test
    fun milestones_fireExactlyOnceAtThresholds() {
        tracker.onMediaItemTransition(songA, 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)

        // 1. Advance 24%
        mockCurrentTimeMs += 24000L
        tracker.onProgressUpdate(24000L, 100000L)
        assertTrue(loggedEvents.isEmpty())

        // 2. Cross 25% threshold
        mockCurrentTimeMs += 1000L
        tracker.onProgressUpdate(25000L, 100000L)
        assertEquals(1, loggedEvents.size)
        assertEquals(AnalyticsConstants.Events.SONG_25_PERCENT, loggedEvents[0].name)
        assertEquals(25, loggedEvents[0].params[AnalyticsConstants.Params.COMPLETION_PERCENTAGE])

        // 3. Cross 50% threshold
        mockCurrentTimeMs += 25000L
        tracker.onProgressUpdate(50000L, 100000L)
        assertEquals(2, loggedEvents.size)
        assertEquals(AnalyticsConstants.Events.SONG_50_PERCENT, loggedEvents[1].name)

        // 4. Cross 75% threshold
        mockCurrentTimeMs += 25000L
        tracker.onProgressUpdate(75000L, 100000L)
        assertEquals(3, loggedEvents.size)
        assertEquals(AnalyticsConstants.Events.SONG_75_PERCENT, loggedEvents[2].name)

        // 5. Cross 100% threshold
        mockCurrentTimeMs += 25000L
        tracker.onProgressUpdate(100000L, 100000L)
        assertEquals(4, loggedEvents.size)
        assertEquals(AnalyticsConstants.Events.SONG_COMPLETED, loggedEvents[3].name)

        // 6. Natural completion callback. song_completed should not duplicate
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 100000L)
        tracker.onSongCompleted()
        val completedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_COMPLETED }
        assertEquals(1, completedEvents.size)
        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
    }

    @Test
    fun seeking_doesNotCauseDuplicateMilestones() {
        tracker.onMediaItemTransition(songA, 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)

        // Cross 25% threshold
        mockCurrentTimeMs += 30000L
        tracker.onProgressUpdate(30000L, 100000L)
        assertEquals(1, loggedEvents.size)
        assertEquals(AnalyticsConstants.Events.SONG_25_PERCENT, loggedEvents[0].name)

        // Seek backward to 10s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_BUFFERING, durationMs = 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)

        // Play back to 30s. 25% should NOT fire again.
        mockCurrentTimeMs += 20000L
        tracker.onProgressUpdate(30000L, 100000L)
        val thresholdEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_25_PERCENT }
        assertEquals(1, thresholdEvents.size)
    }

    @Test
    fun songCompleted_firesOnlyOnce() {
        tracker.onMediaItemTransition(songA, 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)

        // Play to 100s
        mockCurrentTimeMs += 100000L
        tracker.onProgressUpdate(100000L, 100000L)

        val completedCountBefore = loggedEvents.count { it.name == AnalyticsConstants.Events.SONG_COMPLETED }
        assertEquals(1, completedCountBefore)

        // Natural completion finishes the song
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 100000L)
        tracker.onSongCompleted()

        val completedCountAfter = loggedEvents.count { it.name == AnalyticsConstants.Events.SONG_COMPLETED }
        assertEquals(1, completedCountAfter)
    }

    @Test
    fun newSong_resetsThresholdState() {
        // Play songA to 30% -> fires 25% threshold
        tracker.onMediaItemTransition(songA, 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)
        mockCurrentTimeMs += 30000L
        tracker.onProgressUpdate(30000L, 100000L)

        assertEquals(1, loggedEvents.size)
        assertEquals(AnalyticsConstants.Events.SONG_25_PERCENT, loggedEvents[0].name)

        // Transition to songB
        tracker.onMediaItemTransition(songB, 100000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 100000L)

        // Play songB to 30% -> fires 25% threshold again
        mockCurrentTimeMs += 30000L
        tracker.onProgressUpdate(30000L, 100000L)

        val songB25PercentEvent = loggedEvents.firstOrNull {
            it.name == AnalyticsConstants.Events.SONG_25_PERCENT &&
            it.params[AnalyticsConstants.Params.SONG_ID] == "song_2"
        }
        org.junit.Assert.assertNotNull(songB25PercentEvent)
    }

    @Test
    fun skipToNext_logsSongSkippedWithNextButtonReason() {
        // Start songA
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Play for 10s
        mockCurrentTimeMs += 10000L
        tracker.onProgressUpdate(10000L, 200000L)

        // Skip to songB due to next button
        tracker.onMediaItemTransition(songB, 180000L, AnalyticsConstants.SkipReasons.NEXT_BUTTON)

        // Verify song_skipped event
        val skippedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_SKIPPED }
        assertEquals(1, skippedEvents.size)
        val event = skippedEvents[0]
        assertEquals("song_1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Song One", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("Artist A", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals(10L, event.params[AnalyticsConstants.Params.PLAYED_SECONDS])
        assertEquals(200L, event.params[AnalyticsConstants.Params.SONG_LENGTH])
        assertEquals(5, event.params[AnalyticsConstants.Params.COMPLETION_PERCENTAGE])
        assertEquals(AnalyticsConstants.SkipReasons.NEXT_BUTTON, event.params[AnalyticsConstants.Params.SKIP_REASON])
    }

    @Test
    fun skipToPrevious_logsSongSkippedWithPreviousButtonReason() {
        // Start songA
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Play for 20s
        mockCurrentTimeMs += 20000L
        tracker.onProgressUpdate(20000L, 200000L)

        // Skip to songB due to previous button
        tracker.onMediaItemTransition(songB, 180000L, AnalyticsConstants.SkipReasons.PREVIOUS_BUTTON)

        // Verify song_skipped event
        val skippedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_SKIPPED }
        assertEquals(1, skippedEvents.size)
        val event = skippedEvents[0]
        assertEquals("song_1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals(20L, event.params[AnalyticsConstants.Params.PLAYED_SECONDS])
        assertEquals(200L, event.params[AnalyticsConstants.Params.SONG_LENGTH])
        assertEquals(10, event.params[AnalyticsConstants.Params.COMPLETION_PERCENTAGE])
        assertEquals(AnalyticsConstants.SkipReasons.PREVIOUS_BUTTON, event.params[AnalyticsConstants.Params.SKIP_REASON])
    }

    @Test
    fun selectSong_logsSongSkippedWithSongSelectedReason() {
        // Start songA
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Play for 30s
        mockCurrentTimeMs += 30000L
        tracker.onProgressUpdate(30000L, 200000L)

        // Switch to songB without setting explicit skipReason (defaults to song_selected)
        tracker.onMediaItemTransition(songB, 180000L)

        // Verify song_skipped event
        val skippedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_SKIPPED }
        assertEquals(1, skippedEvents.size)
        val event = skippedEvents[0]
        assertEquals("song_1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals(30L, event.params[AnalyticsConstants.Params.PLAYED_SECONDS])
        assertEquals(200L, event.params[AnalyticsConstants.Params.SONG_LENGTH])
        assertEquals(15, event.params[AnalyticsConstants.Params.COMPLETION_PERCENTAGE])
        assertEquals(AnalyticsConstants.SkipReasons.SONG_SELECTED, event.params[AnalyticsConstants.Params.SKIP_REASON])
    }

    @Test
    fun queueChange_logsSongSkippedWithQueueChangeReason() {
        // Start songA
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Play for 40s
        mockCurrentTimeMs += 40000L
        tracker.onProgressUpdate(40000L, 200000L)

        // Switch to songB due to queue change
        tracker.onMediaItemTransition(songB, 180000L, AnalyticsConstants.SkipReasons.QUEUE_CHANGE)

        // Verify song_skipped event
        val skippedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_SKIPPED }
        assertEquals(1, skippedEvents.size)
        val event = skippedEvents[0]
        assertEquals("song_1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals(40L, event.params[AnalyticsConstants.Params.PLAYED_SECONDS])
        assertEquals(200L, event.params[AnalyticsConstants.Params.SONG_LENGTH])
        assertEquals(20, event.params[AnalyticsConstants.Params.COMPLETION_PERCENTAGE])
        assertEquals(AnalyticsConstants.SkipReasons.QUEUE_CHANGE, event.params[AnalyticsConstants.Params.SKIP_REASON])
    }

    @Test
    fun naturalCompletion_doesNotLogSongSkipped() {
        // Start songA
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Play to end
        mockCurrentTimeMs += 200000L
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 200000L)
        tracker.onSongCompleted()

        // Transition to songB (automatic advance)
        tracker.onMediaItemTransition(songB, 180000L)

        // Verify song_listened is logged, but song_skipped is NOT
        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)
        assertEquals("song_1", listenedEvents[0].params[AnalyticsConstants.Params.SONG_ID])

        val skippedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_SKIPPED }
        assertTrue(skippedEvents.isEmpty())
    }

    @Test
    fun sessionTracking_logsListeningSessionWithCorrectMetrics() {
        // 1. Session start / transition to songA
        tracker.onMediaItemTransition(songA, 200000L)
        // 2. Play songA for 50s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)
        mockCurrentTimeMs += 50000L
        tracker.onProgressUpdate(50000L, 200000L)

        // 3. Skip to songB via Next button
        tracker.onMediaItemTransition(songB, 180000L, AnalyticsConstants.SkipReasons.NEXT_BUTTON)

        // 4. Play songB for 100s
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 180000L)
        mockCurrentTimeMs += 100000L
        tracker.onProgressUpdate(100000L, 180000L)

        // 5. Complete songB naturally
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_ENDED, durationMs = 180000L)
        tracker.onSongCompleted()

        // 6. End session
        tracker.onSessionEnded()

        // Verify session event
        val sessionEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.LISTENING_SESSION }
        assertEquals(1, sessionEvents.size)
        val event = sessionEvents[0]
        assertEquals(150L, event.params[AnalyticsConstants.Params.SESSION_DURATION_SECONDS]) // 50s + 100s wall-clock
        assertEquals(150L, event.params[AnalyticsConstants.Params.TOTAL_LISTENING_SECONDS]) // 50s + 100s active listening
        assertEquals(2, event.params[AnalyticsConstants.Params.SONGS_PLAYED])
        assertEquals(1, event.params[AnalyticsConstants.Params.SONGS_COMPLETED])
        assertEquals(1, event.params[AnalyticsConstants.Params.SONGS_SKIPPED])
    }

    @Test
    fun sessionTracking_multipleEndTriggers_fireOnlyOneEvent() {
        // Start session & play songA
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)
        mockCurrentTimeMs += 10000L
        tracker.onProgressUpdate(10000L, 200000L)

        // Trigger session end multiple times (e.g. STATE_ENDED, release(), dismiss(), service destroy)
        tracker.onSessionEnded()
        tracker.onSessionEnded()
        tracker.onSessionEnded()

        // Verify exactly 1 listening_session event
        val sessionEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.LISTENING_SESSION }
        assertEquals(1, sessionEvents.size)
    }

    @Test
    fun automaticTransition_logsSongCompletedAndSongListened() {
        // Start songA
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Play for 200s
        mockCurrentTimeMs += 200000L
        tracker.onProgressUpdate(200000L, 200000L)

        // Transition to songB automatically
        tracker.onMediaItemTransition(songB, 180000L, transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)

        // Verify song_completed (100% threshold) and song_listened logged, and no skip event logged!
        val thresholdEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_COMPLETED }
        assertEquals(1, thresholdEvents.size)

        val listenedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_LISTENED }
        assertEquals(1, listenedEvents.size)

        val skippedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_SKIPPED }
        assertTrue(skippedEvents.isEmpty())
    }

    @Test
    fun multiSongAutoCompletion_logsCorrectSessionMetricsAndNoSkips() {
        // Song 1 starts with 0 duration initially
        tracker.onMediaItemTransition(songA, 0L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Song 1 progress
        mockCurrentTimeMs += 200000L
        tracker.onProgressUpdate(200000L, 200000L)

        // Song 1 -> Song 2 auto transition
        tracker.onMediaItemTransition(songB, 0L, transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 180000L)

        // Song 2 progress
        mockCurrentTimeMs += 180000L
        tracker.onProgressUpdate(180000L, 180000L)

        // Queue ends naturally
        tracker.onMediaItemTransition(null, 0L, transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)

        // Verify session event
        val sessionEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.LISTENING_SESSION }
        assertEquals(1, sessionEvents.size)
        val session = sessionEvents[0]
        assertEquals(2, session.params[AnalyticsConstants.Params.SONGS_PLAYED])
        assertEquals(2, session.params[AnalyticsConstants.Params.SONGS_COMPLETED])
        assertEquals(0, session.params[AnalyticsConstants.Params.SONGS_SKIPPED])

        // Verify no song_skipped events logged
        val skippedEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_SKIPPED }
        assertTrue(skippedEvents.isEmpty())
    }

    @Test
    fun automaticTransition_withPlayingTrue_tracksSongBProgressCorrectly() {
        // Song A transition and starts playing
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Song A progress: 200s (completed)
        mockCurrentTimeMs += 200000L
        tracker.onProgressUpdate(200000L, 200000L)

        // Song A -> Song B auto transition while playing is true
        tracker.onMediaItemTransition(
            newSong = songB,
            newDurationMs = 180000L,
            transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
            isPlaying = true
        )

        // Song B progress: 45s (which is 25% of 180s)
        mockCurrentTimeMs += 45000L
        tracker.onProgressUpdate(45000L, 180000L)

        // Verify that Song A completed and Song B logged a 25% threshold event
        val songACompleted = loggedEvents.any {
            it.name == AnalyticsConstants.Events.SONG_COMPLETED &&
            it.params[AnalyticsConstants.Params.SONG_ID] == "song_1"
        }
        assertTrue("Song A should have completed", songACompleted)

        val songB25Percent = loggedEvents.any {
            it.name == AnalyticsConstants.Events.SONG_25_PERCENT &&
            it.params[AnalyticsConstants.Params.SONG_ID] == "song_2"
        }
        assertTrue("Song B should have fired 25% milestone", songB25Percent)
    }

    @Test
    fun restoredSong_neverPlayed_doesNotEmitSkipOrListenedEvents() {
        // Restored song set on startup (paused state)
        tracker.onMediaItemTransition(songA, 200000L, isPlaying = false)

        // User starts playing song B directly without playing song A
        tracker.onMediaItemTransition(songB, 180000L, isPlaying = true)

        // Verify that song A emits neither song_skipped nor song_listened
        val songAEvents = loggedEvents.filter {
            it.params[AnalyticsConstants.Params.SONG_ID] == "song_1"
        }
        assertTrue("Restored song A that was never played should emit no events", songAEvents.isEmpty())
    }

    @Test
    fun onSessionEnded_finalizesActiveSong_andUpdatesSessionSummaryMetrics() {
        // Start song A and play to completion
        tracker.onMediaItemTransition(songA, 200000L, isPlaying = true)
        mockCurrentTimeMs += 200000L
        tracker.onProgressUpdate(200000L, 200000L)

        // Session ends naturally (e.g. queue completes)
        tracker.onSessionEnded()

        // Verify that listening_session event reports songs_completed = 1
        val sessionEvent = loggedEvents.firstOrNull { it.name == AnalyticsConstants.Events.LISTENING_SESSION }
        org.junit.Assert.assertNotNull("listening_session event should be logged", sessionEvent)
        assertEquals(1, sessionEvent!!.params[AnalyticsConstants.Params.SONGS_COMPLETED])
    }

    @Test
    fun playbackStarts_emitsSingleSongPlayEventFirst() {
        tracker.onMediaItemTransition(songA, 200000L)
        assertTrue(loggedEvents.none { it.name == AnalyticsConstants.Events.SONG_PLAY })

        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        val songPlayEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_PLAY }
        assertEquals(1, songPlayEvents.size)
        val playEvent = songPlayEvents[0]
        assertEquals("song_1", playEvent.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Song One", playEvent.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("Artist A", playEvent.params[AnalyticsConstants.Params.ARTIST])
        assertEquals(200L, playEvent.params[AnalyticsConstants.Params.DURATION_SECONDS])
    }

    @Test
    fun pauseAndResume_doesNotEmitDuplicateSongPlayEvent() {
        tracker.onMediaItemTransition(songA, 200000L)
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Pause
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_READY, durationMs = 200000L)

        // Resume
        tracker.onPlaybackStateChanged(isPlaying = true, playbackState = Player.STATE_READY, durationMs = 200000L)

        val songPlayEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_PLAY }
        assertEquals(1, songPlayEvents.size)
    }

    @Test
    fun restoredSongInPausedState_doesNotEmitSongPlayEvent() {
        tracker.onMediaItemTransition(songA, 200000L, isPlaying = false)
        tracker.onPlaybackStateChanged(isPlaying = false, playbackState = Player.STATE_READY, durationMs = 200000L)

        assertTrue(loggedEvents.none { it.name == AnalyticsConstants.Events.SONG_PLAY })
    }

    @Test
    fun autoNextTransition_emitsSongPlayEventWithAutoNextSource() {
        tracker.onMediaItemTransition(songA, 200000L, isPlaying = true)
        mockCurrentTimeMs += 200000L
        tracker.onProgressUpdate(200000L, 200000L)

        tracker.onMediaItemTransition(
            newSong = songB,
            newDurationMs = 180000L,
            transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
            isPlaying = true
        )

        val songPlayEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.SONG_PLAY }
        assertEquals(2, songPlayEvents.size)
        val songBPlayEvent = songPlayEvents[1]
        assertEquals("song_2", songBPlayEvent.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("AutoNext", songBPlayEvent.params[AnalyticsConstants.Params.PLAY_SOURCE])
    }
}
