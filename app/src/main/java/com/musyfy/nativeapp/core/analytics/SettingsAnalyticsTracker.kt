package com.musyfy.nativeapp.core.analytics

import com.musyfy.nativeapp.feature.appearance.domain.model.AccentMode
import com.musyfy.nativeapp.feature.appearance.domain.model.PlayerBackgroundMode
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks personalization settings changes (theme, player mode, wallpaper, accent mode).
 * Employs a thread-safe singleton state tracker to filter out initial loads and configuration changes.
 */
@Singleton
class SettingsAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    private var lastThemeMode: ThemeMode? = null
    private var lastPlayerMode: PlayerBackgroundMode? = null
    private var lastWallpaperPath: String? = null
    private var lastWallpaperVersion: Long? = null
    private var lastAccentMode: AccentMode? = null

    /**
     * Tracks saved theme changes. Logs only if the theme changes and it's not the initial load.
     */
    @Synchronized
    fun trackThemeChanged(themeMode: ThemeMode) {
        if (lastThemeMode == null) {
            lastThemeMode = themeMode
            return
        }
        if (themeMode != lastThemeMode) {
            lastThemeMode = themeMode
            val themeName = when (themeMode) {
                ThemeMode.X -> "x_theme"
                ThemeMode.Y -> "y_theme"
                ThemeMode.CUSTOM -> "custom_theme"
            }
            analyticsManager.logEvent(AnalyticsEvent.themeChanged(themeName))
        }
    }

    /**
     * Tracks saved player mode changes. Logs only if the player mode changes and it's not the initial load.
     */
    @Synchronized
    fun trackPlayerModeChanged(mode: PlayerBackgroundMode) {
        if (lastPlayerMode == null) {
            lastPlayerMode = mode
            return
        }
        if (mode != lastPlayerMode) {
            lastPlayerMode = mode
            val modeStr = when (mode) {
                PlayerBackgroundMode.WHITE -> "white"
                PlayerBackgroundMode.BLACK -> "black"
            }
            analyticsManager.logEvent(AnalyticsEvent.playerModeChanged(modeStr))
        }
    }

    /**
     * Tracks saved wallpaper changes. Logs only if the wallpaper changes and it's not the initial load.
     * Transitions:
     * - null -> wallpaper = added
     * - wallpaper A -> wallpaper B = replaced
     * - wallpaper -> null = removed
     */
    @Synchronized
    fun trackWallpaperChanged(path: String?, version: Long) {
        if (lastThemeMode == null) {
            // Initial load of settings, just record the state
            lastWallpaperPath = path
            lastWallpaperVersion = version
            return
        }
        val isWallpaperSame = path == lastWallpaperPath && version == lastWallpaperVersion
        if (!isWallpaperSame) {
            val action = when {
                lastWallpaperPath == null && path != null -> "added"
                lastWallpaperPath != null && path == null -> "removed"
                lastWallpaperPath != null && path != null -> "replaced"
                else -> null
            }
            lastWallpaperPath = path
            lastWallpaperVersion = version
            action?.let {
                analyticsManager.logEvent(AnalyticsEvent.wallpaperChanged(it))
            }
        }
    }

    /**
     * Tracks saved accent mode changes. Logs only if the accent mode changes and it's not the initial load.
     */
    @Synchronized
    fun trackAccentChanged(mode: AccentMode) {
        if (lastAccentMode == null) {
            lastAccentMode = mode
            return
        }
        if (mode != lastAccentMode) {
            lastAccentMode = mode
            val sourceStr = when (mode) {
                AccentMode.MUSYFY_ORANGE -> "default_orange"
                AccentMode.AUTO -> "dynamic_palette"
            }
            analyticsManager.logEvent(AnalyticsEvent.accentChanged(sourceStr))
        }
    }

    /**
     * Helper to reset the tracker memory for unit testing.
     */
    @Synchronized
    internal fun clearMemory() {
        lastThemeMode = null
        lastPlayerMode = null
        lastWallpaperPath = null
        lastWallpaperVersion = null
        lastAccentMode = null
    }
}
