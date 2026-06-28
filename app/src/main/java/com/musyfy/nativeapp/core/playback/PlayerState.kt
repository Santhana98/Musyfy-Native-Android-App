package com.musyfy.nativeapp.core.playback

import com.musyfy.nativeapp.domain.model.Song

data class PlaybackUiState(
    val currentSong: Song? = null,
    val state: PlayerState = PlayerState.IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null
)

enum class PlayerState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    BUFFERING,
    COMPLETED,
    ERROR
}
