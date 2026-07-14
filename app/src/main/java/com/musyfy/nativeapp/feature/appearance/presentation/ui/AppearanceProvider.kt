package com.musyfy.nativeapp.feature.appearance.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.core.ui.theme.BackgroundSource
import com.musyfy.nativeapp.core.ui.theme.LocalAppearanceBackgroundSource
import com.musyfy.nativeapp.core.ui.theme.LocalAppearanceOverlay
import com.musyfy.nativeapp.core.ui.theme.LocalAppearanceSettingsCard
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode
import com.musyfy.nativeapp.feature.appearance.presentation.AppearanceViewModel

@Composable
fun AppearanceProvider(
    content: @Composable () -> Unit
) {
    val viewModel: AppearanceViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    var isOverlayOpen by remember { mutableStateOf(false) }

    val bgSource = when (state.themeMode) {
        ThemeMode.X -> BackgroundSource.XTheme
        ThemeMode.Y -> BackgroundSource.YTheme
        ThemeMode.CUSTOM -> {
            val file = state.customWallpaperPath?.let { java.io.File(it) }
            if (file != null && file.exists()) {
                BackgroundSource.Custom(file, state.wallpaperVersion)
            } else {
                BackgroundSource.XTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalAppearanceSettingsCard provides {
            AppearanceEntryCard(
                onClick = { isOverlayOpen = true }
            )
        },
        LocalAppearanceOverlay provides {
            AnimatedVisibility(
                visible = isOverlayOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AppearanceScreen(
                    viewModel = viewModel,
                    onBack = { isOverlayOpen = false }
                )
            }
        },
        LocalAppearanceBackgroundSource provides bgSource
    ) {
        content()
    }
}
