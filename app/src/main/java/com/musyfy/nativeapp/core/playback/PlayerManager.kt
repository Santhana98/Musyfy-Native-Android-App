package com.musyfy.nativeapp.core.playback

import com.musyfy.nativeapp.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerManager {
    val playbackUiState: StateFlow<PlaybackUiState>

    fun playSong(song: Song)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
}
