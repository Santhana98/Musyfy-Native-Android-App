package com.musyfy.nativeapp.core.analytics

import androidx.media3.common.Player
import com.musyfy.nativeapp.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks playback lifecycle events and dispatches them to AnalyticsManager.
 * Employs a state machine to ensure duplicate events from repeated ExoPlayer callbacks are discarded.
 */
@Singleton
class PlaybackAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    private val playbackSourceProvider: PlaybackSourceProvider
) {
    private enum class TrackerPlaybackState {
        IDLE,
        PLAYING,
        PAUSED,
        COMPLETED
    }

    private var currentSongId: String? = null
    private var currentMediaItemIndex: Int = -1
    private var trackerState = TrackerPlaybackState.IDLE
    private var playStartTimestamp: Long = 0L
    private var accumulatedListenedDurationMs: Long = 0L

    // Explicit manual skip signals
    private var pendingManualSkipNext = false
    private var pendingManualSkipPrevious = false

    /**
     * Informs the tracker that a manual skip next or previous action has been explicitly requested.
     */
    @Synchronized
    fun notifyManualSkipRequested(isNext: Boolean) {
        if (isNext) {
            pendingManualSkipNext = true
            pendingManualSkipPrevious = false
        } else {
            pendingManualSkipPrevious = true
            pendingManualSkipNext = false
        }
    }

    /**
     * Informs the tracker that a new playback session has been requested (e.g., from direct clicks).
     */
    @Synchronized
    fun notifyNewPlaybackSessionRequested() {
        pendingManualSkipNext = false
        pendingManualSkipPrevious = false
    }

    /**
     * Resets tracking state for a new song transition and logs skip events if applicable.
     */
    @Synchronized
    fun trackMediaItemTransition(
        newSong: Song?,
        newIndex: Int = -1,
        reason: Int = -1,
        oldSong: Song? = null,
        oldPositionMs: Long = 0L,
        oldDurationMs: Long = 0L,
        playbackMode: String = "standard"
    ) {
        val newSongId = newSong?.id
        if (newSongId != currentSongId) {
            // Check if it's a skip transition
            if (oldSong != null) {
                when {
                    pendingManualSkipNext -> {
                        logSongSkipNext(oldSong, oldPositionMs, oldDurationMs, playbackMode)
                    }
                    pendingManualSkipPrevious -> {
                        logSongSkipPrevious(oldSong, oldPositionMs, oldDurationMs, playbackMode)
                    }
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                        // MediaSession/Lockscreen/Headset skip seeking
                        if (currentMediaItemIndex != -1) {
                            if (newIndex > currentMediaItemIndex) {
                                logSongSkipNext(oldSong, oldPositionMs, oldDurationMs, playbackMode)
                            } else if (newIndex < currentMediaItemIndex) {
                                logSongSkipPrevious(oldSong, oldPositionMs, oldDurationMs, playbackMode)
                            }
                        }
                    }
                }
            }

            // Reset pending manual flags
            pendingManualSkipNext = false
            pendingManualSkipPrevious = false

            // If previous song was playing, accumulate its final listened duration before resetting
            if (trackerState == TrackerPlaybackState.PLAYING && currentSongId != null) {
                accumulateListenedDuration()
            }
            currentSongId = newSongId
            currentMediaItemIndex = newIndex
            trackerState = TrackerPlaybackState.IDLE
            accumulatedListenedDurationMs = 0L
            playStartTimestamp = 0L
        } else {
            // Index might update even if song ID is same (e.g. reordering active item)
            currentMediaItemIndex = newIndex
        }
    }

    /**
     * Responds to changes in player's play/pause status.
     */
    @Synchronized
    fun trackIsPlayingChanged(
        isPlayingNow: Boolean,
        song: Song,
        durationMs: Long,
        currentPositionMs: Long,
        playbackMode: String,
        playbackState: Int
    ) {
        // Safety alignment check to ensure tracker is pointing to the correct song
        if (song.id != currentSongId) {
            trackMediaItemTransition(song)
        }

        if (isPlayingNow) {
            when (trackerState) {
                TrackerPlaybackState.IDLE -> {
                    trackerState = TrackerPlaybackState.PLAYING
                    playStartTimestamp = System.currentTimeMillis()
                    logSongPlay(song, durationMs, playbackMode)
                }
                TrackerPlaybackState.PAUSED -> {
                    trackerState = TrackerPlaybackState.PLAYING
                    playStartTimestamp = System.currentTimeMillis()
                    logSongResume(song, durationMs, currentPositionMs, playbackMode)
                }
                TrackerPlaybackState.COMPLETED -> {
                    // Track is played again after completion
                    trackerState = TrackerPlaybackState.PLAYING
                    accumulatedListenedDurationMs = 0L
                    playStartTimestamp = System.currentTimeMillis()
                    logSongPlay(song, durationMs, playbackMode)
                }
                TrackerPlaybackState.PLAYING -> {
                    // Already in playing state, discard redundant callback
                }
            }
        } else {
            if (trackerState == TrackerPlaybackState.PLAYING) {
                accumulateListenedDuration()
                // Prevent logging song_pause when the track naturally ended or failed/stopped
                if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
                    trackerState = TrackerPlaybackState.PAUSED
                    logSongPause(song, durationMs, currentPositionMs, playbackMode)
                }
            }
        }
    }

    /**
     * Responds to natural completion of a song.
     */
    @Synchronized
    fun trackPlaybackCompleted(song: Song, durationMs: Long) {
        if (song.id != currentSongId) {
            trackMediaItemTransition(song)
        }

        if (trackerState != TrackerPlaybackState.COMPLETED) {
            if (trackerState == TrackerPlaybackState.PLAYING) {
                accumulateListenedDuration()
            }
            trackerState = TrackerPlaybackState.COMPLETED
            logSongComplete(song, durationMs)
        }
    }

    /**
     * Responds to shuffle toggle changes.
     */
    @Synchronized
    fun trackShuffleToggled(shuffleEnabled: Boolean) {
        val params = mapOf<String, Any>(
            AnalyticsConstants.Params.SHUFFLE_ENABLED to shuffleEnabled
        )
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.SHUFFLE_TOGGLE, params))
    }

    /**
     * Responds to repeat mode configuration changes.
     */
    @Synchronized
    fun trackRepeatToggled(repeatModeInt: Int) {
        val repeatModeStr = when (repeatModeInt) {
            Player.REPEAT_MODE_OFF -> "off"
            Player.REPEAT_MODE_ONE -> "one"
            Player.REPEAT_MODE_ALL -> "all"
            else -> "off"
        }
        val params = mapOf<String, Any>(
            AnalyticsConstants.Params.REPEAT_MODE to repeatModeStr
        )
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.REPEAT_TOGGLE, params))
    }

    private fun accumulateListenedDuration() {
        if (playStartTimestamp > 0) {
            val delta = System.currentTimeMillis() - playStartTimestamp
            if (delta > 0) {
                accumulatedListenedDurationMs += delta
            }
            playStartTimestamp = 0L
        }
    }

    private fun calculateCompletionPercentage(durationMs: Long): Double {
        if (durationMs <= 0) return 0.0
        val percentage = (accumulatedListenedDurationMs.toDouble() / durationMs.toDouble()) * 100.0
        return percentage.coerceIn(0.0, 100.0)
    }

    private fun logSongPlay(song: Song, durationMs: Long, playbackMode: String) {
        val params = mutableMapOf<String, Any>(
            AnalyticsConstants.Params.SONG_ID to song.id,
            AnalyticsConstants.Params.SONG_TITLE to song.title,
            AnalyticsConstants.Params.ARTIST to (song.artist ?: "Unknown Artist"),
            AnalyticsConstants.Params.DURATION_MS to durationMs,
            AnalyticsConstants.Params.SOURCE to playbackSourceProvider.getCurrentSource(),
            AnalyticsConstants.Params.PLAYBACK_MODE to playbackMode
        )
        song.imageUrl?.let { params[AnalyticsConstants.Params.ALBUM] = it }
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.SONG_PLAY, params))
    }

    private fun logSongResume(song: Song, durationMs: Long, currentPositionMs: Long, playbackMode: String) {
        val params = mutableMapOf<String, Any>(
            AnalyticsConstants.Params.SONG_ID to song.id,
            AnalyticsConstants.Params.SONG_TITLE to song.title,
            AnalyticsConstants.Params.ARTIST to (song.artist ?: "Unknown Artist"),
            AnalyticsConstants.Params.DURATION_MS to durationMs,
            AnalyticsConstants.Params.CURRENT_POSITION_MS to currentPositionMs,
            AnalyticsConstants.Params.PLAYBACK_MODE to playbackMode
        )
        song.imageUrl?.let { params[AnalyticsConstants.Params.ALBUM] = it }
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.SONG_RESUME, params))
    }

    private fun logSongPause(song: Song, durationMs: Long, currentPositionMs: Long, playbackMode: String) {
        val completionPercentage = calculateCompletionPercentage(durationMs)
        val params = mutableMapOf<String, Any>(
            AnalyticsConstants.Params.SONG_ID to song.id,
            AnalyticsConstants.Params.SONG_TITLE to song.title,
            AnalyticsConstants.Params.ARTIST to (song.artist ?: "Unknown Artist"),
            AnalyticsConstants.Params.DURATION_MS to durationMs,
            AnalyticsConstants.Params.CURRENT_POSITION_MS to currentPositionMs,
            AnalyticsConstants.Params.LISTENED_DURATION_MS to accumulatedListenedDurationMs,
            AnalyticsConstants.Params.COMPLETION_PERCENTAGE to completionPercentage,
            AnalyticsConstants.Params.PLAYBACK_MODE to playbackMode
        )
        song.imageUrl?.let { params[AnalyticsConstants.Params.ALBUM] = it }
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.SONG_PAUSE, params))
    }

    private fun logSongComplete(song: Song, durationMs: Long) {
        val completionPercentage = calculateCompletionPercentage(durationMs)
        val params = mutableMapOf<String, Any>(
            AnalyticsConstants.Params.SONG_ID to song.id,
            AnalyticsConstants.Params.SONG_TITLE to song.title,
            AnalyticsConstants.Params.ARTIST to (song.artist ?: "Unknown Artist"),
            AnalyticsConstants.Params.DURATION_MS to durationMs,
            AnalyticsConstants.Params.LISTENED_DURATION_MS to accumulatedListenedDurationMs,
            AnalyticsConstants.Params.COMPLETION_PERCENTAGE to completionPercentage
        )
        song.imageUrl?.let { params[AnalyticsConstants.Params.ALBUM] = it }
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.SONG_COMPLETE, params))
    }

    private fun logSongSkipNext(song: Song, positionMs: Long, durationMs: Long, playbackMode: String) {
        val params = mutableMapOf<String, Any>(
            AnalyticsConstants.Params.SONG_ID to song.id,
            AnalyticsConstants.Params.SONG_TITLE to song.title,
            AnalyticsConstants.Params.ARTIST to (song.artist ?: "Unknown Artist"),
            AnalyticsConstants.Params.CURRENT_POSITION_MS to positionMs,
            AnalyticsConstants.Params.DURATION_MS to durationMs,
            AnalyticsConstants.Params.PLAYBACK_MODE to playbackMode
        )
        song.imageUrl?.let { params[AnalyticsConstants.Params.ALBUM] = it }
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.SONG_SKIP_NEXT, params))
    }

    private fun logSongSkipPrevious(song: Song, positionMs: Long, durationMs: Long, playbackMode: String) {
        val params = mutableMapOf<String, Any>(
            AnalyticsConstants.Params.SONG_ID to song.id,
            AnalyticsConstants.Params.SONG_TITLE to song.title,
            AnalyticsConstants.Params.ARTIST to (song.artist ?: "Unknown Artist"),
            AnalyticsConstants.Params.CURRENT_POSITION_MS to positionMs,
            AnalyticsConstants.Params.DURATION_MS to durationMs,
            AnalyticsConstants.Params.PLAYBACK_MODE to playbackMode
        )
        song.imageUrl?.let { params[AnalyticsConstants.Params.ALBUM] = it }
        analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.SONG_SKIP_PREVIOUS, params))
    }
}
