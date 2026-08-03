package com.musyfy.nativeapp.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LibraryAnalyticsTrackerTest {

    private val loggedEvents = mutableListOf<AnalyticsEvent>()

    private val fakeAnalyticsManager = object : AnalyticsManager {
        override fun logEvent(event: AnalyticsEvent) {
            loggedEvents.add(event)
        }
        override fun logScreenView(screenName: String, className: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private val fakePlaybackSourceProvider = object : PlaybackSourceProvider {
        override fun getCurrentSource(): String = "Liked"
        override fun getCurrentPlaySource(): String = "Liked"
    }

    private val tracker = LibraryAnalyticsTracker(fakeAnalyticsManager, fakePlaybackSourceProvider)

    @Before
    fun setUp() {
        loggedEvents.clear()
    }

    @Test
    fun testTrackLikedSong_logsEventWithSourceAndMetadata() {
        tracker.trackLikedSong(
            songId = "s123",
            songTitle = "Starboy",
            artist = "The Weeknd",
            source = "Home",
            songDurationSeconds = 230L,
            isCurrentlyPlaying = true
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.LIKED_SONG, event.name)
        assertEquals("s123", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Starboy", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("The Weeknd", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals("Home", event.params[AnalyticsConstants.Params.SOURCE])
        assertEquals(230L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(true, event.params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING])
    }

    @Test
    fun testTrackRemovedLikedSong_logsEventWithSource() {
        tracker.trackRemovedLikedSong(
            songId = "s123",
            songTitle = "Starboy",
            artist = "The Weeknd",
            source = "Liked",
            songDurationSeconds = 230L,
            isCurrentlyPlaying = false
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.REMOVED_LIKED_SONG, event.name)
        assertEquals("s123", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Starboy", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("The Weeknd", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals("Liked", event.params[AnalyticsConstants.Params.SOURCE])
        assertEquals(230L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(false, event.params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING])
    }

    @Test
    fun testTrackSongDeleted_logsEventWithDeleteSourceWasLikedAndPlaylistCount() {
        tracker.trackSongDeleted(
            songId = "s999",
            songTitle = "Blinding Lights",
            artist = "The Weeknd",
            deleteSource = "Search",
            wasLiked = true,
            playlistCount = 3,
            songDurationSeconds = 200L,
            isCurrentlyPlaying = false
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.SONG_DELETED, event.name)
        assertEquals("s999", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Blinding Lights", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("The Weeknd", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals("Search", event.params[AnalyticsConstants.Params.DELETE_SOURCE])
        assertEquals(true, event.params[AnalyticsConstants.Params.WAS_LIKED])
        assertEquals(3, event.params[AnalyticsConstants.Params.PLAYLIST_COUNT])
        assertEquals(200L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(false, event.params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING])
    }
}
