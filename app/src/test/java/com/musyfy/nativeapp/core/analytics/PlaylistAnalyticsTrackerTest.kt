package com.musyfy.nativeapp.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaylistAnalyticsTrackerTest {

    private val loggedEvents = mutableListOf<AnalyticsEvent>()

    private val fakeAnalyticsManager = object : AnalyticsManager {
        override fun logEvent(event: AnalyticsEvent) {
            loggedEvents.add(event)
        }
        override fun logScreenView(screenName: String, className: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private val fakePlaybackSourceProvider = object : PlaybackSourceProvider {
        override fun getCurrentSource(): String = "Playlist"
        override fun getCurrentPlaySource(): String = "Playlist"
    }

    private val tracker = PlaylistAnalyticsTracker(fakeAnalyticsManager, fakePlaybackSourceProvider)

    @Before
    fun setUp() {
        loggedEvents.clear()
    }

    @Test
    fun testTrackPlaylistCreated_logsEventWithParameters() {
        tracker.trackPlaylistCreated(
            playlistId = "p1",
            playlistName = "Chill Vibes",
            initialSongCount = 5,
            creationSource = "Home"
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYLIST_CREATED, event.name)
        assertEquals("p1", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Chill Vibes", event.params[AnalyticsConstants.Params.PLAYLIST_NAME])
        assertEquals(5, event.params[AnalyticsConstants.Params.INITIAL_SONG_COUNT])
        assertEquals("Home", event.params[AnalyticsConstants.Params.CREATION_SOURCE])
    }

    @Test
    fun testTrackPlaylistDeleted_logsEventWithParameters() {
        tracker.trackPlaylistDeleted(
            playlistId = "p1",
            playlistName = "Roadtrip",
            songCountBeforeDelete = 12
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYLIST_DELETED, event.name)
        assertEquals("p1", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Roadtrip", event.params[AnalyticsConstants.Params.PLAYLIST_NAME])
        assertEquals(12, event.params[AnalyticsConstants.Params.SONG_COUNT_BEFORE_DELETE])
    }

    @Test
    fun testTrackPlaylistRenamed_sameNameIgnored() {
        tracker.trackPlaylistRenamed("p1", "Rock", "Rock")
        assertTrue(loggedEvents.isEmpty())
    }

    @Test
    fun testTrackPlaylistRenamed_differentNameLogsEvent() {
        tracker.trackPlaylistRenamed("p1", "Old Name", "New Name")
        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYLIST_RENAMED, event.name)
        assertEquals("p1", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Old Name", event.params[AnalyticsConstants.Params.OLD_NAME])
        assertEquals("New Name", event.params[AnalyticsConstants.Params.NEW_NAME])
    }

    @Test
    fun testTrackPlaylistPlayStarted_logsEventWithFullMetadata() {
        tracker.trackPlaylistPlayStarted(
            playlistId = "p100",
            playlistName = "Party Hits",
            songCount = 20,
            songId = "s42",
            songTitle = "Song Title",
            artist = "Artist Name",
            positionIndex = 3,
            playSource = "Playlist",
            shuffleEnabled = true,
            repeatMode = "ALL"
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYLIST_PLAY_STARTED, event.name)
        assertEquals("p100", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Party Hits", event.params[AnalyticsConstants.Params.PLAYLIST_NAME])
        assertEquals(20, event.params[AnalyticsConstants.Params.SONG_COUNT])
        assertEquals("s42", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Song Title", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("Artist Name", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals(3, event.params[AnalyticsConstants.Params.POSITION_INDEX])
        assertEquals("Playlist", event.params[AnalyticsConstants.Params.PLAY_SOURCE])
        assertEquals(true, event.params[AnalyticsConstants.Params.SHUFFLE_ENABLED])
        assertEquals("ALL", event.params[AnalyticsConstants.Params.REPEAT_MODE])
    }

    @Test
    fun testTrackPlaylistSongAdded_logsEvent() {
        tracker.trackPlaylistSongAdded(
            playlistId = "p1",
            playlistName = "Favorites",
            songId = "s1",
            songTitle = "Title",
            artist = "Artist",
            currentPlaylistSongCount = 10,
            addSource = "Home"
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYLIST_SONG_ADDED, event.name)
        assertEquals("p1", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Title", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals(10, event.params[AnalyticsConstants.Params.CURRENT_PLAYLIST_SONG_COUNT])
    }

    @Test
    fun testTrackPlaylistSongRemoved_logsEvent() {
        tracker.trackPlaylistSongRemoved(
            playlistId = "p1",
            playlistName = "Favorites",
            songId = "s1",
            songTitle = "Title",
            artist = "Artist",
            currentPlaylistSongCount = 9
        )

        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYLIST_SONG_REMOVED, event.name)
        assertEquals("p1", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals(9, event.params[AnalyticsConstants.Params.CURRENT_PLAYLIST_SONG_COUNT])
    }

    @Test
    fun testTrackPlaylistReordered_sameIndexIgnored() {
        tracker.trackPlaylistReordered("p1", "Mix", 2, 2, 10)
        assertTrue(loggedEvents.isEmpty())
    }

    @Test
    fun testTrackPlaylistReordered_differentIndexLogsEvent() {
        tracker.trackPlaylistReordered("p1", "Mix", 0, 3, 10)
        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYLIST_REORDERED, event.name)
        assertEquals("p1", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals(0, event.params[AnalyticsConstants.Params.FROM_INDEX])
        assertEquals(3, event.params[AnalyticsConstants.Params.TO_INDEX])
        assertEquals(10, event.params[AnalyticsConstants.Params.SONG_COUNT])
    }
}
