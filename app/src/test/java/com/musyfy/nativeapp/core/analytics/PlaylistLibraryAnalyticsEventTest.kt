package com.musyfy.nativeapp.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistLibraryAnalyticsEventTest {

    @Test
    fun testPlaylistCreatedEvent() {
        val event = AnalyticsEvent.playlistCreated("p1", "My Playlist", 3, "Home")
        assertEquals(AnalyticsConstants.Events.PLAYLIST_CREATED, event.name)
        assertEquals("p1", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("My Playlist", event.params[AnalyticsConstants.Params.PLAYLIST_NAME])
        assertEquals(3, event.params[AnalyticsConstants.Params.INITIAL_SONG_COUNT])
        assertEquals("Home", event.params[AnalyticsConstants.Params.CREATION_SOURCE])
    }

    @Test
    fun testPlaylistDeletedEvent() {
        val event = AnalyticsEvent.playlistDeleted("p2", "Old Playlist", 10)
        assertEquals(AnalyticsConstants.Events.PLAYLIST_DELETED, event.name)
        assertEquals("p2", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Old Playlist", event.params[AnalyticsConstants.Params.PLAYLIST_NAME])
        assertEquals(10, event.params[AnalyticsConstants.Params.SONG_COUNT_BEFORE_DELETE])
    }

    @Test
    fun testPlaylistRenamedEvent() {
        val event = AnalyticsEvent.playlistRenamed("p3", "Name A", "Name B")
        assertEquals(AnalyticsConstants.Events.PLAYLIST_RENAMED, event.name)
        assertEquals("p3", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Name A", event.params[AnalyticsConstants.Params.OLD_NAME])
        assertEquals("Name B", event.params[AnalyticsConstants.Params.NEW_NAME])
    }

    @Test
    fun testPlaylistSongsAddedEvent() {
        val event = AnalyticsEvent.playlistSongsAdded(4, "Home")
        assertEquals(AnalyticsConstants.Events.PLAYLIST_SONGS_ADDED, event.name)
        assertEquals(4, event.params[AnalyticsConstants.Params.SONGS_COUNT])
        assertEquals("Home", event.params[AnalyticsConstants.Params.ADD_SOURCE])
    }

    @Test
    fun testPlaylistPlayStartedEvent() {
        val event = AnalyticsEvent.playlistPlayStarted(
            playlistId = "p4",
            playlistName = "Party",
            songCount = 15,
            songId = "s1",
            songTitle = "Track 1",
            artist = "Artist 1",
            positionIndex = 0,
            playSource = "Playlist",
            shuffleEnabled = true,
            repeatMode = "ALL"
        )
        assertEquals(AnalyticsConstants.Events.PLAYLIST_PLAY_STARTED, event.name)
        assertEquals("p4", event.params[AnalyticsConstants.Params.PLAYLIST_ID])
        assertEquals("Party", event.params[AnalyticsConstants.Params.PLAYLIST_NAME])
        assertEquals(15, event.params[AnalyticsConstants.Params.SONG_COUNT])
        assertEquals("s1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals(0, event.params[AnalyticsConstants.Params.POSITION_INDEX])
        assertEquals(true, event.params[AnalyticsConstants.Params.SHUFFLE_ENABLED])
        assertEquals("ALL", event.params[AnalyticsConstants.Params.REPEAT_MODE])
    }

    @Test
    fun testLikedSongEvent() {
        val event = AnalyticsEvent.likedSong("s1", "Song Title", "Artist", "Home", 180L, true)
        assertEquals(AnalyticsConstants.Events.LIKED_SONG, event.name)
        assertEquals("s1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Song Title", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("Artist", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals("Home", event.params[AnalyticsConstants.Params.SOURCE])
        assertEquals(180L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(true, event.params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING])
    }

    @Test
    fun testRemovedLikedSongEvent() {
        val event = AnalyticsEvent.removedLikedSong("s1", "Song Title", "Artist", "Liked", 180L, false)
        assertEquals(AnalyticsConstants.Events.REMOVED_LIKED_SONG, event.name)
        assertEquals("s1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Song Title", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("Artist", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals("Liked", event.params[AnalyticsConstants.Params.SOURCE])
        assertEquals(180L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(false, event.params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING])
    }

    @Test
    fun testSongDeletedEvent() {
        val event = AnalyticsEvent.songDeleted("s1", "Song Title", "Artist", "Liked", true, 2, 180L, false)
        assertEquals(AnalyticsConstants.Events.SONG_DELETED, event.name)
        assertEquals("s1", event.params[AnalyticsConstants.Params.SONG_ID])
        assertEquals("Song Title", event.params[AnalyticsConstants.Params.SONG_TITLE])
        assertEquals("Artist", event.params[AnalyticsConstants.Params.ARTIST])
        assertEquals("Liked", event.params[AnalyticsConstants.Params.DELETE_SOURCE])
        assertEquals(true, event.params[AnalyticsConstants.Params.WAS_LIKED])
        assertEquals(2, event.params[AnalyticsConstants.Params.PLAYLIST_COUNT])
        assertEquals(180L, event.params[AnalyticsConstants.Params.DURATION_SECONDS])
        assertEquals(false, event.params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING])
    }
}
