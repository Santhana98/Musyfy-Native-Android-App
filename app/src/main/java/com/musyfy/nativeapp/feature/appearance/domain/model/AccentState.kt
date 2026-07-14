package com.musyfy.nativeapp.feature.appearance.domain.model

data class AccentState(
    val primaryAccent: Int = 0xFFF9423A.toInt(),
    val secondaryAccent: Int = 0xFFF9423A.toInt(),
    val isLoading: Boolean = false,
    val isFallback: Boolean = true,
    val sourceMode: ThemeMode = ThemeMode.X,
    val wallpaperVersion: Long = 0L
)
