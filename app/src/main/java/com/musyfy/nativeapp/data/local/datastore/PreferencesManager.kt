package com.musyfy.nativeapp.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.musyfy.nativeapp.common.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val sessionTokenKey = stringPreferencesKey(Constants.SESSION_TOKEN_KEY)
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")

    val sessionToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[sessionTokenKey]
    }

    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[userEmailKey]
    }

    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[userNameKey]
    }

    suspend fun saveSession(token: String, email: String, name: String) {
        dataStore.edit { preferences ->
            preferences[sessionTokenKey] = token
            preferences[userEmailKey] = email
            preferences[userNameKey] = name
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(sessionTokenKey)
            preferences.remove(userEmailKey)
            preferences.remove(userNameKey)
        }
    }
}
