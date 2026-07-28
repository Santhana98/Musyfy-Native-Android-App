package com.musyfy.nativeapp.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dedicated owner for Application Lifecycle Analytics events (e.g., app_open).
 * Ensures app_open is emitted exactly once per application process lifetime,
 * separated cleanly from UI components and listening analytics.
 */
@Singleton
class AppLifecycleTracker @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    private var appOpenFired = false

    @Synchronized
    fun trackAppOpen() {
        if (!appOpenFired) {
            appOpenFired = true
            analyticsManager.logEvent(AnalyticsEvent(AnalyticsConstants.Events.APP_OPEN))
        }
    }
}
