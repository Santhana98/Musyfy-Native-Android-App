package com.musyfy.nativeapp.core.protection

import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-grade operation-specific lock guard.
 * Isolates lock scoping by operation key so independent actions (e.g. create vs rename vs reorder)
 * do not block each other while duplicate taps on the same operation are cleanly ignored.
 */
@Singleton
class OperationProtector @Inject constructor() {
    private val operationMutexes = ConcurrentHashMap<String, Mutex>()

    /**
     * Executes [action] under a key-isolated lock.
     * Returns true if execution occurred, false if locked by a concurrent identical operation.
     */
    suspend fun withOperationLock(operationKey: String, action: suspend () -> Unit): Boolean {
        val mutex = operationMutexes.computeIfAbsent(operationKey) { Mutex() }
        if (mutex.isLocked) {
            return false
        }
        if (!mutex.tryLock()) {
            return false
        }
        return try {
            action()
            true
        } finally {
            mutex.unlock()
            operationMutexes.remove(operationKey)
        }
    }
}
