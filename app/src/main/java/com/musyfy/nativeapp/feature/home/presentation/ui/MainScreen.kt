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
import com.musyfy.nativeapp.core.ui.components.PremiumThemeBackground
import com.musyfy.nativeapp.feature.download.presentation.ui.UploadScreen
import com.musyfy.nativeapp.feature.library.presentation.ui.LikedScreen
import com.musyfy.nativeapp.feature.search.presentation.ui.SearchScreen
import com.musyfy.nativeapp.feature.settings.presentation.ui.SettingsScreen
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.layout.Column
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.musyfy.nativeapp.core.analytics.NavigationAnalyticsViewModel
import com.musyfy.nativeapp.core.analytics.NavigationAnalyticsMapper

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val analyticsViewModel: NavigationAnalyticsViewModel = hiltViewModel()
    var activeTab by remember { mutableStateOf("home") }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(activeTab) {
        val screenName = NavigationAnalyticsMapper.mapRouteToScreenName(activeTab)
        if (screenName != null) {
            analyticsViewModel.tracker.logScreenView(screenName)
        }
    }

    val homeLazyListState = rememberLazyListState()
    val topBarHeightPx = with(LocalDensity.current) { 76.dp.toPx() }
    val topBarOffsetPx by remember(homeLazyListState) {
        derivedStateOf {
            if (homeLazyListState.firstVisibleItemIndex > 0) {
                -topBarHeightPx
            } else {
                val offset = homeLazyListState.firstVisibleItemScrollOffset.toFloat()
                -offset.coerceAtMost(topBarHeightPx)
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (!isPlayerExpanded) {
                val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                AnimatedVisibility(
                    visible = !isKeyboardVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + expandVertically() + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically() + fadeOut()
                ) {
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

                        val artworkModel = when {
                            currentSong?.artworkPath != null && java.io.File(currentSong.artworkPath).exists() -> {
                                android.util.Log.d("MusyfyPlayback", "Artwork [MainScreen/MiniPlayer]: song.id=${currentSong.id}, artworkPath=${currentSong.artworkPath}, exists=true, loading from local artworkPath")
                                java.io.File(currentSong.artworkPath)
                            }
                            currentSong?.imageUrl != null -> {
                                android.util.Log.d("MusyfyPlayback", "Artwork [MainScreen/MiniPlayer]: song.id=${currentSong.id}, remoteUrl=${currentSong.imageUrl}, loading from remote imageUrl")
                                currentSong.imageUrl
                            }
                            else -> {
                                android.util.Log.d("MusyfyPlayback", "Artwork [MainScreen/MiniPlayer]: song.id=${currentSong?.id}, no artwork available, showing placeholder")
                                null
                            }
                        }

                        if (currentSong != null && uiState.playbackSessionActive) {
                            MiniPlayerPlaceholder(
                                songTitle = currentSong.title,
                                artistName = currentSong.artist.orEmpty(),
                                isPlaying = uiState.isPlaying,
                                progressPct = progressPct,
                                imageUrl = artworkModel,
                                currentTimeText = formatTime(uiState.currentPositionMs),
                                durationText = formatTime(uiState.durationMs),
                                durationMs = uiState.durationMs,
                                onSeek = { position -> viewModel.seekTo(position) },
                                onExpandClick = { isPlayerExpanded = true },
                                onPlayPauseClick = {
                                    if (uiState.isPlaying) {
                                        viewModel.pause()
                                    } else {
                                        viewModel.play()
                                    }
                                },
                                onPrevClick = { viewModel.skipToPrevious() },
                                onNextClick = { viewModel.skipToNext() },
                                onDismissClick = { viewModel.dismissPlaybackSession() }
                            )
                        }
                        MusyfyBottomNavigationBar(
                            activeTab = activeTab,
                            onTabSelected = { activeTab = it },
                            modifier = Modifier.navigationBarsPadding()
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent // Make Scaffold transparent so the background shows through
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed Background at full size (no padding, completely static during IME changes)
            val authViewModel: AuthViewModel = hiltViewModel()
            val themeState by authViewModel.theme.collectAsState()
            PremiumThemeBackground(themeState = themeState)
            // Screen content area (only padding bottom to prevent overlap with the bottom nav elements)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                when (activeTab) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToUpload = { activeTab = "upload" },
                        scrollState = homeLazyListState
                    )
                    "search" -> SearchScreen()
                    "liked" -> LikedScreen()
                    "upload" -> UploadScreen(
                        viewModel = viewModel,
                        onNavigateToHome = { activeTab = "home" }
                    )
                    "settings" -> SettingsScreen(onLogout = onLogout)
                }
            }

            // Floating Top App Bar transparently overlaying the screen background (only on Home tab)
            if (activeTab == "home") {
                val authViewModel: AuthViewModel = hiltViewModel()
                val localAuthState by authViewModel.authState.collectAsState()
                var isLoggingOut by remember { mutableStateOf(false) }

                MusyfyTopBar(
                    userName = localAuthState.userName ?: "User",
                    onLogoutClick = {
                        if (!isLoggingOut) {
                            isLoggingOut = true
                            authViewModel.logout(onLogout)
                        }
                    },
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopCenter)
                        .statusBarsPadding()
                        .graphicsLayer {
                            translationY = topBarOffsetPx
                        }
                )
            }

            // Full Player Screen Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = androidx.compose.animation.slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                ) + androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200)
                ),
                exit = androidx.compose.animation.slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 400,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                ) + androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200)
                )
            ) {
                com.musyfy.nativeapp.feature.player.presentation.ui.PremiumFullPlayerScreen(
                    viewModel = viewModel,
                    onCollapse = { isPlayerExpanded = false }
                )
            }
        }
    }
}
