package com.musyfy.nativeapp.domain.model

data class AuthState(
    val isLoggedIn: Boolean,
    val userName: String?,
    val userEmail: String?,
    val userPassword: String? // Temporary development password storage
) {
    companion object {
        val EMPTY = AuthState(
            isLoggedIn = false,
            userName = null,
            userEmail = null,
            userPassword = null
        )
    }
}
