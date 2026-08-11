package com.musyfy.nativeapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized tracker for Playlist lifecycle and interaction events.
 * Encapsulates AnalyticsManager logging to prevent scattering analytics calls.
 */
@Singleton
class PlaylistAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    private val playbackSourceProvider: PlaybackSourceProvider
) {

    fun trackPlaylistCreated(
        playlistId: String,
        playlistName: String,
        initialSongCount: Int,
        creationSource: String? = null
    ) {
        val sourceAttr = creationSource ?: playbackSourceProvider.getCurrentSource()
        analyticsManager.logEvent(
            AnalyticsEvent.playlistCreated(
                playlistId = playlistId,
                playlistName = playlistName,
                initialSongCount = initialSongCount,
                creationSource = sourceAttr
            )
        )
    }

    fun trackPlaylistDeleted(
        playlistId: String,
        playlistName: String,
        songCountBeforeDelete: Int
    ) {
        analyticsManager.logEvent(
            AnalyticsEvent.playlistDeleted(
                playlistId = playlistId,
                playlistName = playlistName,
                songCountBeforeDelete = songCountBeforeDelete
            )
        )
    }

    fun trackPlaylistRenamed(
        playlistId: String,
        oldName: String,
        newName: String
    ) {
        if (oldName == newName) return
        analyticsManager.logEvent(
            AnalyticsEvent.playlistRenamed(
                playlistId = playlistId,
                oldName = oldName,
                newName = newName
            )
        )
    }

    fun trackPlaylistPlayStarted(
        playlistId: String,
        playlistName: String,
        songCount: Int,
        songId: String,
        songTitle: String,
        artist: String,
        positionIndex: Int,
        playSource: String? = null,
        shuffleEnabled: Boolean? = null,
        repeatMode: String? = null
    ) {
        val sourceAttr = playSource ?: playbackSourceProvider.getCurrentPlaySource()
        analyticsManager.logEvent(
            AnalyticsEvent.playlistPlayStarted(
                playlistId = playlistId,
                playlistName = playlistName,
                songCount = songCount,
                songId = songId,
                songTitle = songTitle,
                artist = artist,
                positionIndex = positionIndex,
                playSource = sourceAttr,
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode
            )
        )
    }

    fun trackPlaylistSongAdded(
        playlistId: String,
        playlistName: String,
        songId: String,
        songTitle: String,
        artist: String,
        currentPlaylistSongCount: Int,
        addSource: String? = null
    ) {
        val sourceAttr = addSource ?: playbackSourceProvider.getCurrentSource()
        analyticsManager.logEvent(
            AnalyticsEvent.playlistSongAdded(
                playlistId = playlistId,
                playlistName = playlistName,
                songId = songId,
                songTitle = songTitle,
                artist = artist,
                currentPlaylistSongCount = currentPlaylistSongCount,
                addSource = sourceAttr
            )
        )
    }

    fun trackPlaylistSongRemoved(
        playlistId: String,
        playlistName: String,
        songId: String,
        songTitle: String,
        artist: String,
        currentPlaylistSongCount: Int
    ) {
        analyticsManager.logEvent(
            AnalyticsEvent.playlistSongRemoved(
                playlistId = playlistId,
                playlistName = playlistName,
                songId = songId,
                songTitle = songTitle,
                artist = artist,
                currentPlaylistSongCount = currentPlaylistSongCount
            )
        )
    }

    fun trackPlaylistReordered(
        playlistId: String,
        playlistName: String,
        fromIndex: Int,
        toIndex: Int,
        songCount: Int
    ) {
        if (fromIndex == toIndex) return
        analyticsManager.logEvent(
            AnalyticsEvent.playlistReordered(
                playlistId = playlistId,
                playlistName = playlistName,
                fromIndex = fromIndex,
                toIndex = toIndex,
                songCount = songCount
            )
        )
    }
}
