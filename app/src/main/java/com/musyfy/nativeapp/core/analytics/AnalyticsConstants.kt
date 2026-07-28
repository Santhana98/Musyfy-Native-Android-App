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
        const val SONG_LISTENED = "song_listened"
        const val SONG_25_PERCENT = "song_25_percent"
        const val SONG_50_PERCENT = "song_50_percent"
        const val SONG_75_PERCENT = "song_75_percent"
        const val SONG_COMPLETED = "song_completed"
        const val SONG_SKIPPED = "song_skipped"
        const val LISTENING_SESSION = "listening_session"
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
        const val DURATION_SECONDS = "duration_seconds"
        const val SONG_LENGTH = "song_length"
        const val COMPLETION_PERCENT = "completion_percent"
        const val PLAYED_SECONDS = "played_seconds"
        const val SKIP_REASON = "skip_reason"
        const val SESSION_DURATION_SECONDS = "session_duration_seconds"
        const val TOTAL_LISTENING_SECONDS = "total_listening_seconds"
        const val SONGS_PLAYED = "songs_played"
        const val SONGS_COMPLETED = "songs_completed"
        const val SONGS_SKIPPED = "songs_skipped"
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
        const val METHOD = "method"
        const val LOGIN_SOURCE = "login_source"
        const val LOGOUT_SOURCE = "logout_source"
    }

    object Auth {
        const val METHOD_LOCAL = "local"
        const val LOGIN_SOURCE_LOGIN_SCREEN = "login_screen"
        const val LOGOUT_SOURCE_HOME_PROFILE = "home_profile"
        const val LOGOUT_SOURCE_SETTINGS = "settings"
    }

    object SkipReasons {
        const val NEXT_BUTTON = "next_button"
        const val PREVIOUS_BUTTON = "previous_button"
        const val SONG_SELECTED = "song_selected"
        const val QUEUE_CHANGE = "queue_change"
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
