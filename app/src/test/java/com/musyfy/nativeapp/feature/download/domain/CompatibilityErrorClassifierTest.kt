package com.musyfy.nativeapp.feature.download.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CompatibilityErrorClassifierTest {

    @Test
    fun testClassify_http403Forbidden_classifiedAsCompatibilityFailure() {
        val message = "ERROR: [youtube] jNQXAC9IVRw: YouTube said: HTTP Error 403: Forbidden"
        val result = CompatibilityErrorClassifier.classify(message)
        assertEquals(CompatibilityFailureType.COMPATIBILITY_FAILURE, result)
    }

    @Test
    fun testClassify_http429TooManyRequests_classifiedAsCompatibilityFailure() {
        val message = "ERROR: [youtube] dQw4w9WgXcQ: HTTP Error 429: Too Many Requests"
        val result = CompatibilityErrorClassifier.classify(message)
        assertEquals(CompatibilityFailureType.COMPATIBILITY_FAILURE, result)
    }

    @Test
    fun testClassify_botConfirmationChallenge_classifiedAsCompatibilityFailure() {
        val message = "Sign in to confirm you’re not a bot. This helps protect our community."
        val result = CompatibilityErrorClassifier.classify(message)
        assertEquals(CompatibilityFailureType.COMPATIBILITY_FAILURE, result)
    }

    @Test
    fun testClassify_poTokenVisitorDataSabr_classifiedAsCompatibilityFailure() {
        val samples = listOf(
            "Missing visitor_data or po_token for streaming player",
            "SABR streaming protocol error: player response missing signature",
            "n-sig calculation error: decipher string failed",
            "Unable to extract uploader id from player response",
            "requested format is not available"
        )
        for (sample in samples) {
            val result = CompatibilityErrorClassifier.classify(sample)
            assertEquals("Failed for sample: $sample", CompatibilityFailureType.COMPATIBILITY_FAILURE, result)
        }
    }

    @Test
    fun testClassify_networkExceptions_classifiedAsNetworkFailure() {
        val timeoutResult = CompatibilityErrorClassifier.classify(SocketTimeoutException("Read timed out"))
        assertEquals(CompatibilityFailureType.NETWORK_FAILURE, timeoutResult)

        val hostResult = CompatibilityErrorClassifier.classify(UnknownHostException("Unable to resolve host"))
        assertEquals(CompatibilityFailureType.NETWORK_FAILURE, hostResult)

        val netMsgResult = CompatibilityErrorClassifier.classify("Failed to connect to host: network is unreachable")
        assertEquals(CompatibilityFailureType.NETWORK_FAILURE, netMsgResult)
    }

    @Test
    fun testClassify_storageExceptions_classifiedAsStorageFailure() {
        val storageSamples = listOf(
            "write failed: ENOSPC (No space left on device)",
            "java.io.IOException: disk full while writing chunk",
            "insufficient storage space to complete file operation"
        )
        for (sample in storageSamples) {
            val result = CompatibilityErrorClassifier.classify(sample)
            assertEquals("Failed for sample: $sample", CompatibilityFailureType.STORAGE_FAILURE, result)
        }
    }

    @Test
    fun testClassify_contentUnavailable_classifiedAsContentUnavailable() {
        val contentSamples = listOf(
            "ERROR: [youtube] Private video. Sign in if you've been granted access to this video",
            "ERROR: [youtube] This video is unavailable",
            "This video is available to this channel's members only"
        )
        for (sample in contentSamples) {
            val result = CompatibilityErrorClassifier.classify(sample)
            assertEquals("Failed for sample: $sample", CompatibilityFailureType.CONTENT_UNAVAILABLE, result)
        }
    }

    @Test
    fun testClassify_unknownError_classifiedAsUnknown() {
        val result = CompatibilityErrorClassifier.classify(IllegalStateException("Arbitrary unexpected exception"))
        assertEquals(CompatibilityFailureType.UNKNOWN, result)
    }

    @Test
    fun testClassify_genericExtractionErrors_notClassifiedAsCompatibilityFailure() {
        // Generic RegexNotFoundError or generic extraction without YouTube-specific innertube indicators
        val result1 = CompatibilityErrorClassifier.classify("RegexNotFoundError: Unable to extract title")
        assertEquals(CompatibilityFailureType.UNKNOWN, result1)

        val result2 = CompatibilityErrorClassifier.classify("Failed to parse JSON response: unexpected end of stream")
        assertEquals(CompatibilityFailureType.UNKNOWN, result2)
    }

    @Test
    fun testClassify_dnsFailure_classifiedAsNetworkFailure() {
        val result = CompatibilityErrorClassifier.classify("java.net.UnknownHostException: No address associated with hostname www.youtube.com")
        assertEquals(CompatibilityFailureType.NETWORK_FAILURE, result)
    }

    @Test
    fun testClassify_cancellation_classifiedAsNotCompatibilityFailure() {
        val result = CompatibilityErrorClassifier.classify(kotlinx.coroutines.CancellationException("Job cancelled"))
        assertEquals(CompatibilityFailureType.NOT_COMPATIBILITY_FAILURE, result)
    }
}
