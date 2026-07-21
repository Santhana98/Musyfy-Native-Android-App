package com.musyfy.nativeapp.feature.player.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.core.analytics.AnalyticsConstants
import com.musyfy.nativeapp.core.analytics.ImportAnalyticsTracker
import com.musyfy.nativeapp.core.playback.PlaybackUiState
import com.musyfy.nativeapp.core.playback.PlayerManager
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.SongRepository
import com.musyfy.nativeapp.feature.download.data.SongDownloaderImpl
import com.musyfy.nativeapp.feature.download.data.YoutubeMetadataExtractor
import com.musyfy.nativeapp.feature.download.domain.SongDownloader
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val songRepository: SongRepository,
    private val songDownloader: SongDownloader,
    private val importAnalyticsTracker: ImportAnalyticsTracker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val playbackUiState: StateFlow<PlaybackUiState> = playerManager.playbackUiState

    val songs: StateFlow<List<Song>> = songRepository.getSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = songDownloader.downloadStatuses

    fun playSong(song: Song) {
        android.util.Log.d("MusyfyPlayback", "PlayerViewModel: Song selected: ID=${song.id}, Title=${song.title}, audioPath=${song.audioPath}, artworkPath=${song.artworkPath}, remoteUrl=${song.url}")
        playerManager.playSong(song)
    }

    fun play() {
        playerManager.play()
    }

    fun dismissPlaybackSession() {
        playerManager.dismissPlaybackSession()
    }

    fun pause() {
        playerManager.pause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun setQueue(songs: List<Song>) {
        playerManager.setQueue(songs)
    }

    fun addToQueue(song: Song) {
        playerManager.addToQueue(song)
    }

    fun playNext(song: Song) {
        playerManager.playNext(song)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        playerManager.reorderQueue(fromIndex, toIndex)
    }

    fun clearQueue() {
        playerManager.clearQueue()
    }

    fun removeFromQueue(songId: String) {
        playerManager.removeFromQueue(songId)
    }

    fun setShuffleModeEnabled(enabled: Boolean) {
        playerManager.setShuffleModeEnabled(enabled)
    }

    fun setRepeatMode(repeatMode: Int) {
        playerManager.setRepeatMode(repeatMode)
    }

    fun skipToNext() {
        viewModelScope.launch {
            try {
                val songsList = songs.value.ifEmpty { songRepository.getSongs().first() }
                val currentSong = playbackUiState.value.currentSong ?: return@launch
                val currentIndex = songsList.indexOfFirst { it.id == currentSong.id }
                if (currentIndex != -1 && currentIndex < songsList.size - 1) {
                    playSong(songsList[currentIndex + 1])
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            try {
                val songsList = songs.value.ifEmpty { songRepository.getSongs().first() }
                val currentSong = playbackUiState.value.currentSong ?: return@launch
                val currentIndex = songsList.indexOfFirst { it.id == currentSong.id }
                if (currentIndex != -1 && currentIndex > 0) {
                    playSong(songsList[currentIndex - 1])
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    // Downloader Interface controls
    // Architectural Note: For Phase 9.5.3 only, PlayerViewModel owns the integration with ImportAnalyticsTracker.
    // This is an intentional architectural decision to minimize changes and may be moved closer to the downloader layer in a future refactor.
    fun startDownload(song: Song) {
        viewModelScope.launch {
            val job = launch {
                songDownloader.downloadSong(song).collect { status ->
                    when (status) {
                        is DownloadStatus.Downloading -> {
                            // Log download_started when the actual download begins (first progress update)
                            importAnalyticsTracker.trackDownloadStarted(song.id)
                        }
                        is DownloadStatus.Downloaded -> {
                            // Log download completed lifecycle event
                            importAnalyticsTracker.trackDownloadCompleted(song.id)

                            // Log import_completed only after the song has been successfully persisted to the local library,
                            // not merely when the download finishes.
                            val persistedSong = songRepository.getSongs().first().find { it.id == song.id }
                            if (persistedSong != null && persistedSong.audioPath != null) {
                                importAnalyticsTracker.trackImportCompleted(song.id)
                            } else {
                                importAnalyticsTracker.trackImportFailed(song.id, AnalyticsConstants.FailureReasons.STORAGE_ERROR)
                            }
                        }
                        is DownloadStatus.Error -> {
                            // Log download failed lifecycle event
                            val normalizedDownloadReason = normalizeDownloadErrorForDownload(status.message)
                            importAnalyticsTracker.trackDownloadFailed(song.id, normalizedDownloadReason)

                            // Log import failed lifecycle event
                            val normalizedReason = normalizeDownloadError(status.message)
                            importAnalyticsTracker.trackImportFailed(song.id, normalizedReason)
                        }
                        else -> {}
                    }
                }
            }
            (songDownloader as? SongDownloaderImpl)?.registerJob(song.id, job)
            job.invokeOnCompletion {
                (songDownloader as? SongDownloaderImpl)?.clearJob(song.id)
            }
        }
    }

    private fun normalizeDownloadError(message: String?): String {
        val msg = message?.lowercase() ?: return AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        return when {
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") || msg.contains("host") -> 
                AnalyticsConstants.FailureReasons.NETWORK_ERROR
            msg.contains("rename") || msg.contains("permission") || msg.contains("space") || msg.contains("storage") || msg.contains("save") || msg.contains("file") -> 
                AnalyticsConstants.FailureReasons.STORAGE_ERROR
            msg.contains("download") || msg.contains("exit code") || msg.contains("youtube-dl") || msg.contains("server returned code") -> 
                AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR
            else -> 
                AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        }
    }

    private fun normalizeDownloadErrorForDownload(message: String?): String {
        val msg = message?.lowercase() ?: return AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        return when {
            msg.contains("permission") || msg.contains("denied") -> 
                AnalyticsConstants.FailureReasons.PERMISSION_ERROR
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") || msg.contains("host") -> 
                AnalyticsConstants.FailureReasons.NETWORK_ERROR
            msg.contains("rename") || msg.contains("space") || msg.contains("storage") || msg.contains("save") || msg.contains("file") -> 
                AnalyticsConstants.FailureReasons.STORAGE_ERROR
            msg.contains("download") || msg.contains("exit code") || msg.contains("youtube-dl") || msg.contains("server returned code") -> 
                AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR
            else -> 
                AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        }
    }

    fun pauseDownload(songId: String) {
        songDownloader.pauseDownload(songId)
    }

    fun cancelDownload(songId: String) {
        // Log import_cancelled and download_cancelled ONLY for explicit user-initiated cancellations.
        importAnalyticsTracker.trackDownloadCancelled(songId)
        importAnalyticsTracker.trackImportCancelled(songId)
        songDownloader.cancelDownload(songId)
    }

    fun deleteDownloadedSong(songId: String) {
        songDownloader.deleteDownloadedSong(songId)
    }

    fun deleteSong(songId: String) {
        viewModelScope.launch {
            // Delete from repository
            songRepository.deleteSong(songId)
            // Delete downloaded files if they exist
            songDownloader.deleteDownloadedSong(songId)
            // Remove from player queue if present
            playerManager.removeFromQueue(songId)
        }
    }

    fun toggleLikeSong(song: Song) {
        viewModelScope.launch {
            val updated = song.copy(liked = !song.liked)
            songRepository.addSong(updated)
        }
    }

    fun restoreSong(song: Song) {
        viewModelScope.launch {
            songRepository.addSong(song)
        }
    }

    // YouTube Import Pipeline
    fun importYoutubeSong(url: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoId = YoutubeMetadataExtractor.extractVideoId(url)
            if (videoId == null) {
                viewModelScope.launch(Dispatchers.Main) {
                    onError("Invalid YouTube URL — please check and try again")
                }
                return@launch
            }

            // Prevent duplicate entries in the library and reuse cached content
            val exists = songs.value.any { it.id == videoId }
            if (exists) {
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess()
                }
                return@launch
            }

            // Log import started when the import pipeline actually begins
            importAnalyticsTracker.trackImportStarted(videoId)

            // Extract metadata from YouTube
            val info = YoutubeMetadataExtractor.fetchVideoInfo(context, url)
            if (info == null) {
                importAnalyticsTracker.trackImportFailed(videoId, AnalyticsConstants.FailureReasons.METADATA_ERROR)
                viewModelScope.launch(Dispatchers.Main) {
                    onError("Failed to extract metadata. Check connection and try again.")
                }
                return@launch
            }

            // Create song entry using extracted metadata
            val newSong = Song(
                id = info.id,
                title = info.title,
                artist = info.uploader ?: "Unknown Artist",
                url = url,
                imageUrl = info.thumbnail,
                durationMs = (info.duration ?: 0) * 1000L
            )
            
            songRepository.addSong(newSong)
            
            // Trigger background download (m4a stream + artwork thumbnail) automatically
            startDownload(newSong)

            viewModelScope.launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
