package com.musyfy.nativeapp.feature.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.musyfy.nativeapp.core.ui.components.MiniPlayerPlaceholder
import com.musyfy.nativeapp.core.ui.components.MusyfyBottomNavigationBar
import com.musyfy.nativeapp.core.ui.components.MusyfyTopBar
import com.musyfy.nativeapp.feature.download.presentation.ui.UploadScreen
import com.musyfy.nativeapp.feature.library.presentation.ui.LikedScreen
import com.musyfy.nativeapp.feature.search.presentation.ui.SearchScreen
import com.musyfy.nativeapp.feature.settings.presentation.ui.SettingsScreen
import androidx.compose.foundation.layout.Column

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("home") }
    var isPlaying by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column {
                MiniPlayerPlaceholder(
                    isPlaying = isPlaying,
                    onPlayPauseClick = { isPlaying = !isPlaying }
                )
                MusyfyBottomNavigationBar(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF070708) // Base dark background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070708))
        ) {
            // Screen content area (only padding bottom to prevent overlap with the bottom nav elements)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                when (activeTab) {
                    "home" -> HomeScreen()
                    "search" -> SearchScreen()
                    "liked" -> LikedScreen()
                    "upload" -> UploadScreen()
                    "settings" -> SettingsScreen()
                }
            }

            // Floating Top App Bar transparently overlaying the screen background
            MusyfyTopBar(
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
            )
        }
    }
}
