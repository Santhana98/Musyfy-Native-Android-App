package com.musyfy.nativeapp.feature.download.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YtDlpVersionComparatorTest {

    @Test
    fun testIsNewer_whenCandidateIsNewerYear_returnsTrue() {
        assertTrue(YtDlpVersionComparator.isNewer("2026.01.01", "2025.12.31"))
    }

    @Test
    fun testIsNewer_whenCandidateIsNewerMonth_returnsTrue() {
        assertTrue(YtDlpVersionComparator.isNewer("2026.09.01", "2026.08.19"))
    }

    @Test
    fun testIsNewer_whenCandidateIsNewerDay_returnsTrue() {
        assertTrue(YtDlpVersionComparator.isNewer("2026.08.20", "2026.08.19"))
    }

    @Test
    fun testIsNewer_whenCandidateIsSame_returnsFalse() {
        assertFalse(YtDlpVersionComparator.isNewer("2026.08.19", "2026.08.19"))
    }

    @Test
    fun testIsNewer_whenCandidateIsOlder_returnsFalse() {
        assertFalse(YtDlpVersionComparator.isNewer("2026.08.18", "2026.08.19"))
        assertFalse(YtDlpVersionComparator.isNewer("2025.12.31", "2026.08.19"))
    }

    @Test
    fun testIsNewer_handlesPrefixVAndWhitespace() {
        assertTrue(YtDlpVersionComparator.isNewer(" v2026.09.25 ", "2026.08.19"))
    }

    @Test
    fun testIsNewer_whenCurrentIsUnknown_returnsTrueIfCandidateValid() {
        assertTrue(YtDlpVersionComparator.isNewer("2026.08.19", "unknown"))
        assertTrue(YtDlpVersionComparator.isNewer("2026.08.19", ""))
    }

    @Test
    fun testIsNewer_whenCandidateIsBlankOrMalformed_returnsFalse() {
        assertFalse(YtDlpVersionComparator.isNewer("", "2026.08.19"))
        assertFalse(YtDlpVersionComparator.isNewer("invalid.version", "2026.08.19"))
        assertFalse(YtDlpVersionComparator.isNewer("nightly", "2026.08.19"))
        assertFalse(YtDlpVersionComparator.isNewer("alpha_build_2", "2026.08.19"))
    }

    @Test
    fun testCompare_ordersChronologically() {
        assertTrue(YtDlpVersionComparator.compare("2026.09.20", "2026.08.19") > 0)
        assertEquals(0, YtDlpVersionComparator.compare("2026.08.19", "2026.08.19"))
        assertTrue(YtDlpVersionComparator.compare("2026.08.18", "2026.08.19") < 0)
    }
}
