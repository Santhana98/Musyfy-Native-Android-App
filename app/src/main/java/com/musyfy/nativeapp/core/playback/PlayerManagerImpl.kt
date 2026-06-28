package com.musyfy.nativeapp.core.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musyfy.nativeapp.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManagerImpl @Inject constructor(
    private val exoPlayer: ExoPlayer
) : PlayerManager {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _playbackUiState = MutableStateFlow(PlaybackUiState())
    override val playbackUiState: StateFlow<PlaybackUiState> = _playbackUiState.asStateFlow()

    private var progressJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val isPlaying = exoPlayer.isPlaying
            _playbackUiState.update {
                it.copy(
                    isPlaying = isPlaying,
                    state = mapExoPlayerState(playbackState, isPlaying),
                    durationMs = exoPlayer.duration.coerceAtLeast(0L),
                    currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                )
            }
            if (isPlaying) {
                startProgressUpdate()
            } else {
                stopProgressUpdate()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackUiState.update {
                it.copy(
                    isPlaying = isPlaying,
                    state = if (isPlaying) PlayerState.PLAYING else mapExoPlayerState(exoPlayer.playbackState, isPlaying)
                )
            }
            if (isPlaying) {
                startProgressUpdate()
            } else {
                stopProgressUpdate()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackUiState.update {
                it.copy(
                    state = PlayerState.ERROR,
                    isPlaying = false,
                    errorMessage = error.localizedMessage ?: "Playback error"
                )
            }
            stopProgressUpdate()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (mediaItem == null) {
                _playbackUiState.update {
                    it.copy(
                        currentSong = null,
                        currentPositionMs = 0L,
                        durationMs = 0L
                    )
                }
            }
        }
    }

    init {
        coroutineScope.launch {
            exoPlayer.addListener(listener)
        }
    }

    override fun playSong(song: Song) {
        coroutineScope.launch {
            _playbackUiState.update {
                it.copy(
                    currentSong = song,
                    state = PlayerState.LOADING,
                    isPlaying = false,
                    errorMessage = null
                )
            }
            val mediaItem = SongMapper.toMediaItem(song)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    override fun play() {
        coroutineScope.launch {
            exoPlayer.play()
        }
    }

    override fun pause() {
        coroutineScope.launch {
            exoPlayer.pause()
        }
    }

    override fun seekTo(positionMs: Long) {
        coroutineScope.launch {
            exoPlayer.seekTo(positionMs)
            _playbackUiState.update {
                it.copy(currentPositionMs = positionMs)
            }
        }
    }

    override fun release() {
        coroutineScope.launch {
            stopProgressUpdate()
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (true) {
                val currentPos = exoPlayer.currentPosition
                val duration = exoPlayer.duration.coerceAtLeast(0L)
                _playbackUiState.update {
                    it.copy(
                        currentPositionMs = currentPos,
                        durationMs = duration
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun mapExoPlayerState(playbackState: Int, isPlaying: Boolean): PlayerState {
        return when (playbackState) {
            Player.STATE_IDLE -> PlayerState.IDLE
            Player.STATE_BUFFERING -> PlayerState.BUFFERING
            Player.STATE_READY -> {
                if (isPlaying) PlayerState.PLAYING else PlayerState.PAUSED
            }
            Player.STATE_ENDED -> PlayerState.COMPLETED
            else -> PlayerState.IDLE
        }
    }
}
