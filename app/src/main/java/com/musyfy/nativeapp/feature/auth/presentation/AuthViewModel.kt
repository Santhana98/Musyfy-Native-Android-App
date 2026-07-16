package com.musyfy.nativeapp.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.domain.model.AuthState
import com.musyfy.nativeapp.domain.usecase.GetAuthStateUseCase
import com.musyfy.nativeapp.domain.usecase.LoginUseCase
import com.musyfy.nativeapp.domain.usecase.LogoutUseCase
import com.musyfy.nativeapp.domain.usecase.RegisterUseCase
import com.musyfy.nativeapp.domain.usecase.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.musyfy.nativeapp.domain.repository.UserRepository

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val userRepository: UserRepository,
    getAuthStateUseCase: GetAuthStateUseCase
) : ViewModel() {

    // Unified auth state from local storage (DataStore)
    val authState: StateFlow<AuthState> = getAuthStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.EMPTY)

    // Dynamic UI display theme
    val theme: StateFlow<String> = userRepository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "male")

    fun saveTheme(themeValue: String) {
        viewModelScope.launch {
            userRepository.saveTheme(themeValue)
        }
    }

    // UI state representing the current action process (Idle, Loading, Success, Error)
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = loginUseCase(email, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = registerUseCase(name, email, password)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = resetPasswordUseCase(email, newPassword)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Password reset failed")
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        if (_uiState.value is AuthUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = logoutUseCase()
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Idle
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Logout failed")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}
