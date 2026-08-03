package com.musyfy.nativeapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationAnalyticsTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    @Volatile
    private var lastLoggedScreen: String? = null

    /**
     * Logs the screen view event if it represents a genuine navigation transition (i.e. different from the last logged screen).
     * Automatically includes previous_screen and optional screen_category.
     */
    fun logScreenView(screenName: String, category: String? = null) {
        synchronized(this) {
            if (screenName != lastLoggedScreen) {
                val previousScreen = lastLoggedScreen
                lastLoggedScreen = screenName
                analyticsManager.logEvent(
                    AnalyticsEvent.screenView(
                        screenName = screenName,
                        previousScreen = previousScreen,
                        screenCategory = category
                    )
                )
            }
        }
    }

    /**
     * Returns the currently active logged screen.
     */
    fun getCurrentScreen(): String? = synchronized(this) { lastLoggedScreen }

    /**
     * Resets the tracker memory if needed.
     */
    fun clearLastLoggedScreen() {
        synchronized(this) {
            lastLoggedScreen = null
        }
    }
}
