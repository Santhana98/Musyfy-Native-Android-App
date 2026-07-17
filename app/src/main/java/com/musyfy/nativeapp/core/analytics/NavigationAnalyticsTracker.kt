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
     */
    fun logScreenView(screenName: String) {
        synchronized(this) {
            if (screenName != lastLoggedScreen) {
                lastLoggedScreen = screenName
                analyticsManager.logEvent(AnalyticsEvent.screenView(screenName))
            }
        }
    }

    /**
     * Resets the tracker memory if needed.
     */
    fun clearLastLoggedScreen() {
        synchronized(this) {
            lastLoggedScreen = null
        }
    }
}
