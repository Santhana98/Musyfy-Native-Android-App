package com.musyfy.nativeapp.core.share

import com.musyfy.nativeapp.core.validation.InputValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShareTargetManagerTest {

    private lateinit var shareTargetManager: ShareTargetManager
    private val inputValidator = InputValidator()

    @Before
    fun setUp() {
        shareTargetManager = ShareTargetManager(inputValidator)
    }

    @Test
    fun extractYouTubeUrl_standardWatchUrl_returnsCanonical() {
        val input = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val result = shareTargetManager.extractYouTubeUrl(input)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result)
    }

    @Test
    fun extractYouTubeUrl_shortUrlWithParams_returnsCanonical() {
        val input = "https://youtu.be/dQw4w9WgXcQ?si=abcdef12345"
        val result = shareTargetManager.extractYouTubeUrl(input)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result)
    }

    @Test
    fun extractYouTubeUrl_mobileYoutubeUrl_returnsCanonical() {
        val input = "https://m.youtube.com/watch?v=dQw4w9WgXcQ"
        val result = shareTargetManager.extractYouTubeUrl(input)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result)
    }

    @Test
    fun extractYouTubeUrl_youtubeMusicUrl_returnsCanonical() {
        val input = "https://music.youtube.com/watch?v=dQw4w9WgXcQ"
        val result = shareTargetManager.extractYouTubeUrl(input)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result)
    }

    @Test
    fun extractYouTubeUrl_youtubeShortsUrl_returnsCanonical() {
        val input = "https://youtube.com/shorts/dQw4w9WgXcQ?feature=share"
        val result = shareTargetManager.extractYouTubeUrl(input)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result)
    }

    @Test
    fun extractYouTubeUrl_sharedTextWithTitleAndUrl_returnsCanonical() {
        val input = "Watch 'Never Gonna Give You Up' on YouTube: https://youtu.be/dQw4w9WgXcQ"
        val result = shareTargetManager.extractYouTubeUrl(input)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", result)
    }

    @Test
    fun extractYouTubeUrl_nonYoutubeArbitraryText_returnsNull() {
        val input = "Check out this recipe: https://example.com/recipe"
        val result = shareTargetManager.extractYouTubeUrl(input)
        assertNull(result)
    }

    @Test
    fun extractYouTubeUrl_nullOrBlank_returnsNull() {
        assertNull(shareTargetManager.extractYouTubeUrl(null))
        assertNull(shareTargetManager.extractYouTubeUrl(""))
        assertNull(shareTargetManager.extractYouTubeUrl("   "))
    }

    @Test
    fun setPendingUrl_and_consumePendingUrl_atomicSingleConsumption() {
        val input = "https://youtu.be/dQw4w9WgXcQ"
        val accepted = shareTargetManager.setPendingUrl(input)
        assertTrue(accepted)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", shareTargetManager.pendingShareUrl.value)

        // First consume returns the canonical URL
        val firstConsume = shareTargetManager.consumePendingUrl()
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", firstConsume)

        // Second consume returns null (state consumed)
        val secondConsume = shareTargetManager.consumePendingUrl()
        assertNull(secondConsume)
        assertNull(shareTargetManager.pendingShareUrl.value)
    }

    @Test
    fun clear_removesPendingUrl() {
        shareTargetManager.setPendingUrl("https://youtu.be/dQw4w9WgXcQ")
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", shareTargetManager.pendingShareUrl.value)
        shareTargetManager.clear()
        assertNull(shareTargetManager.pendingShareUrl.value)
        assertNull(shareTargetManager.consumePendingUrl())
    }
}
