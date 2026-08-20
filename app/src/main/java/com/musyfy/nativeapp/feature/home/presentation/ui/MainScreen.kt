package com.musyfy.nativeapp.feature.home.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.core.analytics.NavigationAnalyticsMapper
import com.musyfy.nativeapp.core.analytics.NavigationAnalyticsViewModel
import com.musyfy.nativeapp.core.ui.components.MiniPlayerPlaceholder
import com.musyfy.nativeapp.core.ui.components.MusyfyBottomNavigationBar
import com.musyfy.nativeapp.core.ui.components.MusyfyTopBar
import com.musyfy.nativeapp.core.ui.components.PremiumThemeBackground
import com.musyfy.nativeapp.core.ui.components.rememberScrollAwareBottomBarState
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import com.musyfy.nativeapp.feature.download.presentation.ui.UploadScreen
import com.musyfy.nativeapp.feature.library.presentation.ui.LikedScreen
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import com.musyfy.nativeapp.feature.search.presentation.ui.SearchScreen
import com.musyfy.nativeapp.feature.settings.presentation.ui.SettingsScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToFeedback: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val analyticsViewModel: NavigationAnalyticsViewModel = hiltViewModel()
    val uploadViewModel: com.musyfy.nativeapp.feature.download.presentation.UploadViewModel = hiltViewModel()
    val hasPendingShare = remember { uploadViewModel.shareTargetManager.pendingShareUrl.value != null }
    var activeTab by remember { mutableStateOf(if (hasPendingShare) "upload" else "home") }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    val pendingSharedUrl by uploadViewModel.shareTargetManager.pendingShareUrl.collectAsState()

    LaunchedEffect(pendingSharedUrl) {
        val sharedUrl = pendingSharedUrl
        if (!sharedUrl.isNullOrEmpty()) {
            val urlToImport = uploadViewModel.shareTargetManager.consumePendingUrl()
            if (!urlToImport.isNullOrEmpty()) {
                selectedPlaylistId = null
                isPlayerExpanded = false
                activeTab = "upload"
                uploadViewModel.handleSharedUrl(urlToImport)
            }
        }
    }

    // Centralized scroll-aware state for Blinkit-style hide/show bottom navigation
    val scrollAwareState = rememberScrollAwareBottomBarState()

    LaunchedEffect(activeTab, isPlayerExpanded) {
        // Ensure bottom nav becomes visible whenever tab changes or player collapses
        scrollAwareState.show()

        val currentRoute = if (isPlayerExpanded) "full_player" else activeTab
        val screenInfo = NavigationAnalyticsMapper.mapRouteToScreenInfo(currentRoute)
        if (screenInfo != null) {
            analyticsViewModel.tracker.logScreenView(screenInfo.name, screenInfo.category)
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

    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val density = LocalDensity.current

    val uiState by viewModel.playbackUiState.collectAsState()
    val currentSong = uiState.currentSong
    val isMiniPlayerActive = currentSong != null && uiState.playbackSessionActive && !isKeyboardVisible

    // Actual floating bottom navigation bar layout height (64.dp pill + 12.dp vertical padding)
    val measuredNavHeightDp = 76.dp
    val animatedNavBarHeight by animateDpAsState(
        targetValue = if (scrollAwareState.isVisible && !isKeyboardVisible) measuredNavHeightDp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "navBarHeightAnim"
    )
    val navBarOffsetYDp by animateDpAsState(
        targetValue = if (scrollAwareState.isVisible && !isKeyboardVisible) 0.dp else 80.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "navBarOffsetYAnim"
    )

    // Dynamic content bottom padding calculation to prevent leftover reserved space & black gaps
    val miniPlayerHeightDp = if (isMiniPlayerActive) 64.dp else 0.dp
    val navBarInsetDp = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val dynamicContentBottomPadding = animatedNavBarHeight + miniPlayerHeightDp + navBarInsetDp

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollAwareState.nestedScrollConnection)
    ) {
        // 1. Full-screen Static Background (no bottom padding, completely static behind content & bars)
        val authViewModel: AuthViewModel = hiltViewModel()
        val themeState by authViewModel.theme.collectAsState()
        PremiumThemeBackground(themeState = themeState)

        // 2. Active Screen Content (padded dynamically so content adapts smoothly to navigation visibility)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = with(density) { WindowInsets.statusBars.getTop(density).toDp() },
                    bottom = dynamicContentBottomPadding
                )
        ) {
            when (activeTab) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToUpload = { activeTab = "upload" },
                    scrollState = homeLazyListState,
                    selectedPlaylistId = selectedPlaylistId,
                    onPlaylistSelected = { selectedPlaylistId = it },
                    onClearSelectedPlaylist = { selectedPlaylistId = null }
                )
                "search" -> SearchScreen()
                "playlists" -> {
                    if (selectedPlaylistId != null) {
                        com.musyfy.nativeapp.feature.playlist.presentation.ui.PlaylistDetailScreen(
                            playlistId = selectedPlaylistId!!,
                            onCollapse = { selectedPlaylistId = null }
                        )
                    } else {
                        com.musyfy.nativeapp.feature.playlist.presentation.ui.PlaylistsScreen(
                            onPlaylistSelected = { selectedPlaylistId = it }
                        )
                    }
                }
                "upload" -> UploadScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        selectedPlaylistId = null
                        activeTab = "home"
                    }
                )
                "settings" -> SettingsScreen(
                    onLogout = onLogout,
                    onNavigateToFeedback = onNavigateToFeedback
                )
            }
        }

        // 3. Floating Top App Bar (only on Home tab)
        if (activeTab == "home") {
            val localAuthState by authViewModel.authState.collectAsState()
            var isLoggingOut by remember { mutableStateOf(false) }

            MusyfyTopBar(
                userName = localAuthState.userName ?: "User",
                onLogoutClick = {
                    if (!isLoggingOut) {
                        isLoggingOut = true
                        authViewModel.logout(
                            logoutSource = com.musyfy.nativeapp.core.analytics.AnalyticsConstants.Auth.LOGOUT_SOURCE_HOME_PROFILE,
                            onSuccess = onLogout
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .graphicsLayer {
                        translationY = topBarOffsetPx
                    }
            )
        }

        // 4. Unified Bottom Control Overlay (MiniPlayer + Bottom Navigation Bar)
        if (!isPlayerExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Single-source system navigation bar inset safety
            ) {
                // MiniPlayer Layer: sits directly above bottom nav bar.
                // When bottom nav hides (animatedNavBarHeight -> 0.dp), MiniPlayer smoothly moves downward
                // to occupy the bottom space above navigationBarsPadding(), remaining 100% visible and unclipped.
                if (isMiniPlayerActive) {
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
                            java.io.File(currentSong.artworkPath)
                        }
                        currentSong?.imageUrl != null -> {
                            currentSong.imageUrl
                        }
                        else -> {
                            null
                        }
                    }

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

                // Bottom Navigation Layer (collapses height and translates down smoothly on scroll down)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedNavBarHeight)
                        .graphicsLayer {
                            translationY = with(density) { navBarOffsetYDp.toPx() }
                            alpha = if (animatedNavBarHeight > 2.dp) 1f else 0f
                        }
                ) {
                    MusyfyBottomNavigationBar(
                        activeTab = activeTab,
                        onTabSelected = { tab ->
                            if (tab == "home") {
                                selectedPlaylistId = null
                            }
                            activeTab = tab
                            scrollAwareState.show()
                        }
                    )
                }
            }
        }

        // 5. Full Player Screen Overlay
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 200)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            com.musyfy.nativeapp.feature.player.presentation.ui.PremiumFullPlayerScreen(
                viewModel = viewModel,
                onCollapse = { isPlayerExpanded = false }
            )
        }
    }
}
