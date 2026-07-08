package com.musyfy.nativeapp.domain.repository

import com.musyfy.nativeapp.domain.model.AuthState
import com.musyfy.nativeapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val authState: Flow<AuthState>
    val sessionUser: Flow<User?>
    val theme: Flow<String>
    suspend fun saveTheme(theme: String)
    suspend fun saveSession(user: User)
    suspend fun clearSession()
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String, newPassword: String): Result<Unit>
}
