package com.musyfy.nativeapp.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<String>,
    val createdAt: Long
)
