package com.musyfy.nativeapp.core.validation

/**
 * Shared validation constants across the application.
 */
object ValidationConstants {
    const val MAX_PLAYLIST_NAME_LENGTH = 50
    const val MAX_YOUTUBE_URL_LENGTH = 2048
    const val MAX_SEARCH_QUERY_LENGTH = 200

    val TRUSTED_YOUTUBE_HOSTS = setOf(
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be"
    )
}
