package com.musyfy.nativeapp.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val url: String,
    val imageUrl: String? = null,
    val durationMs: Long = 0L,
    val liked: Boolean = false
)
