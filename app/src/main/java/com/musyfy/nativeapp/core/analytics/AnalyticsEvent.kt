package com.musyfy.nativeapp.core.analytics

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap()
) {
    companion object {
        fun screenView(screenName: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SCREEN_VIEW,
                params = mapOf(AnalyticsConstants.Params.SCREEN_NAME to screenName)
            )
        }
    }
}
