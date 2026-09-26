package com.musyfy.nativeapp.feature.download.domain.model

sealed interface EngineUpdateState {
    data class Idle(val currentVersion: String) : EngineUpdateState
    data class Checking(val currentVersion: String) : EngineUpdateState
    data class UpdateAvailable(val currentVersion: String, val latestVersion: String) : EngineUpdateState
    data class UpToDate(val currentVersion: String) : EngineUpdateState
    data class Preparing(val currentVersion: String) : EngineUpdateState
    data class BackingUp(val currentVersion: String) : EngineUpdateState
    data class Updating(val currentVersion: String, val targetVersion: String) : EngineUpdateState
    data class Validating(val targetVersion: String) : EngineUpdateState
    data class Success(val newVersion: String) : EngineUpdateState
    data class Failed(
        val currentVersion: String,
        val failureReason: String,
        val category: EngineFailureCategory,
        val rolledBack: Boolean
    ) : EngineUpdateState
    data class RollingBack(val currentVersion: String) : EngineUpdateState
    data class Restored(val currentVersion: String) : EngineUpdateState
    data class InsufficientStorage(val availableBytes: Long, val requiredBytes: Long) : EngineUpdateState
    object Recovering : EngineUpdateState
}

enum class EngineFailureCategory {
    NETWORK_ERROR,
    GITHUB_RATE_LIMIT,
    STORAGE_ERROR,
    INSUFFICIENT_STORAGE,
    BACKUP_FAILED,
    INSTALL_FAILED,
    ENGINE_VALIDATION_FAILED,
    HEALTH_CHECK_NETWORK_FAILED,
    HEALTH_CHECK_COMPATIBILITY_FAILED,
    ROLLBACK_FAILED,
    NO_UPDATE_AVAILABLE,
    ALREADY_UP_TO_DATE,
    USER_CANCELLED,
    UNKNOWN_ERROR
}

sealed interface UpdateCheckResult {
    data class Available(val currentVersion: String, val latestVersion: String) : UpdateCheckResult
    data class Current(val currentVersion: String) : UpdateCheckResult
    data class RateLimited(val message: String) : UpdateCheckResult
    data class NetworkError(val message: String) : UpdateCheckResult
    data class Error(val message: String, val category: EngineFailureCategory) : UpdateCheckResult
}
