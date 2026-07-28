package com.musyfy.nativeapp.data.repository

import com.musyfy.nativeapp.data.local.datastore.PreferencesManager
import com.musyfy.nativeapp.domain.model.AuthState
import com.musyfy.nativeapp.domain.model.User
import com.musyfy.nativeapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : UserRepository {

    override val authState: Flow<AuthState> = preferencesManager.authState

    override val sessionUser: Flow<User?> = combine(
        preferencesManager.userName,
        preferencesManager.userEmail,
        preferencesManager.sessionToken
    ) { name, email, token ->
        if (name != null && email != null) {
            User(name = name, email = email, token = token)
        } else {
            null
        }
    }

    override val theme: Flow<String> = preferencesManager.theme

    override suspend fun saveTheme(theme: String) {
        preferencesManager.saveTheme(theme)
    }

    override suspend fun saveSession(user: User) {
        preferencesManager.saveSession(
            token = user.token.orEmpty(),
            email = user.email,
            name = user.name
        )
    }

    override suspend fun clearSession() {
        preferencesManager.clearAuthState()
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val state = AuthState(
                isLoggedIn = true,
                userName = name,
                userEmail = email,
                userPassword = password,
                sessionStartTimestamp = System.currentTimeMillis()
            )
            preferencesManager.saveAuthState(state)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val currentAuthState = preferencesManager.authState.firstOrNull() ?: AuthState.EMPTY
            val storedEmail = currentAuthState.userEmail
            val storedPassword = currentAuthState.userPassword

            if (storedEmail != null && storedEmail.equals(email, ignoreCase = true) && storedPassword == password) {
                val updatedState = currentAuthState.copy(
                    isLoggedIn = true,
                    sessionStartTimestamp = System.currentTimeMillis()
                )
                preferencesManager.saveAuthState(updatedState)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            preferencesManager.clearAuthState()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        return try {
            val currentAuthState = preferencesManager.authState.firstOrNull() ?: AuthState.EMPTY
            val storedEmail = currentAuthState.userEmail

            if (storedEmail != null && storedEmail.equals(email, ignoreCase = true)) {
                val updatedState = currentAuthState.copy(
                    userPassword = newPassword,
                    isLoggedIn = false // Log out to force new login with the new password
                )
                preferencesManager.saveAuthState(updatedState)
                Result.success(Unit)
            } else {
                Result.failure(Exception("No account registered with this email address"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
