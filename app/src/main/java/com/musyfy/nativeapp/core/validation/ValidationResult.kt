package com.musyfy.nativeapp.core.validation

/**
 * Structured validation result interface.
 */
sealed interface ValidationResult {
    data class Success(val sanitizedInput: String) : ValidationResult
    data class Error(val type: ValidationErrorType, val message: String) : ValidationResult

    val isSuccess: Boolean get() = this is Success
}
