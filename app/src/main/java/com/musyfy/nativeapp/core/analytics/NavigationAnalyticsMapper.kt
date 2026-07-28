package com.musyfy.nativeapp.core.analytics

object NavigationAnalyticsMapper {
    /**
     * Maps a navigation route or active tab string to its corresponding Firebase screen name.
     */
    fun mapRouteToScreenName(routeOrTab: String?): String? {
        return when (routeOrTab) {
            "splash" -> null
            "login" -> "Login"
            "register" -> "Register"
            "forgot_password" -> "ForgotPassword"
            "home" -> "Home"
            "search" -> "Search"
            "upload" -> "Upload"
            "liked" -> "Liked"
            "settings" -> "Settings"
            else -> null
        }
    }
}
