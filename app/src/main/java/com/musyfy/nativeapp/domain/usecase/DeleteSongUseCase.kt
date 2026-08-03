package com.musyfy.nativeapp.domain.usecase

import com.musyfy.nativeapp.core.analytics.LibraryAnalyticsTracker
import com.musyfy.nativeapp.core.playback.PlayerManager
import com.musyfy.nativeapp.domain.repository.PlaylistRepository
import com.musyfy.nativeapp.domain.repository.SongRepository
import com.musyfy.nativeapp.feature.download.domain.SongDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain Use Case acting as central transaction coordinator for song deletion workflows.
 * Coordinates multi-step operations (song DB deletion, playlist reference cleanup, storage purging,
 * playback queue eviction, download cancellation, and analytics dispatching) while keeping repositories decoupled.
 */
@Singleton
class DeleteSongUseCase @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val songDownloader: SongDownloader,
    private val playerManager: PlayerManager,
    private val libraryAnalyticsTracker: LibraryAnalyticsTracker
) {

    suspend operator fun invoke(songId: String, deleteSource: String? = null) = withContext(Dispatchers.IO) {
        if (songId.isBlank()) return@withContext

        val song = songRepository.getSongById(songId).first() ?: return@withContext

        // Query playlist membership count and playback state before executing deletion
        val playlists = playlistRepository.getPlaylists().first()
        val playlistCount = playlists.count { it.songIds.contains(song.id) }
        val isCurrentlyPlaying = playerManager.playbackUiState.value.currentSong?.id == song.id
        val songDurationSeconds = if (song.durationMs > 0) song.durationMs / 1000L else null

        // 1. Cancel any active or queued download job & purge local files
        songDownloader.deleteDownloadedSong(song.id)

        // 2. Remove song from active ExoPlayer playback queue
        playerManager.removeFromQueue(song.id)

        // 3. Purge song ID from all playlist entities in Room DB
        playlistRepository.removeSongFromAllPlaylists(song.id)

        // 4. Delete song record from Room DB songs table
        songRepository.deleteSong(song.id)

        // 5. Log analytics only after deletion workflow completes successfully
        libraryAnalyticsTracker.trackSongDeleted(
            songId = song.id,
            songTitle = song.title,
            artist = song.artist ?: "Unknown Artist",
            deleteSource = deleteSource,
            wasLiked = song.liked,
            playlistCount = playlistCount,
            songDurationSeconds = songDurationSeconds,
            isCurrentlyPlaying = isCurrentlyPlaying
        )
    }
}
