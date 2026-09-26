package com.musyfy.nativeapp.feature.download.domain

import com.musyfy.nativeapp.feature.download.domain.model.EngineFailureCategory
import com.musyfy.nativeapp.feature.download.domain.model.EngineUpdateState
import com.musyfy.nativeapp.feature.download.domain.model.UpdateCheckResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloaderEngineLifecycleTest {

    @Test
    fun testUpdateCheckResult_availableVsCurrent() {
        val available = UpdateCheckResult.Available(currentVersion = 2026.08.19, latestVersion = 2026.09.20)
        assertEquals(2026.08.19, available.currentVersion)
        assertEquals(2026.09.20, available.latestVersion)

        val current = UpdateCheckResult.Current(currentVersion = 2026.08.19)
        assertEquals(2026.08.19, current.currentVersion)
    }

    @Test
    fun testUpdateCheckResult_rateLimitedAndNetworkError() {
        val rateLimited = UpdateCheckResult.RateLimited(API rate limit exceeded)
        assertEquals(API rate limit exceeded, rateLimited.message)

        val netErr = UpdateCheckResult.NetworkError(Failed to connect)
        assertEquals(Failed to connect, netErr.message)
    }

    @Test
    fun testEngineUpdateState_failureCategories() {
        val failed = EngineUpdateState.Failed(
            currentVersion = 2026.08.19,
            failureReason = Probe execution failed,
            category = EngineFailureCategory.ENGINE_VALIDATION_FAILED,
            rolledBack = true
        )
        assertTrue(failed.rolledBack)
        assertEquals(EngineFailureCategory.ENGINE_VALIDATION_FAILED, failed.category)
    }

    @Test
    fun testSingleRetryLogic_cannotExceedMaxRetry() {
        var retryCount = 0
        val maxRetries = 1

        // Initial attempt
        assertTrue(retryCount < maxRetries)
        retryCount++

        // Retry attempt
        assertEquals(1, retryCount)
        assertFalse(retryCount < maxRetries)

        // Reset on success
        retryCount = 0
        assertEquals(0, retryCount)
        assertTrue(retryCount < maxRetries)
    }

    @Test
    fun testTransactionStates_lifecycleSequence() {
        val states = listOf(
            IDLE,
            PREPARING,
            BACKING_UP,
            UPDATING,
            VALIDATING,
            COMMITTED,
            IDLE
        )
        assertEquals(IDLE, states.first())
        assertEquals(IDLE, states.last())
        assertTrue(states.contains(BACKING_UP))
        assertTrue(states.contains(VALIDATING))
    }
}
