package com.musyfy.nativeapp.core.analytics

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap()
) {
    companion object {
        fun screenView(
            screenName: String,
            previousScreen: String? = null,
            screenCategory: String? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.SCREEN_NAME to screenName
            )
            if (!previousScreen.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.PREVIOUS_SCREEN] = previousScreen
            }
            if (!screenCategory.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.SCREEN_CATEGORY] = screenCategory
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SCREEN_VIEW,
                params = params
            )
        }

        fun feedbackScreenViewed(source: String? = null): AnalyticsEvent {
            val params = mutableMapOf<String, Any>()
            if (!source.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.SOURCE] = source
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.FEEDBACK_SCREEN_VIEWED,
                params = params
            )
        }

        fun feedbackSendInitiated(source: String? = null): AnalyticsEvent {
            val params = mutableMapOf<String, Any>()
            if (!source.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.SOURCE] = source
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.FEEDBACK_SEND_INITIATED,
                params = params
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

        fun downloadCompleted(
            sessionId: String,
            durationMs: Long,
            fileSizeBytes: Long = 0L,
            averageDownloadSpeedMbps: Double = 0.0,
            networkType: String? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.DOWNLOAD_SESSION_ID to sessionId,
                AnalyticsConstants.Params.DOWNLOAD_DURATION_MS to durationMs
            )
            if (fileSizeBytes > 0) {
                params[AnalyticsConstants.Params.FILE_SIZE_BYTES] = fileSizeBytes
            }
            if (averageDownloadSpeedMbps > 0) {
                params[AnalyticsConstants.Params.AVERAGE_DOWNLOAD_SPEED_MBPS] = averageDownloadSpeedMbps
            }
            if (!networkType.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.NETWORK_TYPE] = networkType
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.DOWNLOAD_COMPLETED,
                params = params
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

        fun songPlay(
            songId: String,
            songTitle: String,
            artist: String,
            durationSeconds: Long,
            source: String = "android_native",
            playSource: String = "unknown",
            playlistId: String? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.SONG_ID to songId,
                AnalyticsConstants.Params.SONG_TITLE to songTitle,
                AnalyticsConstants.Params.ARTIST to artist,
                AnalyticsConstants.Params.DURATION_SECONDS to durationSeconds,
                AnalyticsConstants.Params.SOURCE to source,
                AnalyticsConstants.Params.PLAY_SOURCE to playSource
            )
            if (!playlistId.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.PLAYLIST_ID] = playlistId
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SONG_PLAY,
                params = params
            )
        }

        fun playlistCreated(
            playlistId: String,
            playlistName: String,
            initialSongCount: Int,
            creationSource: String
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_CREATED,
                params = mapOf(
                    AnalyticsConstants.Params.PLAYLIST_ID to playlistId,
                    AnalyticsConstants.Params.PLAYLIST_NAME to playlistName,
                    AnalyticsConstants.Params.INITIAL_SONG_COUNT to initialSongCount,
                    AnalyticsConstants.Params.CREATION_SOURCE to creationSource
                )
            )
        }

        fun playlistDeleted(
            playlistId: String,
            playlistName: String,
            songCountBeforeDelete: Int
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_DELETED,
                params = mapOf(
                    AnalyticsConstants.Params.PLAYLIST_ID to playlistId,
                    AnalyticsConstants.Params.PLAYLIST_NAME to playlistName,
                    AnalyticsConstants.Params.SONG_COUNT_BEFORE_DELETE to songCountBeforeDelete
                )
            )
        }

        fun playlistRenamed(
            playlistId: String,
            oldName: String,
            newName: String
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_RENAMED,
                params = mapOf(
                    AnalyticsConstants.Params.PLAYLIST_ID to playlistId,
                    AnalyticsConstants.Params.OLD_NAME to oldName,
                    AnalyticsConstants.Params.NEW_NAME to newName
                )
            )
        }

        fun playlistPlayStarted(
            playlistId: String,
            playlistName: String,
            songCount: Int,
            songId: String,
            songTitle: String,
            artist: String,
            positionIndex: Int,
            playSource: String,
            shuffleEnabled: Boolean? = null,
            repeatMode: String? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.PLAYLIST_ID to playlistId,
                AnalyticsConstants.Params.PLAYLIST_NAME to playlistName,
                AnalyticsConstants.Params.SONG_COUNT to songCount,
                AnalyticsConstants.Params.SONG_ID to songId,
                AnalyticsConstants.Params.SONG_TITLE to songTitle,
                AnalyticsConstants.Params.ARTIST to artist,
                AnalyticsConstants.Params.POSITION_INDEX to positionIndex,
                AnalyticsConstants.Params.PLAY_SOURCE to playSource
            )
            if (shuffleEnabled != null) {
                params[AnalyticsConstants.Params.SHUFFLE_ENABLED] = shuffleEnabled
            }
            if (!repeatMode.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.REPEAT_MODE] = repeatMode
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_PLAY_STARTED,
                params = params
            )
        }

        fun playlistSongAdded(
            playlistId: String,
            playlistName: String,
            songId: String,
            songTitle: String,
            artist: String,
            currentPlaylistSongCount: Int,
            addSource: String
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_SONG_ADDED,
                params = mapOf(
                    AnalyticsConstants.Params.PLAYLIST_ID to playlistId,
                    AnalyticsConstants.Params.PLAYLIST_NAME to playlistName,
                    AnalyticsConstants.Params.SONG_ID to songId,
                    AnalyticsConstants.Params.SONG_TITLE to songTitle,
                    AnalyticsConstants.Params.ARTIST to artist,
                    AnalyticsConstants.Params.CURRENT_PLAYLIST_SONG_COUNT to currentPlaylistSongCount,
                    AnalyticsConstants.Params.ADD_SOURCE to addSource
                )
            )
        }

        fun playlistSongsAdded(
            songsCount: Int,
            addSource: String
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.SONGS_COUNT to songsCount,
                AnalyticsConstants.Params.ADD_SOURCE to addSource
            )
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_SONGS_ADDED,
                params = params
            )
        }

        fun playlistSongRemoved(
            playlistId: String,
            playlistName: String,
            songId: String,
            songTitle: String,
            artist: String,
            currentPlaylistSongCount: Int
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_SONG_REMOVED,
                params = mapOf(
                    AnalyticsConstants.Params.PLAYLIST_ID to playlistId,
                    AnalyticsConstants.Params.PLAYLIST_NAME to playlistName,
                    AnalyticsConstants.Params.SONG_ID to songId,
                    AnalyticsConstants.Params.SONG_TITLE to songTitle,
                    AnalyticsConstants.Params.ARTIST to artist,
                    AnalyticsConstants.Params.CURRENT_PLAYLIST_SONG_COUNT to currentPlaylistSongCount
                )
            )
        }

        fun playlistReordered(
            playlistId: String,
            playlistName: String,
            fromIndex: Int,
            toIndex: Int,
            songCount: Int
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYLIST_REORDERED,
                params = mapOf(
                    AnalyticsConstants.Params.PLAYLIST_ID to playlistId,
                    AnalyticsConstants.Params.PLAYLIST_NAME to playlistName,
                    AnalyticsConstants.Params.FROM_INDEX to fromIndex,
                    AnalyticsConstants.Params.TO_INDEX to toIndex,
                    AnalyticsConstants.Params.SONG_COUNT to songCount
                )
            )
        }

        fun likedSong(
            songId: String,
            songTitle: String,
            artist: String,
            source: String,
            songDurationSeconds: Long? = null,
            isCurrentlyPlaying: Boolean? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.SONG_ID to songId,
                AnalyticsConstants.Params.SONG_TITLE to songTitle,
                AnalyticsConstants.Params.ARTIST to artist,
                AnalyticsConstants.Params.SOURCE to source
            )
            if (songDurationSeconds != null && songDurationSeconds > 0) {
                params[AnalyticsConstants.Params.DURATION_SECONDS] = songDurationSeconds
            }
            if (isCurrentlyPlaying != null) {
                params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING] = isCurrentlyPlaying
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.LIKED_SONG,
                params = params
            )
        }

        fun removedLikedSong(
            songId: String,
            songTitle: String,
            artist: String,
            source: String,
            songDurationSeconds: Long? = null,
            isCurrentlyPlaying: Boolean? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.SONG_ID to songId,
                AnalyticsConstants.Params.SONG_TITLE to songTitle,
                AnalyticsConstants.Params.ARTIST to artist,
                AnalyticsConstants.Params.SOURCE to source
            )
            if (songDurationSeconds != null && songDurationSeconds > 0) {
                params[AnalyticsConstants.Params.DURATION_SECONDS] = songDurationSeconds
            }
            if (isCurrentlyPlaying != null) {
                params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING] = isCurrentlyPlaying
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.REMOVED_LIKED_SONG,
                params = params
            )
        }

        fun songDeleted(
            songId: String,
            songTitle: String,
            artist: String,
            deleteSource: String,
            wasLiked: Boolean = false,
            playlistCount: Int = 0,
            songDurationSeconds: Long? = null,
            isCurrentlyPlaying: Boolean? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.SONG_ID to songId,
                AnalyticsConstants.Params.SONG_TITLE to songTitle,
                AnalyticsConstants.Params.ARTIST to artist,
                AnalyticsConstants.Params.DELETE_SOURCE to deleteSource,
                AnalyticsConstants.Params.WAS_LIKED to wasLiked,
                AnalyticsConstants.Params.PLAYLIST_COUNT to playlistCount
            )
            if (songDurationSeconds != null && songDurationSeconds > 0) {
                params[AnalyticsConstants.Params.DURATION_SECONDS] = songDurationSeconds
            }
            if (isCurrentlyPlaying != null) {
                params[AnalyticsConstants.Params.IS_CURRENTLY_PLAYING] = isCurrentlyPlaying
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SONG_DELETED,
                params = params
            )
        }

        fun songDeletedBatch(
            songsCount: Int,
            deleteSource: String
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SONG_DELETED_BATCH,
                params = mapOf(
                    AnalyticsConstants.Params.SONGS_COUNT to songsCount,
                    AnalyticsConstants.Params.DELETE_SOURCE to deleteSource
                )
            )
        }

        // Import Performance Events (Phase 14.1)
        fun shareImportStarted(source: String = "youtube_share"): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.SHARE_IMPORT_STARTED,
                params = mapOf(
                    AnalyticsConstants.Params.SOURCE to source
                )
            )
        }

        fun addToLibraryClicked(sessionId: String, source: String = "youtube"): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.ADD_TO_LIBRARY_CLICKED,
                params = mapOf(
                    AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.IMPORT_SOURCE to source
                )
            )
        }

        fun metadataExtractionStarted(sessionId: String, videoId: String): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.METADATA_EXTRACTION_STARTED,
                params = mapOf(
                    AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.SONG_ID to videoId
                )
            )
        }

        fun metadataExtractionCompleted(
            sessionId: String,
            videoId: String,
            metadataDurationMs: Long,
            songTitle: String,
            artist: String
        ): AnalyticsEvent {
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.METADATA_EXTRACTION_COMPLETED,
                params = mapOf(
                    AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                    AnalyticsConstants.Params.SONG_ID to videoId,
                    AnalyticsConstants.Params.METADATA_DURATION_MS to metadataDurationMs,
                    AnalyticsConstants.Params.SONG_TITLE to songTitle,
                    AnalyticsConstants.Params.ARTIST to artist
                )
            )
        }

        fun firstAudioCached(
            sessionId: String,
            videoId: String,
            cacheReadyDurationMs: Long,
            networkType: String? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                AnalyticsConstants.Params.SONG_ID to videoId,
                AnalyticsConstants.Params.CACHE_READY_DURATION_MS to cacheReadyDurationMs
            )
            if (!networkType.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.NETWORK_TYPE] = networkType
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.FIRST_AUDIO_CACHED,
                params = params
            )
        }

        fun playbackReady(
            sessionId: String,
            songId: String,
            timeToMusicMs: Long,
            networkType: String? = null,
            playbackStartedFrom: String = "import"
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                AnalyticsConstants.Params.SONG_ID to songId,
                AnalyticsConstants.Params.TIME_TO_MUSIC_MS to timeToMusicMs,
                AnalyticsConstants.Params.PLAYBACK_STARTED_FROM to playbackStartedFrom
            )
            if (!networkType.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.NETWORK_TYPE] = networkType
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.PLAYBACK_READY,
                params = params
            )
        }

        fun importFailedWithStage(
            sessionId: String,
            failureReason: String,
            failureStage: String,
            source: String = "youtube",
            networkType: String? = null
        ): AnalyticsEvent {
            val params = mutableMapOf<String, Any>(
                AnalyticsConstants.Params.IMPORT_SESSION_ID to sessionId,
                AnalyticsConstants.Params.IMPORT_SOURCE to source,
                AnalyticsConstants.Params.FAILURE_REASON to failureReason,
                AnalyticsConstants.Params.FAILURE_STAGE to failureStage
            )
            if (!networkType.isNullOrEmpty()) {
                params[AnalyticsConstants.Params.NETWORK_TYPE] = networkType
            }
            return AnalyticsEvent(
                name = AnalyticsConstants.Events.IMPORT_FAILED,
                params = params
            )
        }
    }
}
