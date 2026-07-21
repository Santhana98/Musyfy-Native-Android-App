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
        const val IMPORT_STARTED = "import_started"
        const val IMPORT_COMPLETED = "import_completed"
        const val IMPORT_FAILED = "import_failed"
        const val IMPORT_CANCELLED = "import_cancelled"
        const val DOWNLOAD_STARTED = "download_started"
        const val DOWNLOAD_COMPLETED = "download_completed"
        const val DOWNLOAD_FAILED = "download_failed"
        const val DOWNLOAD_CANCELLED = "download_cancelled"
        const val THEME_CHANGED = "theme_changed"
        const val PLAYER_MODE_CHANGED = "player_mode_changed"
        const val WALLPAPER_CHANGED = "wallpaper_changed"
        const val ACCENT_CHANGED = "accent_changed"
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
        const val IMPORT_SESSION_ID = "import_session_id"
        const val IMPORT_SOURCE = "import_source"
        const val FAILURE_REASON = "failure_reason"
        const val DOWNLOAD_SESSION_ID = "download_session_id"
        const val DOWNLOAD_DURATION_MS = "download_duration_ms"
        const val THEME_NAME = "theme_name"
        const val PLAYER_MODE = "player_mode"
        const val WALLPAPER_ACTION = "wallpaper_action"
        const val ACCENT_SOURCE = "accent_source"
    }

    object FailureReasons {
        const val NETWORK_ERROR = "network_error"
        const val METADATA_ERROR = "metadata_error"
        const val DOWNLOAD_ERROR = "download_error"
        const val STORAGE_ERROR = "storage_error"
        const val INVALID_URL = "invalid_url"
        const val UNKNOWN_ERROR = "unknown_error"
        const val PERMISSION_ERROR = "permission_error"
    }

    object UserProperties {
        const val DEVICE_MODEL = "device_model"
        const val ANDROID_VERSION = "android_version"
        const val APP_VERSION = "app_version"
    }
}
