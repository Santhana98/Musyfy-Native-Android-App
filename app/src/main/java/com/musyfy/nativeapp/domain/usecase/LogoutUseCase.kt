package com.musyfy.nativeapp.domain.usecase

import com.musyfy.nativeapp.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.logout()
    }
}
