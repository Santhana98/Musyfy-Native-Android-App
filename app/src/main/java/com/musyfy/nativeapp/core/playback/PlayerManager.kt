package com.musyfy.nativeapp.core.playback

import com.musyfy.nativeapp.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerManager {
    val playbackUiState: StateFlow<PlaybackUiState>

    fun playSong(song: Song)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setQueue(songs: List<Song>)
    fun addToQueue(song: Song)
    fun playNext(song: Song)
    fun reorderQueue(fromIndex: Int, toIndex: Int)
    fun clearQueue()
    fun removeFromQueue(songId: String)
    fun setShuffleModeEnabled(enabled: Boolean)
    fun setRepeatMode(repeatMode: Int)
    fun release()
}
