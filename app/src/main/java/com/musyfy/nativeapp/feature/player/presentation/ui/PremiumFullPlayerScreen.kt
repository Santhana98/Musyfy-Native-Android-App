package com.musyfy.nativeapp.feature.player.presentation.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.theme.BackgroundSource
import com.musyfy.nativeapp.core.ui.theme.LocalAccentColor
import com.musyfy.nativeapp.core.ui.theme.LocalAppearanceBackgroundSource
import com.musyfy.nativeapp.feature.appearance.domain.model.PlayerBackgroundMode
import com.musyfy.nativeapp.feature.appearance.presentation.AppearanceViewModel
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel

@Composable
fun PremiumFullPlayerScreen(
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

    val context = LocalContext.current
    val uiState by viewModel.playbackUiState.collectAsState()
    val currentSong = uiState.currentSong
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()

    // 1. Injected Appearance settings for White/Black background modes
    val appearanceViewModel: AppearanceViewModel = hiltViewModel()
    val appearanceState by appearanceViewModel.state.collectAsState()
    val playerBgMode = appearanceState.playerBackgroundMode
    val isLightMode = playerBgMode == PlayerBackgroundMode.WHITE

    // Smooth transition animations for background colors and wallpaper alpha
    val animBgColor by animateColorAsState(
        targetValue = if (isLightMode) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 500),
        label = "PremiumBgColor"
    )
    val animWallpaperAlpha by animateFloatAsState(
        targetValue = if (isLightMode) 0.12f else 0.28f,
        animationSpec = tween(durationMillis = 500),
        label = "PremiumWallpaperAlpha"
    )
    val textColor by animateColorAsState(
        targetValue = if (isLightMode) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 500),
        label = "PremiumTextColor"
    )

    // Dynamic Accent Color
    val accentColor = LocalAccentColor.current

    // Wallpaper rendering from unified appearance pipeline
    val bgSource = LocalAppearanceBackgroundSource.current
    val wallpaperPainter = when (bgSource) {
        BackgroundSource.XTheme -> painterResource(id = R.drawable.bg_male)
        BackgroundSource.YTheme -> painterResource(id = R.drawable.bg_female)
        is BackgroundSource.Custom -> coil.compose.rememberAsyncImagePainter(
            model = coil.request.ImageRequest.Builder(context)
                .data(bgSource.file)
                .memoryCacheKey("custom_wallpaper_${bgSource.version}")
                .diskCacheKey("custom_wallpaper_${bgSource.version}")
                .build()
        )
        null -> null
    }

    val secondaryGlassModifier = if (isLightMode) {
        Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Base Background Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animBgColor)
        ) {
            // Blended Wallpaper Layer
            if (wallpaperPainter != null && !isLightMode) {
                Image(
                    painter = wallpaperPainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = animWallpaperAlpha
                )
            }

            // Main Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // A. Drag Handle (Swipe Down to collapse)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
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
                            .background(textColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                    )
                }

                // B. Navigation Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back/Collapse button (←)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onCollapse() }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "←",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Header title
                    Text(
                        text = "NOW PLAYING",
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    // Download icon with dynamic state tracking
                    val currentSongStatus = currentSong?.let { downloadStatuses[it.id] } ?: DownloadStatus.NotDownloaded
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = currentSong != null) {
                                currentSong?.let { song ->
                                    if (currentSongStatus is DownloadStatus.Downloaded) {
                                        viewModel.deleteDownloadedSong(song.id)
                                    } else if (currentSongStatus !is DownloadStatus.Downloading) {
                                        viewModel.startDownload(song)
                                    }
                                }
                            }
                            .padding(8.dp)
                    ) {
                        when (currentSongStatus) {
                            is DownloadStatus.NotDownloaded -> {
                                Text(
                                    text = "⬇",
                                    color = accentColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            is DownloadStatus.Downloading -> {
                                CircularProgressIndicator(
                                    progress = { currentSongStatus.progress },
                                    modifier = Modifier.size(18.dp),
                                    color = accentColor,
                                    strokeWidth = 2.dp
                                )
                            }
                            is DownloadStatus.Downloaded -> {
                                Text(
                                    text = "✅",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            is DownloadStatus.Error -> {
                                Text(
                                    text = "⚠️",
                                    color = Color(0xFFEF5350),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // C. Segmented Background Mode Pill Selector (White / Black)
                PremiumBackgroundModePill(
                    isLightMode = isLightMode,
                    onModeChange = { newMode ->
                        appearanceViewModel.updateState(
                            appearanceState.copy(playerBackgroundMode = newMode)
                        )
                    }
                )

                Spacer(modifier = Modifier.weight(0.5f))

                // D. Large Premium Floating Vinyl Record Component
                val artworkModel = when {
                    currentSong?.artworkPath != null && java.io.File(currentSong.artworkPath).exists() -> {
                        java.io.File(currentSong.artworkPath)
                    }
                    currentSong?.imageUrl != null -> {
                        currentSong.imageUrl
                    }
                    else -> {
                        null
                    }
                }

                PremiumVinylPlayer(
                    imageUrl = artworkModel,
                    isPlaying = uiState.isPlaying,
                    isLightMode = isLightMode,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.weight(0.7f))

                // E. Song Title & Artist Section (Left-aligned with Heart/More buttons)
                PremiumSongInfoSection(
                    song = currentSong,
                    onLikeToggle = {
                        currentSong?.let { viewModel.toggleLikeSong(it) }
                    },
                    onMoreClick = {
                        Toast.makeText(context, "More options coming soon", Toast.LENGTH_SHORT).show()
                    },
                    textColor = textColor,
                    accentColor = accentColor
                )

                // F. Smooth Progress Slider
                PremiumProgressSection(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    onSeek = { position -> viewModel.seekTo(position) },
                    textColor = textColor,
                    accentColor = accentColor,
                    isLightMode = isLightMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                // G. Spring-animated playback controls
                PremiumPlayerControls(
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
                    },
                    isLightMode = isLightMode,
                    accentColor = accentColor,
                    textColor = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.weight(0.4f))

                // H. Bottom row secondary controls (Lyrics, Queue, Visualizer, Sleep, Speed)
                PremiumSecondaryControlsRow(
                    isPlaying = uiState.isPlaying,
                    onQueueClick = { showQueueOverlay = true },
                    onLyricsClick = {
                        Toast.makeText(context, "Lyrics feature coming soon", Toast.LENGTH_SHORT).show()
                    },
                    onSleepClick = {
                        Toast.makeText(context, "Sleep timer coming soon", Toast.LENGTH_SHORT).show()
                    },
                    onSpeedClick = {
                        Toast.makeText(context, "Playback speed coming soon", Toast.LENGTH_SHORT).show()
                    },
                    isLightMode = isLightMode,
                    textColor = textColor,
                    accentColor = accentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .then(secondaryGlassModifier)
                )
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

@Composable
fun PremiumBackgroundModePill(
    isLightMode: Boolean,
    onModeChange: (PlayerBackgroundMode) -> Unit
) {
    val pillBg = if (isLightMode) Color.White.copy(alpha = 0.55f) else Color(0x1AFFFFFF)
    val pillBorderBrush = if (isLightMode) {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                Color.Black.copy(alpha = 0.06f)
            )
        )
    } else {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Color(0x0DFFFFFF),
                Color(0x0DFFFFFF)
            )
        )
    }
    val activeItemBg = if (isLightMode) Color.White else Color(0xFF1E1E22)
    val activeTextColor = if (isLightMode) Color.Black else Color.White
    val inactiveTextColor = if (isLightMode) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.45f)

    val pillModifier = if (isLightMode) {
        Modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
    } else {
        Modifier
    }

    Row(
        modifier = pillModifier
            .background(pillBg, RoundedCornerShape(20.dp))
            .border(1.dp, pillBorderBrush, RoundedCornerShape(20.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // White Mode Option
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isLightMode) activeItemBg else Color.Transparent)
                .clickable { onModeChange(PlayerBackgroundMode.WHITE) }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "White Mode ☀️",
                color = if (isLightMode) activeTextColor else inactiveTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Black Mode Option
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (!isLightMode) activeItemBg else Color.Transparent)
                .clickable { onModeChange(PlayerBackgroundMode.BLACK) }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Black Mode 🌙",
                color = if (!isLightMode) activeTextColor else inactiveTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PremiumSecondaryControlsRow(
    isPlaying: Boolean,
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onSleepClick: () -> Unit,
    onSpeedClick: () -> Unit,
    isLightMode: Boolean,
    textColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val rowPadding = if (isLightMode) {
        Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    } else {
        Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(rowPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Lyrics Item
        PremiumSecondaryControlItem(
            label = "Lyrics",
            iconText = "🎙",
            onClick = onLyricsClick,
            textColor = textColor
        )

        // 2. Queue Item
        PremiumSecondaryControlItem(
            label = "Queue",
            iconText = "☰",
            onClick = onQueueClick,
            textColor = textColor
        )

        // 3. Soundwave Visualizer Pill (Animated center-piece)
        PremiumVisualizerPill(
            isPlaying = isPlaying,
            accentColor = accentColor,
            isLightMode = isLightMode
        )

        // 4. Sleep Timer Item
        PremiumSecondaryControlItem(
            label = "Sleep",
            iconText = "⏰",
            onClick = onSleepClick,
            textColor = textColor
        )

        // 5. Playback Speed Item
        PremiumSecondaryControlItem(
            label = "Speed",
            iconText = "⚙",
            onClick = onSpeedClick,
            textColor = textColor
        )
    }
}

@Composable
fun PremiumSecondaryControlItem(
    label: String,
    iconText: String,
    onClick: () -> Unit,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = iconText,
            color = textColor.copy(alpha = 0.7f),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = textColor.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
fun PremiumVisualizerPill(
    isPlaying: Boolean,
    accentColor: Color,
    isLightMode: Boolean
) {
    // 4 infinite bounce height animations
    val infiniteTransition = rememberInfiniteTransition(label = "SoundBars")
    
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(490, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )
    val bar4Height by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(330, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar4"
    )

    val maxBarHeight = 22.dp
    val barWidth = 3.dp

    val pillBg = if (isLightMode) accentColor.copy(alpha = 0.1f) else accentColor.copy(alpha = 0.15f)
    val pillBorder = if (isLightMode) accentColor.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .background(pillBg, RoundedCornerShape(14.dp))
            .border(1.dp, pillBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(bar1Height, bar2Height, bar3Height, bar4Height)
        repeat(4) { i ->
            val h = if (isPlaying) heights[i] else 0.2f
            Box(
                modifier = Modifier
                    .size(width = barWidth, height = maxBarHeight * h)
                    .background(accentColor, RoundedCornerShape(1.5.dp))
            )
        }
    }
}
