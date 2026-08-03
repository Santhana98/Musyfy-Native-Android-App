package com.musyfy.nativeapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized tracker for Library events (liked songs, unliked songs, deleted songs).
 * Encapsulates AnalyticsManager logging to prevent scattering analytics calls across UI/ViewModels.
 */
@Singleton
class LibraryAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    private val playbackSourceProvider: PlaybackSourceProvider
) {

    fun trackLikedSong(
        songId: String,
        songTitle: String,
        artist: String,
        source: String? = null,
        songDurationSeconds: Long? = null,
        isCurrentlyPlaying: Boolean? = null
    ) {
        val sourceAttr = source ?: playbackSourceProvider.getCurrentSource()
        analyticsManager.logEvent(
            AnalyticsEvent.likedSong(
                songId = songId,
                songTitle = songTitle,
                artist = artist,
                source = sourceAttr,
                songDurationSeconds = songDurationSeconds,
                isCurrentlyPlaying = isCurrentlyPlaying
            )
        )
    }

    fun trackRemovedLikedSong(
        songId: String,
        songTitle: String,
        artist: String,
        source: String? = null,
        songDurationSeconds: Long? = null,
        isCurrentlyPlaying: Boolean? = null
    ) {
        val sourceAttr = source ?: playbackSourceProvider.getCurrentSource()
        analyticsManager.logEvent(
            AnalyticsEvent.removedLikedSong(
                songId = songId,
                songTitle = songTitle,
                artist = artist,
                source = sourceAttr,
                songDurationSeconds = songDurationSeconds,
                isCurrentlyPlaying = isCurrentlyPlaying
            )
        )
    }

    fun trackSongDeleted(
        songId: String,
        songTitle: String,
        artist: String,
        deleteSource: String? = null,
        wasLiked: Boolean = false,
        playlistCount: Int = 0,
        songDurationSeconds: Long? = null,
        isCurrentlyPlaying: Boolean? = null
    ) {
        val sourceAttr = deleteSource ?: playbackSourceProvider.getCurrentSource()
        analyticsManager.logEvent(
            AnalyticsEvent.songDeleted(
                songId = songId,
                songTitle = songTitle,
                artist = artist,
                deleteSource = sourceAttr,
                wasLiked = wasLiked,
                playlistCount = playlistCount,
                songDurationSeconds = songDurationSeconds,
                isCurrentlyPlaying = isCurrentlyPlaying
            )
        )
    }
}
