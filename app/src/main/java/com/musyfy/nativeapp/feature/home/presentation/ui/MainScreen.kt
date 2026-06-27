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
import com.musyfy.nativeapp.core.ui.components.MusyfyBottomNavigationBar
import com.musyfy.nativeapp.core.ui.components.MusyfyTopBar
import com.musyfy.nativeapp.feature.download.presentation.ui.UploadScreen
import com.musyfy.nativeapp.feature.library.presentation.ui.LikedScreen
import com.musyfy.nativeapp.feature.search.presentation.ui.SearchScreen
import com.musyfy.nativeapp.feature.settings.presentation.ui.SettingsScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            MusyfyTopBar()
        },
        bottomBar = {
            MusyfyBottomNavigationBar(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        },
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF070708) // Base dark background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF070708))
        ) {
            when (activeTab) {
                "home" -> HomeScreen()
                "search" -> SearchScreen()
                "liked" -> LikedScreen()
                "upload" -> UploadScreen()
                "settings" -> SettingsScreen()
            }
        }
    }
}
