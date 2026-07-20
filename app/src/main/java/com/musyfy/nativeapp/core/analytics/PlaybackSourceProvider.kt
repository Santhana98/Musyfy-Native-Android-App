package com.musyfy.nativeapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface providing the current context or screen source of playback actions.
 * Decouples playback analytics from direct dependencies on navigation layers.
 */
interface PlaybackSourceProvider {
    fun getCurrentSource(): String
}

/**
 * Default implementation of PlaybackSourceProvider.
 * Defaults to "unknown" and can be extended or replaced dynamically.
 */
@Singleton
class DefaultPlaybackSourceProvider @Inject constructor() : PlaybackSourceProvider {
    override fun getCurrentSource(): String = "unknown"
}
