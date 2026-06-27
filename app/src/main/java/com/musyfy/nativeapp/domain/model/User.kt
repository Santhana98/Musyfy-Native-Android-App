package com.musyfy.nativeapp.domain.model

data class User(
    val name: String,
    val email: String,
    val token: String? = null
)
