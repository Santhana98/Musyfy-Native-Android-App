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
        const val LOGIN = "login"
        const val LOGOUT = "logout"
        const val SONG_PLAY = "song_play"
        const val SONG_PAUSE = "song_pause"
        const val SONG_RESUME = "song_resume"
        const val SONG_COMPLETE = "song_complete"
        const val SONG_SKIP_NEXT = "song_skip_next"
        const val SONG_SKIP_PREVIOUS = "song_skip_previous"
        const val SHUFFLE_TOGGLE = "shuffle_toggle"
        const val REPEAT_TOGGLE = "repeat_toggle"
    }

    object Params {
        const val SCREEN_NAME = "screen_name"
        const val SCREEN_CLASS = "screen_class"
        const val SONG_ID = "song_id"
        const val SONG_TITLE = "song_title"
        const val ARTIST = "artist"
        const val ALBUM = "album"
        const val DURATION_MS = "duration_ms"
        const val CURRENT_POSITION_MS = "current_position_ms"
        const val LISTENED_DURATION_MS = "listened_duration_ms"
        const val COMPLETION_PERCENTAGE = "completion_percentage"
        const val SOURCE = "source"
        const val PLAYBACK_MODE = "playback_mode"
        const val SHUFFLE_ENABLED = "shuffle_enabled"
        const val REPEAT_MODE = "repeat_mode"
    }

    object UserProperties {
        const val DEVICE_MODEL = "device_model"
        const val ANDROID_VERSION = "android_version"
        const val APP_VERSION = "app_version"
    }
}
