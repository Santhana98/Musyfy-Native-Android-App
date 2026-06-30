package com.musyfy.nativeapp.core.playback

import android.content.Context
import android.content.Intent
import java.io.File
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManagerImpl @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val songRepository: SongRepository,
    @param:ApplicationContext private val context: Context
) : PlayerManager {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _playbackUiState = MutableStateFlow(PlaybackUiState())
    override val playbackUiState: StateFlow<PlaybackUiState> = _playbackUiState.asStateFlow()

    private var progressJob: Job? = null
    
    private val sharedPrefs = context.getSharedPreferences("musyfy_playback_prefs", Context.MODE_PRIVATE)

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val isPlaying = exoPlayer.isPlaying
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "STATE_IDLE"
                Player.STATE_BUFFERING -> "STATE_BUFFERING"
                Player.STATE_READY -> "STATE_READY"
                Player.STATE_ENDED -> "STATE_ENDED"
                else -> "UNKNOWN"
            }
            android.util.Log.d("MusyfyPlayback", "ExoPlayer Listener: onPlaybackStateChanged: state=$stateName, isPlaying=$isPlaying, durationMs=${exoPlayer.duration}, currentPositionMs=${exoPlayer.currentPosition}")
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
            android.util.Log.d("MusyfyPlayback", "ExoPlayer Listener: onIsPlayingChanged: isPlaying=$isPlaying")
            _playbackUiState.update {
                it.copy(
                    isPlaying = isPlaying,
                    state = if (isPlaying) PlayerState.PLAYING else mapExoPlayerState(exoPlayer.playbackState, isPlaying)
                )
            }
            if (isPlaying) {
                startProgressUpdate()
                startService()
            } else {
                stopProgressUpdate()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            android.util.Log.e("MusyfyPlayback", "ExoPlayer Listener: onPlayerError: errorCode=${error.errorCode}, errorMessage=${error.message}", error)
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
            android.util.Log.d("MusyfyPlayback", "PlayerManager: onMediaItemTransition: MediaItem ID=${mediaItem?.mediaId}, URI=${mediaItem?.localConfiguration?.uri}")
            if (mediaItem == null) {
                _playbackUiState.update {
                    it.copy(
                        currentSong = null,
                        currentPositionMs = 0L,
                        durationMs = 0L
                    )
                }
            } else {
                coroutineScope.launch {
                    val allSongs = songRepository.getSongs().first()
                    val song = allSongs.find { it.id == mediaItem.mediaId } ?: SongMapper.toSong(mediaItem)
                    android.util.Log.d("MusyfyPlayback", "PlayerManager: Resolved active Song: ID=${song.id}, Title=${song.title}, audioPath=${song.audioPath}")
                    _playbackUiState.update {
                        it.copy(
                            currentSong = song,
                            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                            durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        )
                    }
                }
            }
        }
    }

    init {
        coroutineScope.launch {
            exoPlayer.addListener(listener)
            
            // Restore last played song and progress position
            val lastSongId = sharedPrefs.getString("last_song_id", null)
            val lastPosition = sharedPrefs.getLong("last_position", 0L)
            if (lastSongId != null) {
                try {
                    val allSongs = songRepository.getSongs().first()
                    val song = allSongs.find { it.id == lastSongId }
                    if (song != null) {
                        val mediaItems = allSongs.map { s ->
                            val mediaItem = SongMapper.toMediaItem(s, context)
                            android.util.Log.d("MusyfyPlayback", "PlayerManager: Restore - Mapped ID=${s.id} to URI=${mediaItem.localConfiguration?.uri}")
                            mediaItem
                        }
                        val index = allSongs.indexOf(song).coerceAtLeast(0)
                        
                        _playbackUiState.update {
                            it.copy(
                                currentSong = song,
                                currentPositionMs = lastPosition,
                                state = PlayerState.PAUSED,
                                isPlaying = false
                            )
                        }
                        
                        android.util.Log.d("MusyfyPlayback", "PlayerManager: Restore - Calling setMediaItems(items, index=$index, position=$lastPosition)")
                        exoPlayer.setMediaItems(mediaItems, index, lastPosition)
                        android.util.Log.d("MusyfyPlayback", "PlayerManager: Restore - Calling prepare()")
                        exoPlayer.prepare()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MusyfyPlayback", "PlayerManager: Restore failed", e)
                }
            }
        }
    }

    override fun playSong(song: Song) {
        coroutineScope.launch {
            android.util.Log.d("MusyfyPlayback", "PlayerManager: playSong requested for song ID=${song.id}")
            _playbackUiState.update {
                it.copy(
                    currentSong = song,
                    state = PlayerState.LOADING,
                    isPlaying = false,
                    errorMessage = null
                )
            }
            
            // Load all songs, checking for local files first (m4a/mp3/artwork)
            val allSongs = songRepository.getSongs().first()
            val mediaItems = allSongs.map { s ->
                val mediaItem = SongMapper.toMediaItem(s, context)
                android.util.Log.d("MusyfyPlayback", "PlayerManager: Mapped song ID=${s.id} to URI=${mediaItem.localConfiguration?.uri}")
                mediaItem
            }
            val index = allSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            
            android.util.Log.d("MusyfyPlayback", "PlayerManager: Calling setMediaItems(items, index=$index, position=0)")
            exoPlayer.setMediaItems(mediaItems, index, 0L)
            android.util.Log.d("MusyfyPlayback", "PlayerManager: Calling prepare()")
            exoPlayer.prepare()
            android.util.Log.d("MusyfyPlayback", "PlayerManager: Calling play()")
            exoPlayer.play()
            startService()
        }
    }

    override fun play() {
        coroutineScope.launch {
            exoPlayer.play()
            startService()
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
            _playbackUiState.value.currentSong?.let { song ->
                savePlaybackState(song.id, positionMs)
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

    private fun startService() {
        try {
            val intent = Intent(context, PlayerService::class.java)
            context.startService(intent)
        } catch (e: Exception) {
            // Safe fallback
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
                
                // Caches the progress details periodically
                _playbackUiState.value.currentSong?.let { song ->
                    savePlaybackState(song.id, currentPos)
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun savePlaybackState(songId: String, position: Long) {
        sharedPrefs.edit()
            .putString("last_song_id", songId)
            .putLong("last_position", position)
            .apply()
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
