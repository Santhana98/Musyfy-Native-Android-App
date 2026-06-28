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
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import androidx.compose.foundation.layout.Column

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    var activeTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            Column {
                val uiState by viewModel.playbackUiState.collectAsState()
                val currentSong = uiState.currentSong

                val progressPct = if (uiState.durationMs > 0) {
                    uiState.currentPositionMs.toFloat() / uiState.durationMs
                } else {
                    0f
                }

                val formatTime: (Long) -> String = { ms ->
                    val totalSeconds = ms / 1000
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    "$minutes:${if (seconds < 10) "0" else ""}$seconds"
                }

                MiniPlayerPlaceholder(
                    songTitle = currentSong?.title ?: "No Song Playing",
                    artistName = currentSong?.artist.orEmpty(),
                    isPlaying = uiState.isPlaying,
                    progressPct = progressPct,
                    imageUrl = currentSong?.imageUrl,
                    currentTimeText = formatTime(uiState.currentPositionMs),
                    durationText = formatTime(uiState.durationMs),
                    durationMs = uiState.durationMs,
                    onSeek = { position -> viewModel.seekTo(position) },
                    onPlayPauseClick = {
                        if (uiState.isPlaying) {
                            viewModel.pause()
                        } else {
                            if (currentSong == null) {
                                val songsList = viewModel.songs.value
                                if (songsList.isNotEmpty()) {
                                    viewModel.playSong(songsList.first())
                                }
                            } else {
                                viewModel.play()
                            }
                        }
                    },
                    onPrevClick = { viewModel.skipToPrevious() },
                    onNextClick = { viewModel.skipToNext() }
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
                    "home" -> HomeScreen(viewModel = viewModel)
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
