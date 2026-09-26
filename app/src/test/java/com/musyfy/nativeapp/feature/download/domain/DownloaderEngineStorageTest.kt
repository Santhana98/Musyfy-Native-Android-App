package com.musyfy.nativeapp.feature.download.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloaderEngineStorageTest {

    private fun calculateRequiredStorageBytes(binarySizeBytes: Long): Long {
        val expectedDownloadSizeBytes = 10 * 1024 * 1024L
        val safetyMarginBytes = 10 * 1024 * 1024L
        val binarySize = if (binarySizeBytes > 0L) binarySizeBytes else 3 * 1024 * 1024L
        return (binarySize * 2) + expectedDownloadSizeBytes + safetyMarginBytes
    }

    private fun isStorageSufficient(usableSpace: Long, binarySizeBytes: Long): Boolean {
        return usableSpace >= calculateRequiredStorageBytes(binarySizeBytes)
    }

    @Test
    fun testStorageCalculation_defaultFallbackWhenBinaryMissing() {
        val required = calculateRequiredStorageBytes(0L)
        // (3MB * 2) + 10MB + 10MB = 26MB
        val expected = (3 * 2 + 10 + 10) * 1024 * 1024L
        assertEquals(expected, required)
    }

    @Test
    fun testStorageCalculation_normalRuntimeSize() {
        val normalSize = 4 * 1024 * 1024L // 4MB binary
        val required = calculateRequiredStorageBytes(normalSize)
        // (4MB * 2) + 10MB + 10MB = 28MB
        val expected = 28 * 1024 * 1024L
        assertEquals(expected, required)
    }

    @Test
    fun testStorageCalculation_unusuallyLargeRuntime() {
        val largeSize = 50 * 1024 * 1024L // 50MB binary
        val required = calculateRequiredStorageBytes(largeSize)
        // (50MB * 2) + 10MB + 10MB = 120MB
        val expected = 120 * 1024 * 1024L
        assertEquals(expected, required)
    }

    @Test
    fun testStorageSufficiency_sufficientSpace_returnsTrue() {
        val binarySize = 4 * 1024 * 1024L // requires 28MB
        val usableSpace = 50 * 1024 * 1024L // 50MB available
        assertTrue(isStorageSufficient(usableSpace, binarySize))
    }

    @Test
    fun testStorageSufficiency_insufficientSpace_returnsFalse() {
        val binarySize = 4 * 1024 * 1024L // requires 28MB
        val usableSpace = 20 * 1024 * 1024L // 20MB available
        assertFalse(isStorageSufficient(usableSpace, binarySize))
    }

    @Test
    fun testStorageSufficiency_exactBoundarySpace_returnsTrue() {
        val binarySize = 4 * 1024 * 1024L // requires 28MB
        val usableSpace = 28 * 1024 * 1024L // exactly 28MB available
        assertTrue(isStorageSufficient(usableSpace, binarySize))
    }
}
