package com.musyfy.nativeapp.core.analytics

import androidx.media3.common.Player
import com.musyfy.nativeapp.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Phase 9.6.2 – Listening Engagement & Threshold Analytics Tracker.
 * Tracks actual audible listening duration for each song (only while in ExoPlayer Playing state)
 * and emits milestone events (25%, 50%, 75%, and completion) plus a final song_listened event.
 */
@Singleton
class ListeningAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    private val playbackSourceProvider: PlaybackSourceProvider? = null
) {
    /**
     * Injectable/overridable time provider for unit testing.
     */
    var timeProvider: () -> Long = { System.currentTimeMillis() }

    private var currentSong: Song? = null
    private var totalSongDurationMs: Long = 0L
    private var accumulatedListeningMs: Long = 0L
    private var playingStartTimestamp: Long = 0L
    private var isPlayingActive: Boolean = false

    private val firedThresholds = mutableSetOf<String>()

    private var pendingSkipReason: String? = null

    private var sessionActive = false
    private var sessionStartTimestamp = 0L
    private var sessionTotalListeningMs = 0L
    private var sessionSongsPlayed = 0
    private var sessionSongsCompleted = 0
    private var sessionSongsSkipped = 0
    private var hasCountedPlayForCurrentSong = false
    private var hasFiredSongPlayForCurrentSong = false

    @Synchronized
    fun setPendingSkipReason(reason: String) {
        pendingSkipReason = reason
    }

    @Synchronized
    fun hasPendingSkipReason(): Boolean = pendingSkipReason != null

    @Synchronized
    fun getAndClearPendingSkipReason(): String? {
        val reason = pendingSkipReason
        pendingSkipReason = null
        return reason
    }

    private fun ensureSessionStarted() {
        if (!sessionActive) {
            sessionActive = true
            sessionStartTimestamp = timeProvider()
            sessionTotalListeningMs = 0L
            sessionSongsPlayed = 0
            sessionSongsCompleted = 0
            sessionSongsSkipped = 0
            hasCountedPlayForCurrentSong = false
            android.util.Log.d("ListeningSessionDebug", "Session STARTED: timestamp=$sessionStartTimestamp")
        }
    }

    @Synchronized
    fun onSessionEnded() {
        android.util.Log.d("ListeningSessionDebug", "onSessionEnded() CALLED: sessionActive=$sessionActive")
        if (!sessionActive) {
            android.util.Log.d("ListeningSessionDebug", "onSessionEnded() EARLY RETURN: sessionActive is false")
            return
        }

        if (currentSong != null) {
            finalizeCurrentSong(transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)
        }

        if (isPlayingActive) {
            accumulateCurrentInterval()
            isPlayingActive = false
        }

        val sessionDuration = (timeProvider() - sessionStartTimestamp) / 1000L
        val totalListening = sessionTotalListeningMs / 1000L

        val event = AnalyticsEvent.listeningSession(
            sessionDurationSeconds = sessionDuration,
            totalListeningSeconds = totalListening,
            songsPlayed = sessionSongsPlayed,
            songsCompleted = sessionSongsCompleted,
            songsSkipped = sessionSongsSkipped
        )
        android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Emitting listening_session: event=$event")
        analyticsManager.logEvent(event)
        android.util.Log.d("ListeningSessionDebug", "AFTER AnalyticsManager.logEvent(listening_session)")

        sessionActive = false
    }

    @Synchronized
    fun onSongCompleted() {
        onMediaItemTransition(
            newSong = null,
            newDurationMs = 0L,
            transitionReason = Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
        )
    }

    @Synchronized
    fun onMediaItemTransition(
        newSong: Song?,
        newDurationMs: Long = 0L,
        skipReason: String? = null,
        transitionReason: Int = -1,
        isPlaying: Boolean = false
    ) {
        android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] onMediaItemTransition called. newSong=${newSong?.id}, duration=$newDurationMs, skipReason=$skipReason, transitionReason=$transitionReason, isPlaying=$isPlaying")
        
        if (currentSong != null) {
            finalizeCurrentSong(transitionReason, skipReason)
        } else {
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] No previous song to finalize.")
        }

        // Reset every runtime tracking field and start tracking new song or end session
        if (newSong != null) {
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Resetting tracker and initializing new song tracking: ${newSong.id}")
            startTrackingNewSong(newSong, newDurationMs)
            if (isPlaying) {
                isPlayingActive = true
                playingStartTimestamp = timeProvider()
                ensureSessionStarted()
                if (!hasCountedPlayForCurrentSong) {
                    hasCountedPlayForCurrentSong = true
                    sessionSongsPlayed++
                }
                checkAndEmitSongPlay(transitionReason)
                android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Tracker initialized with active playback. isPlayingActive=true, playingStartTimestamp=$playingStartTimestamp")
            } else {
                android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Tracker initialized with inactive playback.")
            }
        } else {
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Resetting tracker and ending session because newSong is null")
            currentSong = null
            totalSongDurationMs = 0L
            accumulatedListeningMs = 0L
            playingStartTimestamp = 0L
            isPlayingActive = false
            firedThresholds.clear()
            hasCountedPlayForCurrentSong = false
            hasFiredSongPlayForCurrentSong = false
            onSessionEnded()
        }

        pendingSkipReason = null
    }

    private fun checkAndEmitSongPlay(transitionReason: Int = -1) {
        val song = currentSong ?: return
        if (!hasFiredSongPlayForCurrentSong) {
            hasFiredSongPlayForCurrentSong = true
            val durationSeconds = if (totalSongDurationMs > 0L) totalSongDurationMs / 1000L else song.durationMs / 1000L
            val source = playbackSourceProvider?.getCurrentSource() ?: "android_native"
            val playSource = if (transitionReason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                "AutoNext"
            } else {
                playbackSourceProvider?.getCurrentPlaySource() ?: "unknown"
            }
            val playlistId = playbackSourceProvider?.getPlaylistId()

            val songPlayEvent = AnalyticsEvent.songPlay(
                songId = song.id,
                songTitle = song.title,
                artist = song.artist ?: "Unknown Artist",
                durationSeconds = durationSeconds,
                source = source,
                playSource = playSource,
                playlistId = playlistId
            )
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Emitting song_play: event=$songPlayEvent")
            analyticsManager.logEvent(songPlayEvent)
        }
    }

    private fun finalizeCurrentSong(
        transitionReason: Int = -1,
        skipReason: String? = null
    ) {
        val song = currentSong ?: return
        android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] finalizeCurrentSong called: ${song.id}, hasCountedPlay=$hasCountedPlayForCurrentSong")

        if (isPlayingActive) {
            accumulateCurrentInterval()
            isPlayingActive = false
        }
        checkThresholds()

        if (hasCountedPlayForCurrentSong) {
            val percentage = getCompletionPercentage()
            val isNaturalCompletion = firedThresholds.contains("100") ||
                    percentage >= 99 ||
                    (transitionReason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && percentage >= 90)

            val durationSeconds = accumulatedListeningMs / 1000L
            val songLengthSeconds = totalSongDurationMs / 1000L

            if (isNaturalCompletion) {
                if (sessionActive) {
                    sessionSongsCompleted++
                }
                if (!firedThresholds.contains("100")) {
                    firedThresholds.add("100")
                    logThresholdEvent(
                        AnalyticsConstants.Events.SONG_COMPLETED,
                        song,
                        durationSeconds,
                        songLengthSeconds,
                        percentage
                    )
                }
            } else {
                if (sessionActive) {
                    sessionSongsSkipped++
                }
                val resolvedReason = skipReason ?: pendingSkipReason ?: AnalyticsConstants.SkipReasons.SONG_SELECTED
                val skipEvent = AnalyticsEvent.songSkipped(
                    songId = song.id,
                    songTitle = song.title,
                    artist = song.artist ?: "Unknown Artist",
                    playedSeconds = durationSeconds,
                    songLength = songLengthSeconds,
                    completionPercentage = percentage,
                    skipReason = resolvedReason
                )
                android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Emitting song_skipped: event=$skipEvent")
                analyticsManager.logEvent(skipEvent)
            }

            val listenedEvent = AnalyticsEvent.songListened(
                songId = song.id,
                songTitle = song.title,
                artist = song.artist ?: "Unknown Artist",
                durationSeconds = durationSeconds,
                songLength = songLengthSeconds,
                completionPercent = percentage
            )
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Emitting song_listened: event=$listenedEvent")
            analyticsManager.logEvent(listenedEvent)
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Previous song finalized: ${song.id}")
        } else {
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Skipping finalization events for ${song.id} because it was never played in this process lifetime.")
        }
        currentSong = null
    }

    /**
     * Responds to changes in player play/pause state and ExoPlayer state.
     * Only counts time when isPlaying is true AND playbackState is Player.STATE_READY.
     * Paused and buffering states are excluded.
     */
    @Synchronized
    fun onPlaybackStateChanged(isPlaying: Boolean, playbackState: Int, durationMs: Long = 0L) {
        android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] onPlaybackStateChanged: isPlaying=$isPlaying, playbackState=$playbackState, durationMs=$durationMs")
        if (currentSong == null) return

        if (durationMs > 0L && (totalSongDurationMs <= 0L || totalSongDurationMs != durationMs)) {
            totalSongDurationMs = durationMs
        }

        val shouldBePlayingActive = isPlaying && playbackState == Player.STATE_READY

        if (shouldBePlayingActive) {
            ensureSessionStarted()
            if (!hasCountedPlayForCurrentSong) {
                hasCountedPlayForCurrentSong = true
                sessionSongsPlayed++
            }
            checkAndEmitSongPlay()
        }

        if (shouldBePlayingActive && !isPlayingActive) {
            // Start playing interval
            isPlayingActive = true
            playingStartTimestamp = timeProvider()
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Playback became active. playingStartTimestamp=$playingStartTimestamp")
        } else if (!shouldBePlayingActive && isPlayingActive) {
            // End playing interval & accumulate active duration
            accumulateCurrentInterval()
            isPlayingActive = false
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Playback became inactive. accumulatedListeningMs=$accumulatedListeningMs")
            checkThresholds()
        }
    }

    /**
     * Responds to periodic progress updates from the existing playback loop.
     * Incrementally accumulates active listening time and evaluates threshold milestones.
     */
    @Synchronized
    fun onProgressUpdate(currentPositionMs: Long, durationMs: Long = 0L) {
        if (currentSong == null) return

        if (durationMs > 0L && (totalSongDurationMs <= 0L || totalSongDurationMs != durationMs)) {
            totalSongDurationMs = durationMs
        }

        android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] onProgressUpdate: currentSong=${currentSong?.id}, currentPositionMs=$currentPositionMs, durationMs=$durationMs, isPlayingActive=$isPlayingActive, playingStartTimestamp=$playingStartTimestamp")

        if (isPlayingActive && playingStartTimestamp > 0L) {
            ensureSessionStarted()
            if (!hasCountedPlayForCurrentSong) {
                hasCountedPlayForCurrentSong = true
                sessionSongsPlayed++
            }
            checkAndEmitSongPlay()
            val now = timeProvider()
            val elapsed = now - playingStartTimestamp
            if (elapsed > 0L) {
                accumulatedListeningMs += elapsed
                if (sessionActive) {
                    sessionTotalListeningMs += elapsed
                }
                playingStartTimestamp = now // slide starting point forward
                android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Progress accumulated: elapsed=$elapsed, accumulatedListeningMs=$accumulatedListeningMs")
            }
            checkThresholds()
        }
    }

    private fun startTrackingNewSong(song: Song, durationMs: Long) {
        currentSong = song
        totalSongDurationMs = durationMs.coerceAtLeast(0L)
        accumulatedListeningMs = 0L
        playingStartTimestamp = 0L
        isPlayingActive = false
        firedThresholds.clear()
        hasCountedPlayForCurrentSong = false
        hasFiredSongPlayForCurrentSong = false
    }

    private fun accumulateCurrentInterval() {
        if (playingStartTimestamp > 0L) {
            val now = timeProvider()
            val elapsed = now - playingStartTimestamp
            if (elapsed > 0L) {
                accumulatedListeningMs += elapsed
                if (sessionActive) {
                    sessionTotalListeningMs += elapsed
                }
            }
            playingStartTimestamp = 0L
        }
    }

    private fun getCompletionPercentage(): Int {
        if (totalSongDurationMs <= 0L) return 0
        return ((accumulatedListeningMs.toDouble() / totalSongDurationMs.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
    }

    private fun checkThresholds() {
        val song = currentSong ?: return
        val songLengthSeconds = totalSongDurationMs / 1000L
        if (songLengthSeconds <= 0L) return

        val durationSeconds = accumulatedListeningMs / 1000L
        val percentage = getCompletionPercentage()
        android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] checkThresholds: song=${song.id}, durationSeconds=$durationSeconds, songLengthSeconds=$songLengthSeconds, percentage=$percentage, fired=${firedThresholds}")

        if (percentage >= 25 && !firedThresholds.contains("25")) {
            firedThresholds.add("25")
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Firing 25% milestone for ${song.id}")
            logThresholdEvent(AnalyticsConstants.Events.SONG_25_PERCENT, song, durationSeconds, songLengthSeconds, percentage)
        }
        if (percentage >= 50 && !firedThresholds.contains("50")) {
            firedThresholds.add("50")
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Firing 50% milestone for ${song.id}")
            logThresholdEvent(AnalyticsConstants.Events.SONG_50_PERCENT, song, durationSeconds, songLengthSeconds, percentage)
        }
        if (percentage >= 75 && !firedThresholds.contains("75")) {
            firedThresholds.add("75")
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Firing 75% milestone for ${song.id}")
            logThresholdEvent(AnalyticsConstants.Events.SONG_75_PERCENT, song, durationSeconds, songLengthSeconds, percentage)
        }
        if (percentage >= 100 && !firedThresholds.contains("100")) {
            firedThresholds.add("100")
            android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Firing 100% milestone (completion) for ${song.id}")
            logThresholdEvent(AnalyticsConstants.Events.SONG_COMPLETED, song, durationSeconds, songLengthSeconds, percentage)
        }
    }

    private fun logThresholdEvent(
        eventName: String,
        song: Song,
        durationSeconds: Long,
        songLengthSeconds: Long,
        completionPercentage: Int
    ) {
        val event = AnalyticsEvent.songThreshold(
            eventName = eventName,
            songId = song.id,
            songTitle = song.title,
            artist = song.artist ?: "Unknown Artist",
            durationSeconds = durationSeconds,
            songLength = songLengthSeconds,
            completionPercentage = completionPercentage
        )
        android.util.Log.d("ListeningAnalyticsTracker", "[DEBUG-TRACKER] Emitting threshold event: $eventName, event=$event")
        analyticsManager.logEvent(event)
    }

    private fun flushAndReset(nextSong: Song?, nextDurationMs: Long, skipReason: String? = null) {
        if (isPlayingActive) {
            accumulateCurrentInterval()
            isPlayingActive = false
        }
        checkThresholds()

        val song = currentSong
        if (song != null) {
            val durationSeconds = accumulatedListeningMs / 1000L
            val songLengthSeconds = totalSongDurationMs / 1000L
            val completionPercentInt = getCompletionPercentage()

            if (skipReason != null) {
                if (sessionActive) {
                    sessionSongsSkipped++
                }
                val skipEvent = AnalyticsEvent.songSkipped(
                    songId = song.id,
                    songTitle = song.title,
                    artist = song.artist ?: "Unknown Artist",
                    playedSeconds = durationSeconds,
                    songLength = songLengthSeconds,
                    completionPercentage = completionPercentInt,
                    skipReason = skipReason
                )
                analyticsManager.logEvent(skipEvent)
            }

            val event = AnalyticsEvent.songListened(
                songId = song.id,
                songTitle = song.title,
                artist = song.artist ?: "Unknown Artist",
                durationSeconds = durationSeconds,
                songLength = songLengthSeconds,
                completionPercent = completionPercentInt
            )
            analyticsManager.logEvent(event)
        }

        if (nextSong != null) {
            startTrackingNewSong(nextSong, nextDurationMs)
        } else {
            currentSong = null
            totalSongDurationMs = 0L
            accumulatedListeningMs = 0L
            playingStartTimestamp = 0L
            isPlayingActive = false
            firedThresholds.clear()
        }
    }
}
