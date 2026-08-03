package com.musyfy.nativeapp.core.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    private val validator = InputValidator()

    @Test
    fun testSanitize_removesControlCharactersAndTrimsWhitespace() {
        val input = "  \u0000Hello \u0007World!  "
        val result = validator.sanitize(input)
        assertEquals("Hello World!", result)
    }

    @Test
    fun testSanitize_preservesUnicodeAndEmoji() {
        val unicodeInput = " ❤️ Favorites - தமிழ் Hits - Workout 💪 - 日本語 "
        val result = validator.sanitize(unicodeInput)
        assertEquals("❤️ Favorites - தமிழ் Hits - Workout 💪 - 日本語", result)
    }

    @Test
    fun testValidatePlaylistName_emptyOrWhitespace_returnsEmptyError() {
        val emptyResult = validator.validatePlaylistName("")
        assertTrue(emptyResult is ValidationResult.Error)
        assertEquals(ValidationErrorType.EMPTY, (emptyResult as ValidationResult.Error).type)

        val whitespaceResult = validator.validatePlaylistName("   \t\n  ")
        assertTrue(whitespaceResult is ValidationResult.Error)
        assertEquals(ValidationErrorType.EMPTY, (whitespaceResult as ValidationResult.Error).type)
    }

    @Test
    fun testValidatePlaylistName_tooLong_returnsTooLongError() {
        val longName = "A".repeat(51)
        val result = validator.validatePlaylistName(longName)
        assertTrue(result is ValidationResult.Error)
        assertEquals(ValidationErrorType.TOO_LONG, (result as ValidationResult.Error).type)
    }

    @Test
    fun testValidatePlaylistName_validUnicodeAndEmoji_returnsSuccess() {
        val validName = " ❤️ Favorites 🎵 "
        val result = validator.validatePlaylistName(validName)
        assertTrue(result is ValidationResult.Success)
        assertEquals("❤️ Favorites 🎵", (result as ValidationResult.Success).sanitizedInput)
    }

    @Test
    fun testValidateYoutubeUrl_validUrlsAndIds_returnsSuccess() {
        val validUrls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ",
            "dQw4w9WgXcQ"
        )
        for (url in validUrls) {
            val result = validator.validateYoutubeUrl(url)
            assertTrue("Failed for $url", result is ValidationResult.Success)
        }
    }

    @Test
    fun testValidateYoutubeUrl_untrustedHostOrMalformed_returnsError() {
        val invalidUrls = listOf(
            "https://google.com/search?q=youtube",
            "https://phishing-youtube.com/watch?v=12345678901",
            "ftp://youtube.com/watch?v=12345678901",
            "not_a_valid_url_or_id"
        )
        for (url in invalidUrls) {
            val result = validator.validateYoutubeUrl(url)
            assertTrue("Expected error for $url", result is ValidationResult.Error)
        }
    }

    @Test
    fun testValidateSearchQuery_empty_returnsValidIdleState() {
        val result = validator.validateSearchQuery("   ")
        assertTrue(result is ValidationResult.Success)
        assertEquals("", (result as ValidationResult.Success).sanitizedInput)
    }

    @Test
    fun testValidateSearchQuery_validUnicode_returnsSuccess() {
        val result = validator.validateSearchQuery("  தமிழ் Songs  ")
        assertTrue(result is ValidationResult.Success)
        assertEquals("தமிழ் Songs", (result as ValidationResult.Success).sanitizedInput)
    }

    @Test
    fun testValidateId_checksNullOrBlank() {
        assertFalse(validator.validateId(null))
        assertFalse(validator.validateId("  "))
        assertTrue(validator.validateId("song_123"))
    }

    @Test
    fun testIsValidIndex_checksBounds() {
        assertFalse(validator.isValidIndex(-1, 5))
        assertFalse(validator.isValidIndex(5, 5))
        assertFalse(validator.isValidIndex(0, 0))
        assertTrue(validator.isValidIndex(0, 5))
        assertTrue(validator.isValidIndex(4, 5))
    }
}
