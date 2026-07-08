package com.musyfy.nativeapp.domain.usecase

import com.musyfy.nativeapp.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<Unit> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return userRepository.register(name.trim(), email.trim(), password)
    }
}
