package com.musyfy.nativeapp.core.share

import android.content.Intent
import com.musyfy.nativeapp.core.validation.InputValidator
import com.musyfy.nativeapp.feature.download.data.YoutubeMetadataExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-ready coordinator for Android Share Target (ACTION_SEND).
 * Safely extracts YouTube URLs from shared text, maintains pending state across Splash/Auth flows,
 * and provides atomic single-consumption to prevent duplicate imports on recompositions/rotations.
 */
@Singleton
class ShareTargetManager @Inject constructor(
    private val inputValidator: InputValidator
) {
    private val _pendingShareUrl = MutableStateFlow<String?>(null)
    val pendingShareUrl: StateFlow<String?> = _pendingShareUrl.asStateFlow()

    /**
     * Extracts and standardizes a YouTube canonical URL from any text input containing a YouTube link.
     */
    fun extractYouTubeUrl(rawText: String?): String? {
        if (rawText.isNullOrBlank()) return null
        val videoId = YoutubeMetadataExtractor.extractVideoId(rawText) ?: return null
        return "https://www.youtube.com/watch?v=$videoId"
    }

    /**
     * Inspects an incoming Android Intent and extracts any shared YouTube URL/text.
     * Returns true if a valid YouTube target was recognized and queued, false otherwise.
     */
    fun processIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        if (intent.action != Intent.ACTION_SEND) return false

        val mimeType = intent.type
        if (mimeType == null || (!mimeType.equals("text/plain", ignoreCase = true) && !mimeType.startsWith("text/"))) {
            return false
        }

        val rawText = try {
            intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
        } catch (e: Exception) {
            null
        } ?: return false

        val canonicalUrl = extractYouTubeUrl(rawText)
        if (canonicalUrl != null) {
            _pendingShareUrl.value = canonicalUrl
            return true
        }

        return false
    }

    /**
     * Sets the pending shared URL directly if valid.
     */
    fun setPendingUrl(url: String): Boolean {
        val canonicalUrl = extractYouTubeUrl(url)
        if (canonicalUrl != null) {
            _pendingShareUrl.value = canonicalUrl
            return true
        }
        return false
    }

    /**
     * Atomically consumes the pending share URL so that it is processed exactly once.
     */
    fun consumePendingUrl(): String? {
        return _pendingShareUrl.getAndUpdate { null }
    }

    /**
     * Explicitly clears any queued share URL.
     */
    fun clear() {
        _pendingShareUrl.value = null
    }
}
