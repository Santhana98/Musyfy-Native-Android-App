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

        fun importStarted(sessionId: String, source: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.IMPORT_STARTED,
                params = mapOf(
                    AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.IMPORT_SOURCE to source
                )
            )
        }

        fun importCompleted(sessionId: String, source: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.IMPORT_COMPLETED,
                params = mapOf(
                    AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.IMPORT_SOURCE to source
                )
            )
        }

        fun importFailed(sessionId: String, reason: String, source: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.IMPORT_FAILED,
                params = mapOf(
                    AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.IMPORT_SOURCE to source,
                    AnalyticsConstants.Params.FAILURE_REASON to reason
                )
            )
        }

        fun importCancelled(sessionId: String, source: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.IMPORT_CANCELLED,
                params = mapOf(
                    AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.IMPORT_SOURCE to source
                )
            )
        }

        fun downloadStarted(sessionId: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.DOWNLOAD_STARTED,
                params = mapOf(
                    AnalyticsConstants.Params.DOWNLOAD_SESSION_ID to sessionId
                )
            )
        }

        fun downloadCompleted(sessionId: String, durationMs: Long): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.DOWNLOAD_COMPLETED,
                params = mapOf(
                    AnalyticsConstants.Params.DOWNLOAD_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.DOWNLOAD_DURATION_MS to durationMs
                )
            )
        }

        fun downloadFailed(sessionId: String, reason: String, durationMs: Long): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.DOWNLOAD_FAILED,
                params = mapOf(
                    AnalyticsConstants.Params.DOWNLOAD_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.FAILURE_REASON to reason,
                    AnalyticsConstants.Params.DOWNLOAD_DURATION_MS to durationMs
                )
            )
        }

        fun downloadCancelled(sessionId: String, durationMs: Long): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.DOWNLOAD_CANCELLED,
                params = mapOf(
                    AnalyticsConstants.Params.DOWNLOAD_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.DOWNLOAD_DURATION_MS to durationMs
                )
            )
        }

        fun themeChanged(themeName: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.THEME_CHANGED,
                params = mapOf(AnalyticsConstants.Params.THEME_NAME to themeName)
            )
        }

        fun playerModeChanged(playerMode: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYER_MODE_CHANGED,
                params = mapOf(AnalyticsConstants.Params.PLAYER_MODE to playerMode)
            )
        }

        fun wallpaperChanged(action: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.WALLPAPER_CHANGED,
                params = mapOf(AnalyticsConstants.Params.WALLPAPER_ACTION to action)
            )
        }

        fun accentChanged(source: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.ACCENT_CHANGED,
                params = mapOf(AnalyticsConstants.Params.ACCENT_SOURCE to source)
            )
        }

        fun songListened(
            songId: String,
            songTitle: String,
            artist: String,
            durationSeconds: Long,
            songLength: Long,
            completionPercent: Int
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SONG_LISTENED,
                params = mapOf(
                    AnalyticsConstants.Params.SONG_ID to songId,
                    AnalyticsConstants.Params.SONG_TITLE to songTitle,
                    AnalyticsConstants.Params.ARTIST to artist,
                    AnalyticsConstants.Params.DURATION_SECONDS to durationSeconds,
                    AnalyticsConstants.Params.SONG_LENGTH to songLength,
                    AnalyticsConstants.Params.COMPLETION_PERCENT to completionPercent
                )
            )
        }

        fun songThreshold(
            eventName: String,
            songId: String,
            songTitle: String,
            artist: String,
            durationSeconds: Long,
            songLength: Long,
            completionPercentage: Int
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = eventName,
                params = mapOf(
                    AnalyticsConstants.Params.SONG_ID to songId,
                    AnalyticsConstants.Params.SONG_TITLE to songTitle,
                    AnalyticsConstants.Params.ARTIST to artist,
                    AnalyticsConstants.Params.DURATION_SECONDS to durationSeconds,
                    AnalyticsConstants.Params.SONG_LENGTH to songLength,
                    AnalyticsConstants.Params.COMPLETION_PERCENTAGE to completionPercentage
                )
            )
        }

        fun songSkipped(
            songId: String,
            songTitle: String,
            artist: String,
            playedSeconds: Long,
            songLength: Long,
            completionPercentage: Int,
            skipReason: String
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SONG_SKIPPED,
                params = mapOf(
                    AnalyticsConstants.Params.SONG_ID to songId,
                    AnalyticsConstants.Params.SONG_TITLE to songTitle,
                    AnalyticsConstants.Params.ARTIST to artist,
                    AnalyticsConstants.Params.PLAYED_SECONDS to playedSeconds,
                    AnalyticsConstants.Params.SONG_LENGTH to songLength,
                    AnalyticsConstants.Params.COMPLETION_PERCENTAGE to completionPercentage,
                    AnalyticsConstants.Params.SKIP_REASON to skipReason
                )
            )
        }

        fun listeningSession(
            sessionDurationSeconds: Long,
            totalListeningSeconds: Long,
            songsPlayed: Int,
            songsCompleted: Int,
            songsSkipped: Int
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.LISTENING_SESSION,
                params = mapOf(
                    AnalyticsConstants.Params.SESSION_DURATION_SECONDS to sessionDurationSeconds,
                    AnalyticsConstants.Params.TOTAL_LISTENING_SECONDS to totalListeningSeconds,
                    AnalyticsConstants.Params.SONGS_PLAYED to songsPlayed,
                    AnalyticsConstants.Params.SONGS_COMPLETED to songsCompleted,
                    AnalyticsConstants.Params.SONGS_SKIPPED to songsSkipped
                )
            )
        }

        fun login(
            method: String = AnalyticsConstants.Auth.METHOD_LOCAL,
            loginSource: String = AnalyticsConstants.Auth.LOGIN_SOURCE_LOGIN_SCREEN
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.LOGIN,
                params = mapOf(
                    AnalyticsConstants.Params.METHOD to method,
                    AnalyticsConstants.Params.LOGIN_SOURCE to loginSource
                )
            )
        }

        fun logout(
            logoutSource: String,
            method: String = AnalyticsConstants.Auth.METHOD_LOCAL,
            sessionDurationSeconds: Long? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.METHOD to method,
                AnalyticsConstants.Params.LOGOUT_SOURCE to logoutSource
            )
            if (sessionDurationSeconds != null && sessionDurationSeconds >= 0) {
                params[AnalyticsConstants.Params.SESSION_DURATION_SECONDS] = sessionDurationSeconds
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.LOGOUT,
                params = params
            )
        }
    }
}
