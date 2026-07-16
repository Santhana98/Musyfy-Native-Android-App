package com.musyfy.nativeapp.core.analytics

object AnalyticsConstants {
    object Screens {
        const val HOME = "home_screen"
        const val SEARCH = "search_screen"
        const val LIKED = "liked_screen"
        const val UPLOAD = "upload_screen"
        const val SETTINGS = "settings_screen"
        const val PLAYER = "player_screen"
    }

    object Events {
        const val SCREEN_VIEW = "screen_view"
        const val APP_OPEN = "app_open"
    }

    object Params {
        const val SCREEN_NAME = "screen_name"
        const val SCREEN_CLASS = "screen_class"
    }
}
