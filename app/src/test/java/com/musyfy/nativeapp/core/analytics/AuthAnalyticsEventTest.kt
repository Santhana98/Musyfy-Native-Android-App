package com.musyfy.nativeapp.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthAnalyticsEventTest {

    @Test
    fun testLoginEvent_defaultParameters() {
        val event = AnalyticsEvent.login()
        assertEquals(AnalyticsConstants.Events.LOGIN, event.name)
        assertEquals("local", event.params[AnalyticsConstants.Params.METHOD])
        assertEquals("login_screen", event.params[AnalyticsConstants.Params.LOGIN_SOURCE])
    }

    @Test
    fun testLoginEvent_customParameters() {
        val event = AnalyticsEvent.login(method = "custom_local", loginSource = "custom_screen")
        assertEquals(AnalyticsConstants.Events.LOGIN, event.name)
        assertEquals("custom_local", event.params[AnalyticsConstants.Params.METHOD])
        assertEquals("custom_screen", event.params[AnalyticsConstants.Params.LOGIN_SOURCE])
    }

    @Test
    fun testLogoutEvent_homeProfileSource_withoutDuration() {
        val event = AnalyticsEvent.logout(logoutSource = AnalyticsConstants.Auth.LOGOUT_SOURCE_HOME_PROFILE)
        assertEquals(AnalyticsConstants.Events.LOGOUT, event.name)
        assertEquals("local", event.params[AnalyticsConstants.Params.METHOD])
        assertEquals("home_profile", event.params[AnalyticsConstants.Params.LOGOUT_SOURCE])
        assertNull(event.params[AnalyticsConstants.Params.SESSION_DURATION_SECONDS])
    }

    @Test
    fun testLogoutEvent_settingsSource_withDuration() {
        val event = AnalyticsEvent.logout(
            logoutSource = AnalyticsConstants.Auth.LOGOUT_SOURCE_SETTINGS,
            sessionDurationSeconds = 120L
        )
        assertEquals(AnalyticsConstants.Events.LOGOUT, event.name)
        assertEquals("local", event.params[AnalyticsConstants.Params.METHOD])
        assertEquals("settings", event.params[AnalyticsConstants.Params.LOGOUT_SOURCE])
        assertEquals(120L, event.params[AnalyticsConstants.Params.SESSION_DURATION_SECONDS])
    }
}
