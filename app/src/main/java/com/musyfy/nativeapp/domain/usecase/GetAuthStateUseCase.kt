package com.musyfy.nativeapp.domain.usecase

import com.musyfy.nativeapp.domain.model.AuthState
import com.musyfy.nativeapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<AuthState> {
        return userRepository.authState
    }
}
