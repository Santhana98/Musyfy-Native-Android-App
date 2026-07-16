package com.musyfy.nativeapp.core.analytics

interface AnalyticsManager {
    fun logEvent(event: AnalyticsEvent)
    fun logScreenView(screenName: String, className: String? = null)
    fun setUserProperty(name: String, value: String?)
}
