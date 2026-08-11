package com.musyfy.nativeapp.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.musyfy.nativeapp.common.Constants
import com.musyfy.nativeapp.domain.model.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val sessionTokenKey = stringPreferencesKey(Constants.SESSION_TOKEN_KEY)
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userPasswordKey = stringPreferencesKey("user_password") // Temporary development password storage
    private val sessionStartTimestampKey = longPreferencesKey("session_start_timestamp")
    private val feedbackInitiationsKey = stringPreferencesKey("feedback_initiation_timestamps")

    suspend fun canInitiateFeedback(maxAllowed: Int = 3, rollingWindowMs: Long = 24 * 60 * 60 * 1000L): Boolean {
        val now = System.currentTimeMillis()
        val preferences = dataStore.data.first()
        val raw = preferences[feedbackInitiationsKey] ?: ""
        val recentTimestamps = raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { (now - it) < rollingWindowMs }
        return recentTimestamps.size < maxAllowed
    }

    suspend fun recordFeedbackInitiated(rollingWindowMs: Long = 24 * 60 * 60 * 1000L) {
        val now = System.currentTimeMillis()
        dataStore.edit { preferences ->
            val raw = preferences[feedbackInitiationsKey] ?: ""
            val validTimestamps = raw.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .filter { (now - it) < rollingWindowMs }
                .toMutableList()
            validTimestamps.add(now)
            preferences[feedbackInitiationsKey] = validTimestamps.joinToString(",")
        }
    }

    val authState: Flow<AuthState> = dataStore.data.map { preferences ->
        AuthState(
            isLoggedIn = preferences[isLoggedInKey] ?: false,
            userName = preferences[userNameKey],
            userEmail = preferences[userEmailKey],
            userPassword = preferences[userPasswordKey],
            sessionStartTimestamp = preferences[sessionStartTimestampKey]
        )
    }

    val sessionToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[sessionTokenKey]
    }

    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[userEmailKey]
    }

    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[userNameKey]
    }

    suspend fun saveAuthState(state: AuthState) {
        dataStore.edit { preferences ->
            preferences[isLoggedInKey] = state.isLoggedIn
            if (state.userName != null) {
                preferences[userNameKey] = state.userName
            } else {
                preferences.remove(userNameKey)
            }
            if (state.userEmail != null) {
                preferences[userEmailKey] = state.userEmail
            } else {
                preferences.remove(userEmailKey)
            }
            if (state.userPassword != null) {
                preferences[userPasswordKey] = state.userPassword
            } else {
                preferences.remove(userPasswordKey)
            }
            if (state.sessionStartTimestamp != null) {
                preferences[sessionStartTimestampKey] = state.sessionStartTimestamp
            } else if (state.isLoggedIn && preferences[sessionStartTimestampKey] == null) {
                preferences[sessionStartTimestampKey] = System.currentTimeMillis()
            } else if (!state.isLoggedIn) {
                preferences.remove(sessionStartTimestampKey)
            }
        }
    }

    suspend fun clearAuthState() {
        dataStore.edit { preferences ->
            preferences[isLoggedInKey] = false
            preferences.remove(sessionTokenKey)
            preferences.remove(sessionStartTimestampKey)
        }
    }

    suspend fun saveSession(token: String, email: String, name: String) {
        dataStore.edit { preferences ->
            preferences[sessionTokenKey] = token
            preferences[userEmailKey] = email
            preferences[userNameKey] = name
            preferences[isLoggedInKey] = true
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences[isLoggedInKey] = false
            preferences.remove(sessionTokenKey)
        }
    }

    private val themeKey = stringPreferencesKey("musyfi_theme")

    val theme: Flow<String> = dataStore.data.map { preferences ->
        preferences[themeKey] ?: "male"
    }

    suspend fun saveTheme(themeValue: String) {
        dataStore.edit { preferences ->
            preferences[themeKey] = themeValue
        }
    }
}
