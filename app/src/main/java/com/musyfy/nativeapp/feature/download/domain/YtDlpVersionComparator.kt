package com.musyfy.nativeapp.feature.download.domain

object YtDlpVersionComparator {

    private fun cleanVersionString(v: String): String {
        return v.trim()
            .removePrefix("v")
            .removePrefix("V")
            .removePrefix("yt-dlp")
            .removePrefix("_")
            .trim()
    }

    /**
     * Checks if a version string is a plausible release version (starts with 4-digit year >= 2020).
     */
    fun isValidReleaseVersion(v: String): Boolean {
        val s = cleanVersionString(v)
        val firstPart = s.split(".", "_", "-").firstOrNull() ?: return false
        val year = firstPart.toLongOrNull() ?: return false
        return year in 2020..2099
    }

    /**
     * Compares two yt-dlp versions (e.g. "2026.08.19" vs "2026.09.01" or "2026.9.1" vs "2026.10.1").
     * Returns:
     *  > 0 if v1 > v2
     *  < 0 if v1 < v2
     *  = 0 if v1 == v2
     */
    fun compare(v1: String, v2: String): Int {
        val s1 = cleanVersionString(v1)
        val s2 = cleanVersionString(v2)

        if (s1.isEmpty() && s2.isEmpty()) return 0
        if (s1.isEmpty()) return -1
        if (s2.isEmpty()) return 1
        if (s1 == s2) return 0

        val parts1 = s1.split(".", "_", "-")
        val parts2 = s2.split(".", "_", "-")

        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val p1 = parts1.getOrNull(i)
            val p2 = parts2.getOrNull(i)

            if (p1 == null && p2 == null) continue
            if (p1 == null) return -1
            if (p2 == null) return 1

            val num1 = p1.toLongOrNull()
            val num2 = p2.toLongOrNull()

            if (num1 != null && num2 != null) {
                if (num1 != num2) {
                    return num1.compareTo(num2)
                }
            } else if (num1 != null) {
                // Numeric release takes precedence over arbitrary non-numeric text
                return 1
            } else if (num2 != null) {
                // Non-numeric candidate is NOT newer than a valid numeric release
                return -1
            } else {
                val strCmp = p1.compareTo(p2, ignoreCase = true)
                if (strCmp != 0) return strCmp
            }
        }
        return 0
    }

    fun isNewer(candidate: String, current: String): Boolean {
        // Enforce that candidate is a valid release version to avoid unsafe updates/downgrades
        if (!isValidReleaseVersion(candidate)) {
            return false
        }
        if (!isValidReleaseVersion(current)) {
            return true
        }
        return compare(candidate, current) > 0
    }
}
