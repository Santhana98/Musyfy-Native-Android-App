package com.musyfy.nativeapp.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppLifecycleTrackerTest {

    private val loggedEvents = mutableListOf<AnalyticsEvent>()
    private val analyticsManager = object : AnalyticsManager {
        override fun logEvent(event: AnalyticsEvent) {
            loggedEvents.add(event)
        }
        override fun logScreenView(screenName: String, className: String?) {}
        override fun setUserProperty(name: String, value: String?) {}
    }

    private lateinit var tracker: AppLifecycleTracker

    @Before
    fun setUp() {
        loggedEvents.clear()
        tracker = AppLifecycleTracker(analyticsManager)
    }

    @Test
    fun trackAppOpen_firesExactlyOnce() {
        tracker.trackAppOpen()
        tracker.trackAppOpen()
        tracker.trackAppOpen()

        val appOpenEvents = loggedEvents.filter { it.name == AnalyticsConstants.Events.APP_OPEN }
        assertEquals(1, appOpenEvents.size)
    }
}
