package com.musyfy.nativeapp.data.repository

import com.musyfy.nativeapp.data.local.datastore.PreferencesManager
import com.musyfy.nativeapp.domain.model.User
import com.musyfy.nativeapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : UserRepository {

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

    override suspend fun saveSession(user: User) {
        preferencesManager.saveSession(
            token = user.token.orEmpty(),
            email = user.email,
            name = user.name
        )
    }

    override suspend fun clearSession() {
        preferencesManager.clearSession()
    }
}
