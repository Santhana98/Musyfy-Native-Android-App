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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus

@Composable
fun FullPlayerScreen(
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQueueOverlay by remember { mutableStateOf(false) }

    // Intercept Back button press to collapse the player or close the queue overlay
    BackHandler(enabled = true) {
        if (showQueueOverlay) {
            showQueueOverlay = false
        } else {
            onCollapse()
        }
    }

    val uiState by viewModel.playbackUiState.collectAsState()
    val currentSong = uiState.currentSong
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
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

                    // Download icon with dynamic state tracking
                    val currentSongStatus = currentSong?.let { downloadStatuses[it.id] } ?: DownloadStatus.NotDownloaded
                    Box(
                        modifier = Modifier
                            .clickable(enabled = currentSong != null) {
                                currentSong?.let { song ->
                                    if (currentSongStatus is DownloadStatus.Downloaded) {
                                        viewModel.deleteDownloadedSong(song.id)
                                    } else if (currentSongStatus !is DownloadStatus.Downloading) {
                                        viewModel.startDownload(song)
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp)
                    ) {
                        when (currentSongStatus) {
                            is DownloadStatus.NotDownloaded -> {
                                Text(text = "⬇", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            is DownloadStatus.Downloading -> {
                                androidx.compose.material3.CircularProgressIndicator(
                                    progress = { currentSongStatus.progress },
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.5.dp
                                )
                            }
                            is DownloadStatus.Downloaded -> {
                                Text(text = "✅", color = Color(0xFF4CAF50), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            is DownloadStatus.Error -> {
                                Text(text = "⚠️", color = Color(0xFFEF5350), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.4f))

                // 3. Rotating Vinyl Record Component
                val artworkModel = when {
                    currentSong?.artworkPath != null && java.io.File(currentSong.artworkPath).exists() -> {
                        android.util.Log.d("MusyfyPlayback", "Artwork [FullPlayer]: song.id=${currentSong.id}, artworkPath=${currentSong.artworkPath}, exists=true, loading from local artworkPath")
                        java.io.File(currentSong.artworkPath)
                    }
                    currentSong?.imageUrl != null -> {
                        android.util.Log.d("MusyfyPlayback", "Artwork [FullPlayer]: song.id=${currentSong.id}, remoteUrl=${currentSong.imageUrl}, loading from remote imageUrl")
                        currentSong.imageUrl
                    }
                    else -> {
                        android.util.Log.d("MusyfyPlayback", "Artwork [FullPlayer]: song.id=${currentSong?.id}, no artwork available, showing placeholder")
                        null
                    }
                }

                VinylPlayer(
                    imageUrl = artworkModel,
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
                    shuffleModeEnabled = uiState.shuffleModeEnabled,
                    repeatMode = uiState.repeatMode,
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
                    onNextClick = { viewModel.skipToNext() },
                    onShuffleToggle = {
                        viewModel.setShuffleModeEnabled(!uiState.shuffleModeEnabled)
                    },
                    onRepeatToggle = {
                        viewModel.setRepeatMode((uiState.repeatMode + 1) % 3)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Queue Trigger Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showQueueOverlay = true }) {
                        Text(text = "☰", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Play Queue Overlay Screen
        if (showQueueOverlay) {
            QueueOverlayScreen(
                viewModel = viewModel,
                onClose = { showQueueOverlay = false }
            )
        }
    }
}
