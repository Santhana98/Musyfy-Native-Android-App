package com.musyfy.nativeapp.core.protection

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-grade in-flight action guard.
 * Prevents concurrent execution of identical key-based operations (e.g. song ID for likes, deletes).
 */
@Singleton
class InFlightGuard @Inject constructor() {
    private val inFlightKeys = ConcurrentHashMap.newKeySet<String>()

    /**
     * Executes [block] if [key] is not currently in-flight.
     * Returns true if execution completed, false if skipped due to duplicate in-flight request.
     */
    suspend fun runIfKeyNotInFlight(key: String, block: suspend () -> Unit): Boolean {
        if (!inFlightKeys.add(key)) {
            return false
        }
        return try {
            block()
            true
        } finally {
            inFlightKeys.remove(key)
        }
    }
}
