package com.musyfy.nativeapp.core.protection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production-grade conflated action guard ("last selected value wins").
 * Cancels any previous in-flight task for the target domain and processes only the latest requested action.
 */
@Singleton
class ConflatedActionGuard @Inject constructor() {
    private val activeJob = AtomicReference<Job?>(null)

    fun launchConflated(scope: CoroutineScope, block: suspend () -> Unit) {
        val previous = activeJob.getAndSet(
            scope.launch {
                block()
            }
        )
        previous?.cancel()
    }
}
