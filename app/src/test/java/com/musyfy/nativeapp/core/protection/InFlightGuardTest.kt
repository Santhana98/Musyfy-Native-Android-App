package com.musyfy.nativeapp.core.protection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InFlightGuardTest {

    private val guard = InFlightGuard()

    @Test
    fun testRunIfKeyNotInFlight_allowsFirstExecutionAndClearsKey() = runTest {
        var count = 0
        val executed = guard.runIfKeyNotInFlight("key_1") {
            count++
        }
        assertTrue(executed)
        assertEquals(1, count)

        // Key is cleared after completion, so second execution succeeds
        val executed2 = guard.runIfKeyNotInFlight("key_1") {
            count++
        }
        assertTrue(executed2)
        assertEquals(2, count)
    }
}
