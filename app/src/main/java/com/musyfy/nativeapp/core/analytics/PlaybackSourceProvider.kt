package com.musyfy.nativeapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface providing the current context or screen source of playback actions.
 * Decouples playback analytics from direct dependencies on navigation layers.
 */
interface PlaybackSourceProvider {
    fun getCurrentSource(): String
    fun getCurrentPlaySource(): String = getCurrentSource()
    fun getPlaylistId(): String? = null
    fun setExplicitSource(source: String?, playlistId: String? = null) {}
    fun clearExplicitSource() {}
}

/**
 * Default implementation of PlaybackSourceProvider.
 * Interacts with NavigationAnalyticsTracker to resolve active screen or explicit source overrides.
 */
@Singleton
class DefaultPlaybackSourceProvider @Inject constructor(
    private val navigationAnalyticsTracker: NavigationAnalyticsTracker
) : PlaybackSourceProvider {

    @Volatile
    private var explicitSource: String? = null

    @Volatile
    private var explicitPlaylistId: String? = null

    override fun setExplicitSource(source: String?, playlistId: String?) {
        explicitSource = source
        explicitPlaylistId = playlistId
    }

    override fun clearExplicitSource() {
        explicitSource = null
        explicitPlaylistId = null
    }

    override fun getPlaylistId(): String? = explicitPlaylistId

    override fun getCurrentSource(): String {
        return explicitSource ?: navigationAnalyticsTracker.getCurrentScreen() ?: "unknown"
    }

    override fun getCurrentPlaySource(): String {
        val source = explicitSource ?: navigationAnalyticsTracker.getCurrentScreen()
        return when (source) {
            "Home", "Search", "Liked", "Playlist", "FullPlayer", "MiniPlayer", "Queue", "Notification", "AutoNext" -> source
            else -> source ?: "unknown"
        }
    }
}
