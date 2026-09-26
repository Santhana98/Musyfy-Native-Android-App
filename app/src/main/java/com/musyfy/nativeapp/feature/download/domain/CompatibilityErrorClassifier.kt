package com.musyfy.nativeapp.feature.download.domain

enum class CompatibilityFailureType {
    COMPATIBILITY_FAILURE,
    NETWORK_FAILURE,
    STORAGE_FAILURE,
    CONTENT_UNAVAILABLE,
    NOT_COMPATIBILITY_FAILURE,
    UNKNOWN
}

object CompatibilityErrorClassifier {

    /**
     * Categorizes an error message or exception thrown during metadata extraction or audio download.
     * Strictly returns [CompatibilityFailureType.COMPATIBILITY_FAILURE] ONLY for genuine
     * YouTube / yt-dlp compatibility regressions (HTTP 403, 429, bot checks, PO Token, SABR, n-sig, etc.).
     * Does NOT treat generic extraction errors, network timeouts, or private videos as compatibility errors.
     */
    fun classify(rawMessage: String?): CompatibilityFailureType {
        if (rawMessage.isNullOrBlank()) return CompatibilityFailureType.NOT_COMPATIBILITY_FAILURE
        val fullMsg = rawMessage.lowercase()

        // 1. Explicitly check for Network Failures (Must NOT trigger engine update)
        if (fullMsg.contains("unknownhostexception") ||
            fullMsg.contains("sockettimeoutexception") ||
            fullMsg.contains("connectexception") ||
            fullMsg.contains("noroutetohostexception") ||
            fullMsg.contains("failed to connect") ||
            fullMsg.contains("no address associated with hostname") ||
            fullMsg.contains("connection refused") ||
            fullMsg.contains("software caused connection abort") ||
            fullMsg.contains("network is unreachable") ||
            fullMsg.contains("timed out") ||
            fullMsg.contains("read timeout") ||
            fullMsg.contains("connection timeout") ||
            (fullMsg.contains("sslhandshakeexception") && fullMsg.contains("timeout"))
        ) {
            return CompatibilityFailureType.NETWORK_FAILURE
        }

        // 2. Storage / Disk Failures (Must NOT trigger engine update)
        if (fullMsg.contains("enospc") ||
            fullMsg.contains("no space left on device") ||
            fullMsg.contains("insufficient storage") ||
            fullMsg.contains("disk full")
        ) {
            return CompatibilityFailureType.STORAGE_FAILURE
        }

        // 3. Video-Specific / User Input Issues (Must NOT trigger engine update)
        if (fullMsg.contains("this video is unavailable") ||
            fullMsg.contains("video unavailable") ||
            fullMsg.contains("private video") ||
            fullMsg.contains("this video has been removed") ||
            fullMsg.contains("account associated with this video has been terminated") ||
            fullMsg.contains("copyright infringement") ||
            fullMsg.contains("members-only") ||
            fullMsg.contains("invalid youtube url") ||
            fullMsg.contains("failed to parse video id") ||
            fullMsg.contains("sign in if you've been granted access")
        ) {
            return CompatibilityFailureType.CONTENT_UNAVAILABLE
        }

        // 4. Narrow YouTube Innertube / yt-dlp Compatibility Failure Patterns
        // Strictly matched to known YouTube protocol and player changes
        if (fullMsg.contains("http error 429") ||
            fullMsg.contains("429: too many requests") ||
            fullMsg.contains("http error 403") ||
            fullMsg.contains("403: forbidden") ||
            fullMsg.contains("sign in to confirm you’re not a bot") ||
            fullMsg.contains("sign in to confirm you're not a bot") ||
            fullMsg.contains("confirm you're not a bot") ||
            fullMsg.contains("confirm you’re not a bot") ||
            fullMsg.contains("bot verification") ||
            fullMsg.contains("missing required visitor data") ||
            fullMsg.contains("visitor_data") ||
            fullMsg.contains("gvs po token") ||
            fullMsg.contains("po_token") ||
            fullMsg.contains("po token") ||
            fullMsg.contains("sabr-only") ||
            fullMsg.contains("sabr streaming") ||
            fullMsg.contains("n-sig calculation error") ||
            fullMsg.contains("signature decipher") ||
            fullMsg.contains("player response missing signature") ||
            (fullMsg.contains("unsupported client") && fullMsg.contains("youtube")) ||
            (fullMsg.contains("player_client") && fullMsg.contains("extractor"))
        ) {
            return CompatibilityFailureType.COMPATIBILITY_FAILURE
        }

        return CompatibilityFailureType.UNKNOWN
    }

    fun classify(throwable: Throwable?): CompatibilityFailureType {
        if (throwable == null) return CompatibilityFailureType.NOT_COMPATIBILITY_FAILURE

        if (throwable is java.util.concurrent.CancellationException ||
            throwable is kotlinx.coroutines.CancellationException
        ) {
            return CompatibilityFailureType.NOT_COMPATIBILITY_FAILURE
        }

        // Collect all error messages across the cause chain
        val messageBuilder = StringBuilder()
        var curr: Throwable? = throwable
        while (curr != null) {
            val msg = curr.message
            if (!msg.isNullOrEmpty()) {
                messageBuilder.append(" ").append(msg)
            }
            val className = curr.javaClass.name
            messageBuilder.append(" ").append(className)
            curr = curr.cause
        }
        return classify(messageBuilder.toString())
    }

    /**
     * Returns true if the error represents a YouTube / yt-dlp compatibility failure
     * that could be resolved by updating the yt-dlp runtime engine.
     */
    fun isCompatibilityFailure(throwable: Throwable?): Boolean {
        return classify(throwable) == CompatibilityFailureType.COMPATIBILITY_FAILURE
    }

    fun isCompatibilityFailure(message: String?): Boolean {
        return classify(message) == CompatibilityFailureType.COMPATIBILITY_FAILURE
    }
}
