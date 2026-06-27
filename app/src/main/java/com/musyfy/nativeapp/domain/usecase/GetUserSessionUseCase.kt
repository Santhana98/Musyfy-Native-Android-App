package com.musyfy.nativeapp.domain.usecase

import com.musyfy.nativeapp.domain.model.User
import com.musyfy.nativeapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSessionUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> {
        return userRepository.sessionUser
    }
}
