package com.musyfy.nativeapp.core.validation

import java.net.URI
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-grade centralized Input Validator.
 * Provides URI-parsed URL validation, Unicode-safe sanitization, and structured validation results.
 */
@Singleton
class InputValidator @Inject constructor() {

    companion object {
        private val CONTROL_CHARS_PATTERN = Pattern.compile("\\p{C}+")
        private val VIDEO_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{11}$")
    }

    /**
     * Sanitizes input by stripping non-printable control characters (\p{C}+) and trimming
     * leading/trailing whitespace, while preserving all valid Unicode, emoji, and non-Latin scripts.
     */
    fun sanitize(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val sanitized = CONTROL_CHARS_PATTERN.matcher(input).replaceAll("")
        return sanitized.trim()
    }

    /**
     * Validates playlist names for creation and renaming.
     */
    fun validatePlaylistName(name: String?): ValidationResult {
        val sanitized = sanitize(name)
        if (sanitized.isEmpty()) {
            return ValidationResult.Error(
                type = ValidationErrorType.EMPTY,
                message = "Playlist name cannot be empty"
            )
        }
        if (sanitized.length > ValidationConstants.MAX_PLAYLIST_NAME_LENGTH) {
            return ValidationResult.Error(
                type = ValidationErrorType.TOO_LONG,
                message = "Playlist name cannot exceed ${ValidationConstants.MAX_PLAYLIST_NAME_LENGTH} characters"
            )
        }
        return ValidationResult.Success(sanitized)
    }

    /**
     * Validates YouTube URLs using URI parsing and trusted host verification.
     */
    fun validateYoutubeUrl(url: String?): ValidationResult {
        if (url.isNullOrBlank()) {
            return ValidationResult.Error(
                type = ValidationErrorType.EMPTY,
                message = "YouTube URL cannot be empty"
            )
        }
        val trimmed = url.trim()
        if (trimmed.length > ValidationConstants.MAX_YOUTUBE_URL_LENGTH) {
            return ValidationResult.Error(
                type = ValidationErrorType.TOO_LONG,
                message = "URL exceeds maximum length of ${ValidationConstants.MAX_YOUTUBE_URL_LENGTH} characters"
            )
        }

        // Direct 11-character Video ID check
        if (VIDEO_ID_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.Success(trimmed)
        }

        return try {
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase()
            if (scheme != null && scheme != "http" && scheme != "https") {
                return ValidationResult.Error(
                    type = ValidationErrorType.INVALID_FORMAT,
                    message = "URL must use HTTP or HTTPS protocol"
                )
            }

            val host = uri.host?.lowercase()
            if (host == null || !ValidationConstants.TRUSTED_YOUTUBE_HOSTS.contains(host)) {
                return ValidationResult.Error(
                    type = ValidationErrorType.UNTRUSTED_HOST,
                    message = "URL host is not a trusted YouTube domain"
                )
            }

            ValidationResult.Success(trimmed)
        } catch (e: Exception) {
            ValidationResult.Error(
                type = ValidationErrorType.INVALID_FORMAT,
                message = "Malformed URL format"
            )
        }
    }

    /**
     * Validates search query input.
     * Empty search string is treated as a valid idle state (Success("")).
     */
    fun validateSearchQuery(query: String?): ValidationResult {
        val sanitized = sanitize(query)
        if (sanitized.isEmpty()) {
            return ValidationResult.Success("") // Valid idle state
        }
        if (sanitized.length > ValidationConstants.MAX_SEARCH_QUERY_LENGTH) {
            return ValidationResult.Error(
                type = ValidationErrorType.TOO_LONG,
                message = "Search query exceeds maximum length of ${ValidationConstants.MAX_SEARCH_QUERY_LENGTH} characters"
            )
        }
        return ValidationResult.Success(sanitized)
    }

    /**
     * Validates string IDs (e.g. songId, playlistId) before repository actions.
     */
    fun validateId(id: String?): Boolean {
        return !id.isNullOrBlank()
    }

    /**
     * Validates index bounds for collection access.
     */
    fun isValidIndex(index: Int, size: Int): Boolean {
        return size > 0 && index in 0 until size
    }
}
