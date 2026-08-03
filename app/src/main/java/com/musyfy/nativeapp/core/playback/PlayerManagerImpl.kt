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
import com.musyfy.nativeapp.core.analytics.PlaybackAnalyticsTracker
import com.musyfy.nativeapp.core.analytics.ListeningAnalyticsTracker
import com.musyfy.nativeapp.core.analytics.AnalyticsConstants
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
    @param:ApplicationContext private val context: Context,
    private val playbackAnalyticsTracker: PlaybackAnalyticsTracker,
    private val listeningAnalyticsTracker: ListeningAnalyticsTracker
) : PlayerManager {

    companion object {
        private const val PREFS_NAME = "musyfy_playback_prefs"
        private const val KEY_LAST_SONG_ID = "last_song_id"
        private const val KEY_LAST_POSITION = "last_position"
        private const val KEY_PLAYBACK_SESSION_ACTIVE = "playback_session_active"
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _playbackUiState = MutableStateFlow(
        PlaybackUiState(
            playbackSessionActive = sharedPrefs.getBoolean(KEY_PLAYBACK_SESSION_ACTIVE, true)
        )
    )
    override val playbackUiState: StateFlow<PlaybackUiState> = _playbackUiState.asStateFlow()

    private var progressJob: Job? = null
    private var pendingPlaySongReason: String? = null

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
            listeningAnalyticsTracker.onPlaybackStateChanged(
                isPlaying = isPlaying,
                playbackState = playbackState,
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
            )
            val currentSong = _playbackUiState.value.currentSong
            if (playbackState == Player.STATE_ENDED && currentSong != null) {
                listeningAnalyticsTracker.onSessionEnded()
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
                    state = if (isPlaying) PlayerState.PLAYING else mapExoPlayerState(exoPlayer.playbackState, isPlaying),
                    playbackSessionActive = if (isPlaying) true else it.playbackSessionActive
                )
            }
            listeningAnalyticsTracker.onPlaybackStateChanged(
                isPlaying = isPlaying,
                playbackState = exoPlayer.playbackState,
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
            )
            val currentSong = _playbackUiState.value.currentSong
            if (currentSong != null) {
                val playbackMode = when {
                    exoPlayer.shuffleModeEnabled -> "shuffle"
                    exoPlayer.repeatMode == Player.REPEAT_MODE_ONE -> "repeat_one"
                    exoPlayer.repeatMode == Player.REPEAT_MODE_ALL -> "repeat_all"
                    else -> "standard"
                }
                playbackAnalyticsTracker.trackIsPlayingChanged(
                    isPlayingNow = isPlaying,
                    song = currentSong,
                    durationMs = exoPlayer.duration.coerceAtLeast(0L),
                    currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                    playbackMode = playbackMode,
                    playbackState = exoPlayer.playbackState
                )
            }
            if (isPlaying) {
                saveSessionActiveState(true)
                startProgressUpdate()
                startService()
            } else {
                stopProgressUpdate()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val errorCode = error.errorCode
            val category = when (errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "NETWORK_ERROR"
                
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "STORAGE_ERROR"

                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                PlaybackException.ERROR_CODE_DECODING_FAILED -> "DECODER_ERROR"

                else -> "GENERAL_PLAYBACK_ERROR"
            }

            android.util.Log.e("MusyfyPlayback", "ExoPlayer Listener: onPlayerError: category=$category, errorCode=$errorCode, msg=${error.message}", error)

            val currentQueue = _playbackUiState.value.queue
            val currentSong = _playbackUiState.value.currentSong
            val currentIndex = if (currentSong != null) currentQueue.indexOfFirst { it.id == currentSong.id } else -1

            // Context-Aware Recovery: If error is unplayable track and next song exists, auto-advance!
            if ((category == "STORAGE_ERROR" || category == "DECODER_ERROR") && currentIndex != -1 && currentIndex < currentQueue.size - 1) {
                android.util.Log.w("MusyfyPlayback", "onPlayerError: Context-aware recovery advancing to next track.")
                val nextSong = currentQueue[currentIndex + 1]
                playSong(nextSong, currentQueue)
            } else {
                _playbackUiState.update {
                    it.copy(
                        state = PlayerState.ERROR,
                        isPlaying = false,
                        errorMessage = "Playback error ($category): ${error.localizedMessage ?: "Unknown"}"
                    )
                }
                stopProgressUpdate()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val isPlaying = exoPlayer.isPlaying && exoPlayer.playbackState == Player.STATE_READY
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRANSITION] PlayerManager: onMediaItemTransition called. MediaItem ID=${mediaItem?.mediaId}, reason=$reason, playerIsPlaying=$isPlaying")
            
            val oldSong = _playbackUiState.value.currentSong
            val oldPositionMs = _playbackUiState.value.currentPositionMs
            val oldDurationMs = _playbackUiState.value.durationMs
            val playbackMode = when {
                exoPlayer.shuffleModeEnabled -> "shuffle"
                exoPlayer.repeatMode == Player.REPEAT_MODE_ONE -> "repeat_one"
                exoPlayer.repeatMode == Player.REPEAT_MODE_ALL -> "repeat_all"
                else -> "standard"
            }

            if (mediaItem == null) {
                if (oldSong != null && reason != Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    val skipReason = pendingPlaySongReason ?: AnalyticsConstants.SkipReasons.QUEUE_CHANGE
                    pendingPlaySongReason = null
                    listeningAnalyticsTracker.setPendingSkipReason(skipReason)
                }
                android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRANSITION] PlayerManager: mediaItem is null, finalizing previous song")
                listeningAnalyticsTracker.onMediaItemTransition(
                    newSong = null,
                    newDurationMs = 0L,
                    transitionReason = reason,
                    isPlaying = isPlaying
                )
                _playbackUiState.update {
                    it.copy(
                        currentSong = null,
                        currentPositionMs = 0L,
                        durationMs = 0L
                    )
                }
                playbackAnalyticsTracker.trackMediaItemTransition(
                    newSong = null,
                    reason = reason,
                    oldSong = oldSong,
                    oldPositionMs = oldPositionMs,
                    oldDurationMs = oldDurationMs,
                    playbackMode = playbackMode
                )
            } else {
                val currentQueue = _playbackUiState.value.queue
                val song = currentQueue.find { it.id == mediaItem.mediaId } ?: SongMapper.toSong(mediaItem)
                val newIndex = currentQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRANSITION] PlayerManager: Resolved active Song synchronously: ID=${song.id}, Title=${song.title}, isPlaying=$isPlaying")
                
                if (oldSong != null && song.id != oldSong.id) {
                    val skipReason = when {
                        pendingPlaySongReason != null -> {
                            val r = pendingPlaySongReason
                            pendingPlaySongReason = null
                            r
                        }
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                            null
                        }
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                            val oldIndex = currentQueue.indexOfFirst { it.id == oldSong.id }
                            if (oldIndex != -1) {
                                if (newIndex > oldIndex || (oldIndex == currentQueue.size - 1 && newIndex == 0)) {
                                    AnalyticsConstants.SkipReasons.NEXT_BUTTON
                                } else {
                                    AnalyticsConstants.SkipReasons.PREVIOUS_BUTTON
                                }
                            } else {
                                AnalyticsConstants.SkipReasons.NEXT_BUTTON
                            }
                        }
                        reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
                            AnalyticsConstants.SkipReasons.QUEUE_CHANGE
                        }
                        else -> {
                            AnalyticsConstants.SkipReasons.SONG_SELECTED
                        }
                    }
                    if (skipReason != null) {
                        listeningAnalyticsTracker.setPendingSkipReason(skipReason)
                    }
                }
                pendingPlaySongReason = null

                _playbackUiState.update {
                    it.copy(
                        currentSong = song,
                        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    )
                }
                playbackAnalyticsTracker.trackMediaItemTransition(
                    newSong = song,
                    newIndex = newIndex,
                    reason = reason,
                    oldSong = oldSong,
                    oldPositionMs = oldPositionMs,
                    oldDurationMs = oldDurationMs,
                    playbackMode = playbackMode
                )
                listeningAnalyticsTracker.onMediaItemTransition(
                    newSong = song,
                    newDurationMs = exoPlayer.duration.coerceAtLeast(0L),
                    transitionReason = reason,
                    isPlaying = isPlaying
                )
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playbackUiState.update {
                it.copy(shuffleModeEnabled = shuffleModeEnabled)
            }
            playbackAnalyticsTracker.trackShuffleToggled(shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _playbackUiState.update {
                it.copy(repeatMode = repeatMode)
            }
            playbackAnalyticsTracker.trackRepeatToggled(repeatMode)
        }
    }

    init {
        coroutineScope.launch {
            exoPlayer.addListener(listener)
            
            // Restore last played song and progress position
            val lastSongId = sharedPrefs.getString(KEY_LAST_SONG_ID, null)
            val lastPosition = sharedPrefs.getLong(KEY_LAST_POSITION, 0L)
            val sessionActive = sharedPrefs.getBoolean(KEY_PLAYBACK_SESSION_ACTIVE, true)
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
                                isPlaying = false,
                                queue = allSongs,
                                playbackSessionActive = sessionActive
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

    override fun playSong(song: Song, customQueue: List<Song>?) {
        coroutineScope.launch {
            android.util.Log.d("MusyfyPlayback", "PlayerManager: playSong requested for song ID=${song.id}")
            
            val oldSong = _playbackUiState.value.currentSong
            val currentQueue = _playbackUiState.value.queue
            if (oldSong != null && currentQueue.isNotEmpty()) {
                val currentIndex = currentQueue.indexOfFirst { it.id == oldSong.id }
                val targetIndex = currentQueue.indexOfFirst { it.id == song.id }
                if (currentIndex != -1 && targetIndex != -1) {
                    if (targetIndex == currentIndex + 1 || (currentIndex == currentQueue.size - 1 && targetIndex == 0)) {
                        playbackAnalyticsTracker.notifyManualSkipRequested(isNext = true)
                        pendingPlaySongReason = AnalyticsConstants.SkipReasons.NEXT_BUTTON
                    } else if (targetIndex == currentIndex - 1 || (currentIndex == 0 && targetIndex == currentQueue.size - 1)) {
                        playbackAnalyticsTracker.notifyManualSkipRequested(isNext = false)
                        pendingPlaySongReason = AnalyticsConstants.SkipReasons.PREVIOUS_BUTTON
                    } else {
                        playbackAnalyticsTracker.notifyNewPlaybackSessionRequested()
                        pendingPlaySongReason = AnalyticsConstants.SkipReasons.SONG_SELECTED
                    }
                } else {
                    playbackAnalyticsTracker.notifyNewPlaybackSessionRequested()
                    pendingPlaySongReason = AnalyticsConstants.SkipReasons.SONG_SELECTED
                }
            } else {
                playbackAnalyticsTracker.notifyNewPlaybackSessionRequested()
                pendingPlaySongReason = null
            }

            // Determine active playback queue (custom queue e.g. Playlist/Liked or fallback to Library)
            val activeQueue = if (!customQueue.isNullOrEmpty()) {
                customQueue
            } else {
                songRepository.getSongs().first()
            }

            saveSessionActiveState(true)
            _playbackUiState.update {
                it.copy(
                    currentSong = song,
                    state = PlayerState.LOADING,
                    isPlaying = false,
                    errorMessage = null,
                    queue = activeQueue,
                    playbackSessionActive = true
                )
            }
            
            val mediaItems = activeQueue.map { s ->
                val mediaItem = SongMapper.toMediaItem(s, context)
                android.util.Log.d("MusyfyPlayback", "PlayerManager: Mapped song ID=${s.id} to URI=${mediaItem.localConfiguration?.uri}")
                mediaItem
            }
            val index = activeQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            
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
            if (exoPlayer.isPlaying) return@launch
            exoPlayer.play()
            startService()
        }
    }

    override fun pause() {
        coroutineScope.launch {
            if (!exoPlayer.isPlaying && exoPlayer.playbackState != Player.STATE_BUFFERING) return@launch
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

    override fun setQueue(songs: List<Song>) {
        coroutineScope.launch {
            if (songs.isEmpty()) {
                clearQueue()
                return@launch
            }
            val currentSong = _playbackUiState.value.currentSong
            val mediaItems = songs.map { s -> SongMapper.toMediaItem(s, context) }
            val existingIndex = if (currentSong != null) songs.indexOfFirst { it.id == currentSong.id } else -1

            if (existingIndex != -1) {
                val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                exoPlayer.setMediaItems(mediaItems, existingIndex, currentPos)
                _playbackUiState.update {
                    it.copy(queue = songs)
                }
            } else {
                exoPlayer.setMediaItems(mediaItems)
                _playbackUiState.update {
                    it.copy(queue = songs)
                }
            }
        }
    }

    override fun addToQueue(song: Song) {
        coroutineScope.launch {
            val currentQueue = _playbackUiState.value.queue
            if (currentQueue.isEmpty()) {
                playSong(song)
                return@launch
            }
            if (currentQueue.none { it.id == song.id }) {
                val updatedQueue = currentQueue + song
                val mediaItem = SongMapper.toMediaItem(song, context)
                exoPlayer.addMediaItem(mediaItem)
                _playbackUiState.update {
                    it.copy(queue = updatedQueue)
                }
            }
        }
    }

    override fun playNext(song: Song) {
        coroutineScope.launch {
            val currentQueue = _playbackUiState.value.queue.toMutableList()
            if (currentQueue.isEmpty()) {
                playSong(song)
                return@launch
            }

            val mediaItem = SongMapper.toMediaItem(song, context)
            val existingIndex = currentQueue.indexOfFirst { it.id == song.id }
            if (existingIndex != -1) {
                currentQueue.removeAt(existingIndex)
                exoPlayer.removeMediaItem(existingIndex)
            }

            val rawIndex = exoPlayer.currentMediaItemIndex
            val currentIndex = if (rawIndex == -1) 0 else rawIndex
            val insertIndex = (currentIndex + 1).coerceIn(0, currentQueue.size)

            currentQueue.add(insertIndex, song)
            exoPlayer.addMediaItem(insertIndex, mediaItem)

            _playbackUiState.update {
                it.copy(queue = currentQueue)
            }
        }
    }

    override fun reorderQueue(fromIndex: Int, toIndex: Int) {
        coroutineScope.launch {
            val currentQueue = _playbackUiState.value.queue.toMutableList()
            if (currentQueue.size <= 1) return@launch

            val safeFromIndex = fromIndex.coerceIn(0, currentQueue.size - 1)
            val safeToIndex = toIndex.coerceIn(0, currentQueue.size - 1)
            if (safeFromIndex == safeToIndex) return@launch

            val song = currentQueue.removeAt(safeFromIndex)
            currentQueue.add(safeToIndex, song)
            exoPlayer.moveMediaItem(safeFromIndex, safeToIndex)
            _playbackUiState.update {
                it.copy(queue = currentQueue)
            }
        }
    }

    override fun clearQueue() {
        coroutineScope.launch {
            exoPlayer.clearMediaItems()
            _playbackUiState.update {
                it.copy(
                    queue = emptyList(),
                    currentSong = null,
                    currentPositionMs = 0L,
                    durationMs = 0L,
                    isPlaying = false,
                    state = PlayerState.IDLE
                )
            }
        }
    }

    override fun removeFromQueue(songId: String) {
        coroutineScope.launch {
            val currentQueue = _playbackUiState.value.queue
            if (currentQueue.isEmpty()) return@launch

            val index = currentQueue.indexOfFirst { it.id == songId }
            if (index == -1) return@launch

            val updatedQueue = currentQueue.filter { it.id != songId }
            val isCurrentPlayingSong = (_playbackUiState.value.currentSong?.id == songId)

            if (isCurrentPlayingSong) {
                if (updatedQueue.isEmpty()) {
                    clearQueue()
                } else {
                    val nextIndex = index.coerceAtMost(updatedQueue.size - 1)
                    val nextSong = updatedQueue[nextIndex]
                    exoPlayer.removeMediaItem(index)
                    playSong(nextSong, updatedQueue)
                }
            } else {
                exoPlayer.removeMediaItem(index)
                _playbackUiState.update {
                    it.copy(queue = updatedQueue)
                }
            }
        }
    }

    override fun setShuffleModeEnabled(enabled: Boolean) {
        coroutineScope.launch {
            exoPlayer.shuffleModeEnabled = enabled
            _playbackUiState.update {
                it.copy(shuffleModeEnabled = enabled)
            }
        }
    }

    override fun setRepeatMode(repeatMode: Int) {
        coroutineScope.launch {
            exoPlayer.repeatMode = repeatMode
            _playbackUiState.update {
                it.copy(repeatMode = repeatMode)
            }
        }
    }

    override fun release() {
        coroutineScope.launch {
            listeningAnalyticsTracker.onSessionEnded()
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
                listeningAnalyticsTracker.onProgressUpdate(currentPos, duration)
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
            .putString(KEY_LAST_SONG_ID, songId)
            .putLong(KEY_LAST_POSITION, position)
            .apply()
    }

    private fun saveSessionActiveState(active: Boolean) {
        sharedPrefs.edit()
            .putBoolean(KEY_PLAYBACK_SESSION_ACTIVE, active)
            .apply()
    }

    override fun dismissPlaybackSession() {
        coroutineScope.launch {
            listeningAnalyticsTracker.onSessionEnded()
            if (!_playbackUiState.value.isPlaying) {
                saveSessionActiveState(false)
                _playbackUiState.update {
                    it.copy(playbackSessionActive = false)
                }
                try {
                    val intent = Intent(context, PlayerService::class.java)
                    context.stopService(intent)
                } catch (e: Exception) {
                    // Safe fallback
                }
            }
        }
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
