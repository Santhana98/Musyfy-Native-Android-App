package com.musyfy.nativeapp.feature.player.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel

@Composable
fun FullPlayerScreen(
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept Back button press to collapse the player
    BackHandler(enabled = true) {
        onCollapse()
    }

    val uiState by viewModel.playbackUiState.collectAsState()
    val currentSong = uiState.currentSong

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0505), Color(0xFF0D0D0D))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Drag Handle (Swipe Down gesture zone to collapse)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount > 12f) {
                                    onCollapse()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp, 4.dp)
                        .background(Color(0x22FFFFFF), RoundedCornerShape(2.dp))
                )
            }

            // 2. Navigation Header Row (matches legacy display: flex)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Collapse button (⌄)
                Text(
                    text = "⌄",
                    color = Color(0xFF888888),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onCollapse() }
                        .padding(horizontal = 12.dp)
                )

                // Header title
                Text(
                    text = "NOW PLAYING",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                // Download icon placeholder (⬇)
                Text(
                    text = "⬇",
                    color = Color(0xFFE53935),
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // 3. Rotating Vinyl Record Component
            VinylPlayer(
                imageUrl = currentSong?.imageUrl,
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.weight(0.6f))

            // 4. Metadata (Title & Artist)
            SongInfoSection(
                title = currentSong?.title ?: "No Song Playing",
                artist = currentSong?.artist ?: "Unknown Artist"
            )

            // 5. Progress Section (Slider & Time labels)
            ProgressSection(
                currentPositionMs = uiState.currentPositionMs,
                durationMs = uiState.durationMs,
                onSeek = { position -> viewModel.seekTo(position) }
            )

            // 6. Playback controls (Play, Pause, Prev, Next)
            PlayerControls(
                isPlaying = uiState.isPlaying,
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
        }
    }
}
