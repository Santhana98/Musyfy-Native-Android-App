package com.musyfy.nativeapp.domain.repository

import com.musyfy.nativeapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val sessionUser: Flow<User?>
    suspend fun saveSession(user: User)
    suspend fun clearSession()
}
