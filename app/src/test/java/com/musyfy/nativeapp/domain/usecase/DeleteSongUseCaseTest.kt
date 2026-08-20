package com.musyfy.nativeapp.domain.usecase

import com.musyfy.nativeapp.core.analytics.AnalyticsEvent
import com.musyfy.nativeapp.core.analytics.AnalyticsManager
import com.musyfy.nativeapp.core.analytics.LibraryAnalyticsTracker
import com.musyfy.nativeapp.core.analytics.PlaybackSourceProvider
import com.musyfy.nativeapp.core.playback.PlaybackUiState
import com.musyfy.nativeapp.core.playback.PlayerManager
import com.musyfy.nativeapp.domain.model.Playlist
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.PlaylistRepository
import com.musyfy.nativeapp.domain.repository.SongRepository
import com.musyfy.nativeapp.feature.download.domain.SongDownloader
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteSongUseCaseTest {

    private val sampleSong = Song(
        id = "s1",
        title = "Test Song",
        artist = "Test Artist",
        url = "https://youtube.com/watch?v=123",
        durationMs = 180000L,
        liked = true
    )

    private val samplePlaylist = Playlist(
        id = "p1",
        name = "Favorites",
        songIds = listOf("s1", "s2"),
        createdAt = System.currentTimeMillis()
    )

    private val songs = mutableListOf(sampleSong)
    private val playlists = mutableListOf(samplePlaylist)
    private val deletedDownloads = mutableListOf<String>()
    private val removedQueueSongIds = mutableListOf<String>()
    private val loggedEvents = mutableListOf<AnalyticsEvent>()

    private val fakeSongRepository = object : SongRepository {
        override fun getSongs(): Flow<List<Song>> = flowOf(songs)
        override fun getSongById(id: String): Flow<Song?> = flowOf(songs.find { it.id == id })
        override suspend fun addSong(song: Song) { songs.add(song) }
        override suspend fun deleteSong(songId: String) { songs.removeAll { it.id == songId } }
        override suspend fun deleteSongs(songIds: List<String>) {
            val set = songIds.toSet()
            songs.removeAll { it.id in set }
        }
    }

    private val fakePlaylistRepository = object : PlaylistRepository {
        override fun getPlaylists(): Flow<List<Playlist>> = flowOf(playlists)
        override fun getPlaylistById(id: String): Flow<Playlist?> = flowOf(playlists.find { it.id == id })
        override suspend fun createPlaylist(name: String): Playlist = samplePlaylist
        override suspend fun renamePlaylist(id: String, newName: String) {}
        override suspend fun deletePlaylist(id: String) {}
        override suspend fun addSongToPlaylist(playlistId: String, songId: String) {}
        override suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>): Int = songIds.size
        override suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {}
        override suspend fun removeSongFromAllPlaylists(songId: String) {
            val updated = playlists.map { p -> p.copy(songIds = p.songIds.filter { it != songId }) }
            playlists.clear()
            playlists.addAll(updated)
        }
        override suspend fun removeSongsFromAllPlaylists(songIds: List<String>) {
            val set = songIds.toSet()
            val updated = playlists.map { p -> p.copy(songIds = p.songIds.filter { it !in set }) }
            playlists.clear()
            playlists.addAll(updated)
        }
        override suspend fun reorderPlaylistSongs(playlistId: String, fromIndex: Int, toIndex: Int) {}
    }

    private val fakeSongDownloader = object : SongDownloader {
        override val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = MutableStateFlow(emptyMap())
        override fun getDownloadStatus(songId: String): Flow<DownloadStatus> = flowOf(DownloadStatus.NotDownloaded)
        override fun downloadSong(song: Song): Flow<DownloadStatus> = flowOf(DownloadStatus.NotDownloaded)
        override fun pauseDownload(songId: String) {}
        override fun cancelDownload(songId: String) {}
        override fun deleteDownloadedSong(songId: String): Boolean {
            deletedDownloads.add(songId)
            return true
        }
    }

    private val fakePlayerManager = object : PlayerManager {
        override val playbackUiState: StateFlow<PlaybackUiState> = MutableStateFlow(PlaybackUiState())
        override fun playSong(song: Song, customQueue: List<Song>?) {}
        override fun play() {}
        override fun pause() {}
        override fun seekTo(positionMs: Long) {}
        override fun setQueue(songs: List<Song>) {}
        override fun addToQueue(song: Song) {}
        override fun playNext(song: Song) {}
        override fun reorderQueue(fromIndex: Int, toIndex: Int) {}
        override fun clearQueue() {}
        override fun removeFromQueue(songId: String) { removedQueueSongIds.add(songId) }
        override fun setShuffleModeEnabled(enabled: Boolean) {}
        override fun setRepeatMode(repeatMode: Int) {}
        override fun dismissPlaybackSession() {}
        override fun release() {}
    }

    private val fakeAnalyticsManager = object : AnalyticsManager {
        override fun logEvent(event: AnalyticsEvent) { loggedEvents.add(event) }
        override fun logScreenView(screenName: String, className: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private val fakeSourceProvider = object : PlaybackSourceProvider {
        override fun getCurrentSource(): String = "Liked"
        override fun getCurrentPlaySource(): String = "Liked"
    }

    private val libraryAnalyticsTracker = LibraryAnalyticsTracker(fakeAnalyticsManager, fakeSourceProvider)

    private val deleteSongUseCase = DeleteSongUseCase(
        songRepository = fakeSongRepository,
        playlistRepository = fakePlaylistRepository,
        songDownloader = fakeSongDownloader,
        playerManager = fakePlayerManager,
        libraryAnalyticsTracker = libraryAnalyticsTracker
    )

    @Before
    fun setUp() {
        songs.clear()
        songs.add(sampleSong)
        playlists.clear()
        playlists.add(samplePlaylist)
        deletedDownloads.clear()
        removedQueueSongIds.clear()
        loggedEvents.clear()
    }

    @Test
    fun testInvoke_deletesSongOrchestratesAllLayersAndLogsAnalytics() = runTest {
        deleteSongUseCase("s1", deleteSource = "Liked")

        // 1. Storage purged
        assertTrue(deletedDownloads.contains("s1"))

        // 2. Playback queue updated
        assertTrue(removedQueueSongIds.contains("s1"))

        // 3. Playlist references purged
        assertEquals(listOf("s2"), playlists[0].songIds)

        // 4. Song record deleted from song repository
        assertTrue(songs.isEmpty())

        // 5. Analytics logged only after successful deletion
        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals("song_deleted", event.name)
        assertEquals("s1", event.params["song_id"])
        assertEquals(true, event.params["was_liked"])
        assertEquals(1, event.params["playlist_count"])
    }

    @Test
    fun testInvoke_nonExistentSongId_returnsEarlyWithoutAnalytics() = runTest {
        deleteSongUseCase("non_existent_id", deleteSource = "Home")

        assertTrue(loggedEvents.isEmpty())
        assertTrue(deletedDownloads.isEmpty())
        assertTrue(removedQueueSongIds.isEmpty())
    }

    @Test
    fun testDeleteSongs_batchDeletesMultipleSongsAndLogsBatchAnalytics() = runTest {
        val secondSong = Song(id = "s2", title = "Second Song", artist = "Second Artist", url = "https://youtube.com/watch?v=456")
        songs.add(secondSong)

        deleteSongUseCase.deleteSongs(listOf("s1", "s2"), deleteSource = "Liked")

        // 1. Storage purged for both
        assertTrue(deletedDownloads.contains("s1"))
        assertTrue(deletedDownloads.contains("s2"))

        // 2. Playback queue updated for both
        assertTrue(removedQueueSongIds.contains("s1"))
        assertTrue(removedQueueSongIds.contains("s2"))

        // 3. Playlist references purged
        assertTrue(playlists[0].songIds.isEmpty())

        // 4. Song records deleted
        assertTrue(songs.isEmpty())

        // 5. One batch analytics event logged
        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals("song_deleted_batch", event.name)
        assertEquals(2, event.params["songs_count"])
        assertEquals("Liked", event.params["delete_source"])
    }
}
