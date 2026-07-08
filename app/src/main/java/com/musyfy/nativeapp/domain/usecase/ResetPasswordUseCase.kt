package com.musyfy.nativeapp.domain.usecase

import com.musyfy.nativeapp.domain.repository.UserRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, newPassword: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return userRepository.resetPassword(email.trim(), newPassword)
    }
}
