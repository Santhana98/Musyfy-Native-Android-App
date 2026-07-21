package com.musyfy.nativeapp.core.analytics

import com.musyfy.nativeapp.feature.appearance.domain.model.AccentMode
import com.musyfy.nativeapp.feature.appearance.domain.model.PlayerBackgroundMode
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsAnalyticsTrackerTest {

    private val loggedEvents = mutableListOf<AnalyticsEvent>()

    private val fakeAnalyticsManager = object : AnalyticsManager {
        override fun logEvent(event: AnalyticsEvent) {
            loggedEvents.add(event)
        }
        override fun logScreenView(screenName: String, className: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private val tracker = SettingsAnalyticsTracker(fakeAnalyticsManager)

    @Before
    fun setUp() {
        loggedEvents.clear()
        tracker.clearMemory()
    }

    @Test
    fun testInitialLoad_doesNotLogEvents() {
        tracker.trackThemeChanged(ThemeMode.X)
        tracker.trackPlayerModeChanged(PlayerBackgroundMode.BLACK)
        tracker.trackWallpaperChanged(null, 0L)
        tracker.trackAccentChanged(AccentMode.AUTO)

        assertTrue(loggedEvents.isEmpty())
    }

    @Test
    fun testThemeChange_logsCorrectly() {
        // Load initial state
        tracker.trackThemeChanged(ThemeMode.X)
        assertTrue(loggedEvents.isEmpty())

        // Change theme
        tracker.trackThemeChanged(ThemeMode.Y)
        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.THEME_CHANGED, event.name)
        assertEquals("y_theme", event.params[AnalyticsConstants.Params.THEME_NAME])

        // Same theme transition ignored
        tracker.trackThemeChanged(ThemeMode.Y)
        assertEquals(1, loggedEvents.size)

        // Change to custom
        tracker.trackThemeChanged(ThemeMode.CUSTOM)
        assertEquals(2, loggedEvents.size)
        assertEquals("custom_theme", loggedEvents[1].params[AnalyticsConstants.Params.THEME_NAME])
    }

    @Test
    fun testPlayerModeChange_logsCorrectly() {
        // Load initial state
        tracker.trackPlayerModeChanged(PlayerBackgroundMode.BLACK)
        assertTrue(loggedEvents.isEmpty())

        // Change mode
        tracker.trackPlayerModeChanged(PlayerBackgroundMode.WHITE)
        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.PLAYER_MODE_CHANGED, event.name)
        assertEquals("white", event.params[AnalyticsConstants.Params.PLAYER_MODE])

        // Same ignored
        tracker.trackPlayerModeChanged(PlayerBackgroundMode.WHITE)
        assertEquals(1, loggedEvents.size)
    }

    @Test
    fun testWallpaperChange_transitions() {
        // 1. Initial state (null wallpaper)
        tracker.trackThemeChanged(ThemeMode.X) // needed to initialize lastThemeMode non-null
        tracker.trackWallpaperChanged(null, 0L)
        assertTrue(loggedEvents.isEmpty())

        // 2. null -> wallpaper = added
        tracker.trackWallpaperChanged("path/to/wallpaperA.jpg", 1L)
        assertEquals(1, loggedEvents.size)
        assertEquals(AnalyticsConstants.Events.WALLPAPER_CHANGED, loggedEvents[0].name)
        assertEquals("added", loggedEvents[0].params[AnalyticsConstants.Params.WALLPAPER_ACTION])

        // 3. same wallpaper = ignored
        tracker.trackWallpaperChanged("path/to/wallpaperA.jpg", 1L)
        assertEquals(1, loggedEvents.size)

        // 4. wallpaper A -> wallpaper B = replaced
        tracker.trackWallpaperChanged("path/to/wallpaperB.jpg", 2L)
        assertEquals(2, loggedEvents.size)
        assertEquals("replaced", loggedEvents[1].params[AnalyticsConstants.Params.WALLPAPER_ACTION])

        // 5. wallpaper -> null = removed
        tracker.trackWallpaperChanged(null, 3L)
        assertEquals(3, loggedEvents.size)
        assertEquals("removed", loggedEvents[2].params[AnalyticsConstants.Params.WALLPAPER_ACTION])
    }

    @Test
    fun testAccentChange_logsCorrectly() {
        // Load initial state
        tracker.trackAccentChanged(AccentMode.AUTO)
        assertTrue(loggedEvents.isEmpty())

        // Change mode
        tracker.trackAccentChanged(AccentMode.MUSYFY_ORANGE)
        assertEquals(1, loggedEvents.size)
        val event = loggedEvents[0]
        assertEquals(AnalyticsConstants.Events.ACCENT_CHANGED, event.name)
        assertEquals("default_orange", event.params[AnalyticsConstants.Params.ACCENT_SOURCE])

        // Same ignored
        tracker.trackAccentChanged(AccentMode.MUSYFY_ORANGE)
        assertEquals(1, loggedEvents.size)
    }
}
