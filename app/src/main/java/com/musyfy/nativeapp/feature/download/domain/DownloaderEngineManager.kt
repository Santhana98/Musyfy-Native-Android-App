package com.musyfy.nativeapp.feature.download.domain

import com.musyfy.nativeapp.feature.download.domain.model.EngineUpdateState
import com.musyfy.nativeapp.feature.download.domain.model.UpdateCheckResult
import kotlinx.coroutines.flow.StateFlow

interface DownloaderEngineManager {
    val engineState: StateFlow<EngineUpdateState>
    fun getCurrentEngineVersion(): String
    suspend fun checkForUpdates(force: Boolean = false): UpdateCheckResult
    suspend fun updateEngine(triggerSource: String = "manual"): Result<Unit>
    suspend fun rollback(reason: String): Result<Unit>
    suspend fun validateEngine(): Boolean
    suspend fun recoverOnStartup()
    fun isUpdateInProgress(): Boolean
}
