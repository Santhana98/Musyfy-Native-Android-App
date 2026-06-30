package com.musyfy.nativeapp.feature.player.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    fun startDownload(song: Song) {
        viewModelScope.launch {
            val job = launch {
                songDownloader.downloadSong(song).collect { }
            }
            (songDownloader as? SongDownloaderImpl)?.registerJob(song.id, job)
            job.invokeOnCompletion {
                (songDownloader as? SongDownloaderImpl)?.clearJob(song.id)
            }
        }
    }

    fun pauseDownload(songId: String) {
        songDownloader.pauseDownload(songId)
    }

    fun cancelDownload(songId: String) {
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

            // Extract metadata from YouTube
            val info = YoutubeMetadataExtractor.fetchVideoInfo(context, url)
            if (info == null) {
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
