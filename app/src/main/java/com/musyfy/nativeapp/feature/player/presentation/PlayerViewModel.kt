package com.musyfy.nativeapp.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.core.playback.PlaybackUiState
import com.musyfy.nativeapp.core.playback.PlayerManager
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val songRepository: SongRepository
) : ViewModel() {

    val playbackUiState: StateFlow<PlaybackUiState> = playerManager.playbackUiState

    val songs: StateFlow<List<Song>> = songRepository.getSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun playSong(song: Song) {
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
}
