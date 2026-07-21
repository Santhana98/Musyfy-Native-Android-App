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
    }
}
