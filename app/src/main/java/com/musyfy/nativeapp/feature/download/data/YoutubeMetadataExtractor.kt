package com.musyfy.nativeapp.feature.download.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.util.regex.Pattern

data class YoutubeVideoInfo(
    val id: String,
    val title: String,
    val uploader: String?,
    val duration: Long?,
    val thumbnail: String?
)

object YoutubeMetadataExtractor {

    private const val TAG = "YoutubeMetadataExtractor"

    fun extractVideoId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            Log.d(TAG, "extractVideoId: input is empty")
            return null
        }
        val patterns = listOf(
            Pattern.compile("https?://(?:(?:www|m|music)\\.)?youtube\\.com/watch\\?(?:[^\\s]*&)?v=([a-zA-Z0-9_-]{11})"),
            Pattern.compile("https?://youtu\\.be/([a-zA-Z0-9_-]{11})"),
            Pattern.compile("https?://(?:(?:www|m|music)\\.)?youtube\\.com/shorts/([a-zA-Z0-9_-]{11})"),
            Pattern.compile("https?://(?:(?:www|m|music)\\.)?youtube\\.com/embed/([a-zA-Z0-9_-]{11})"),
            Pattern.compile("https?://(?:(?:www|m|music)\\.)?youtube\\.com/live/([a-zA-Z0-9_-]{11})"),
            Pattern.compile("^([a-zA-Z0-9_-]{11})$")
        )
        for (pattern in patterns) {
            val matcher = pattern.matcher(trimmed)
            if (matcher.find()) {
                val id = matcher.group(1)
                Log.d(TAG, "extractVideoId: matched video ID: $id")
                return id
            }
        }
        Log.d(TAG, "extractVideoId: failed to match video ID pattern for input: $trimmed")
        return null
    }

    fun fetchVideoInfo(context: Context, url: String): YoutubeVideoInfo? {
        try {
            Log.d(TAG, "fetchVideoInfo: processing URL: $url")
            val videoId = extractVideoId(url)
            if (videoId == null) {
                Log.e(TAG, "fetchVideoInfo: failed to parse video ID from URL: $url")
                return null
            }
            
            val canonicalUrl = "https://www.youtube.com/watch?v=$videoId"
            Log.d(TAG, "fetchVideoInfo: Canonical YouTube Link: $canonicalUrl")
            
            val request = YoutubeDLRequest(canonicalUrl).apply {
                addOption("--dump-single-json")
                addOption("--no-download")
                addOption("--extractor-args", "youtube:player_client=android,web")
            }
            
            Log.d(TAG, "fetchVideoInfo: Executing yt-dlp metadata extraction...")
            val response = YoutubeDL.getInstance().execute(request)
            Log.d(TAG, "fetchVideoInfo: Execution finished. Exit code: ${response.exitCode}")
            
            if (response.exitCode == 0 && !response.out.isNullOrEmpty()) {
                val json = response.out
                Log.d(TAG, "fetchVideoInfo: Extracted JSON output length: ${json.length}")
                
                val gson = Gson()
                val info = gson.fromJson(json, YoutubeVideoInfo::class.java)
                Log.d(TAG, "fetchVideoInfo: Successfully decoded metadata object: $info")
                return info
            } else {
                Log.e(
                    TAG, 
                    "fetchVideoInfo: yt-dlp returned non-zero exit code or empty output. " +
                    "Exit code: ${response.exitCode}, out: ${response.out}, err: ${response.err}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchVideoInfo: Exception encountered during extraction pipeline", e)
        }
        return null
    }
}
