package com.musyfy.nativeapp.feature.search.presentation.ui

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.components.SongOptionsBottomSheet
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import com.musyfy.nativeapp.feature.home.presentation.ui.SwipeToRevealSongRow
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import com.musyfy.nativeapp.feature.playlist.presentation.PlaylistViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val songs by viewModel.songs.collectAsState()
    val uiState by viewModel.playbackUiState.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()

    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    var songOptionsTarget by remember { mutableStateOf<Song?>(null) }
    var swipedSongId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteConfirmationForSong by remember { mutableStateOf<Song?>(null) }

    // Focus state for search bar animations
    var isSearchFocused by remember { mutableStateOf(false) }

    // Recent Searches history state (stored in memory, pre-populated with beautiful defaults)
    var recentSearches by remember {
        mutableStateOf(listOf("Lofi Chill", "Synthwave", "Acoustic", "Jazz Piano"))
    }

    // Single source of truth derivations for library categories
    val recentlyPlayed = remember(songs, uiState.currentSong) {
        val current = uiState.currentSong
        val list = if (current != null) {
            listOf(current) + songs.filter { it.id != current.id }
        } else {
            songs
        }
        list.take(4)
    }

    val recentlyImported = remember(songs) {
        songs.takeLast(4).reversed()
    }

    val mostPlayed = remember(songs) {
        // Take a stable, distinct subset sorted deterministically (e.g. by title length)
        songs.sortedBy { it.title.length }.take(4)
    }

    val likedSongs = remember(songs) {
        songs.filter { it.liked }.take(4)
    }

    val filteredSongs = remember(query, songs) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            songs.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                        it.artist?.contains(trimmed, ignoreCase = true) == true
            }
        }
    }

    // Search Bar Focus animations
    val searchBarHeight by animateDpAsState(
        targetValue = if (isSearchFocused) 64.dp else 56.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "SearchBarHeight"
    )
    val searchBarScale by animateFloatAsState(
        targetValue = if (isSearchFocused) 1.0f else 0.98f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "SearchBarScale"
    )
    val searchBarBorderColor by animateColorAsState(
        targetValue = if (isSearchFocused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "SearchBarBorder"
    )
    val searchBarShadowRadius by animateDpAsState(
        targetValue = if (isSearchFocused) 10.dp else 2.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "SearchBarShadow"
    )

    // Dark overlay opacity animation (keeping wallpaper sharp beneath)
    val darkOverlayAlpha by animateFloatAsState(
        targetValue = if (isSearchFocused) 0.4f else 0.0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "DarkOverlayAlpha"
    )

    // Focus listener haptic
    LaunchedEffect(isSearchFocused) {
        if (isSearchFocused) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Subtle dark overlay when focused (keeps wallpaper sharp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = darkOverlayAlpha))
        )

        // Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp)) // App Bar offset (shifted up, respecting status bar via Scaffold)

            // Search Header Title Block
            Column {
                Text(
                    text = "Search",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Search your personal music library",
                    color = Color(0xFF9E9E9E),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= SEARCH BAR HERO (Seamless Single-Piece Glass component) =================
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(searchBarHeight)
                    .scale(searchBarScale)
                    .onFocusChanged { isSearchFocused = it.isFocused }
                    .shadow(
                        elevation = searchBarShadowRadius,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = if (isSearchFocused) MaterialTheme.colorScheme.primary else Color.Black,
                        spotColor = if (isSearchFocused) MaterialTheme.colorScheme.primary else Color.Black
                    )
                    .background(
                        if (isSearchFocused) Color(0x33000000) else Color(0x1F000000),
                        RoundedCornerShape(18.dp)
                    )
                    .border(1.dp, searchBarBorderColor, RoundedCornerShape(18.dp)),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        if (query.trim().isNotEmpty() && !recentSearches.contains(query.trim())) {
                            recentSearches = (listOf(query.trim()) + recentSearches).take(5)
                        }
                        focusManager.clearFocus()
                    }
                ),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Back arrow directly inside the Row
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSearchFocused,
                            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -20 }),
                            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -20 })
                        ) {
                            Text(
                                text = "←",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clickable { focusManager.clearFocus() }
                                    .padding(end = 12.dp)
                            )
                        }

                        // Search icon directly inside Row
                        val searchIconAlpha by animateFloatAsState(
                            targetValue = if (isSearchFocused) 0.5f else 1.0f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            label = "SearchIconAlpha"
                        )
                        Text(
                            text = "🔍",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .alpha(searchIconAlpha)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text input and placeholder overlay area (100% transparent stacking Box)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search songs, artists...",
                                    color = Color(0x66FFFFFF),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }

                        // Animated Clear Button directly inside Row
                        androidx.compose.animation.AnimatedVisibility(
                            visible = query.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Text(
                                text = "✕",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        query = ""
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) // Soft tick haptic
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ================= SCROLLABLE RESULTS / EMPTY STATE AREA =================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // STATE 1: Idle (Not Focused & Empty) -> Show Library Sections
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isSearchFocused && query.trim().isEmpty(),
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    if (songs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp)
                                .background(Color(0x0AFFFFFF), RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Your Library is Empty",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Use the Import tab to download songs from YouTube and build your library.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            if (recentlyPlayed.isNotEmpty()) {
                                LibrarySection(title = "Recently Played", songs = recentlyPlayed) { song ->
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.playSong(song, queue = recentlyPlayed)
                                }
                            }

                            if (recentlyImported.isNotEmpty()) {
                                LibrarySection(title = "Recently Imported", songs = recentlyImported) { song ->
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.playSong(song, queue = recentlyImported)
                                }
                            }

                            if (mostPlayed.isNotEmpty()) {
                                LibrarySection(title = "Most Played", songs = mostPlayed) { song ->
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.playSong(song, queue = mostPlayed)
                                }
                            }

                            if (likedSongs.isNotEmpty()) {
                                LibrarySection(title = "Liked Songs", songs = likedSongs) { song ->
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.playSong(song, queue = likedSongs)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // STATE 2: Focused & Empty -> Show Recent Searches
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSearchFocused && query.trim().isEmpty(),
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Recent Searches",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                        if (recentSearches.isEmpty()) {
                            Text(
                                text = "Your recent searches will appear here.",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                recentSearches.forEach { search ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                query = search
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            }
                                            .padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(text = "🕒", fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
                                            Text(text = search, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                        }
                                        Text(
                                            text = "✕",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .clickable {
                                                    recentSearches = recentSearches.filter { it != search }
                                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                }
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // STATE 3: Typing & Match Found -> Show Search Results
                androidx.compose.animation.AnimatedVisibility(
                    visible = query.trim().isNotEmpty() && filteredSongs.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(150))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Search Results (${filteredSongs.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(filteredSongs) { index, song ->
                                val status = downloadStatuses[song.id] ?: DownloadStatus.NotDownloaded
                                AnimatedResultRow(index = index) {
                                    var isPressed by remember { mutableStateOf(false) }
                                    val cardScale by animateFloatAsState(
                                        targetValue = if (isPressed) 1.03f else 1.0f,
                                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                                        label = "ResultCardScale"
                                    )
                                    val cardShadow by animateDpAsState(
                                        targetValue = if (isPressed) 8.dp else 2.dp,
                                        animationSpec = tween(200),
                                        label = "ResultCardShadow"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .scale(cardScale)
                                            .shadow(elevation = cardShadow, shape = RoundedCornerShape(16.dp))
                                            .background(Color(0x0FFFFFFF), RoundedCornerShape(16.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                            .pointerInput(Unit) {
                                                awaitPointerEventScope {
                                                    while (true) {
                                                        awaitFirstDown()
                                                        isPressed = true
                                                        waitForUpOrCancellation()
                                                        isPressed = false
                                                    }
                                                }
                                            }
                                            .padding(6.dp)
                                    ) {
                                        SwipeToRevealSongRow(
                                            song = song,
                                            downloadStatus = status,
                                            isActive = uiState.currentSong?.id == song.id,
                                            onClick = {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) // Selection haptic
                                                // Save to recent searches history if clicked
                                                if (query.trim().isNotEmpty() && !recentSearches.contains(query.trim())) {
                                                    recentSearches = (listOf(query.trim()) + recentSearches).take(5)
                                                }
                                                viewModel.playSong(song, queue = filteredSongs)
                                            },
                                            onDownloadClick = {
                                                if (status is DownloadStatus.Downloaded) {
                                                    viewModel.deleteDownloadedSong(song.id)
                                                } else if (status !is DownloadStatus.Downloading) {
                                                    viewModel.startDownload(song)
                                                }
                                            },
                                            onOptionClick = {
                                                songOptionsTarget = song
                                            },
                                            onPlayNext = {
                                                viewModel.playNext(song)
                                            },
                                            onAddToPlaylist = {
                                                songOptionsTarget = song
                                            },
                                            onDelete = {
                                                showDeleteConfirmationForSong = song
                                            },
                                            onToggleLike = {
                                                viewModel.toggleLikeSong(song, source = "Search")
                                            },
                                            isRevealed = swipedSongId == song.id,
                                            onReveal = { opened -> swipedSongId = if (opened) song.id else null },
                                            modifier = Modifier.background(Color.Transparent)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // STATE 4: Typing & No Matches -> Show Premium No Results
                androidx.compose.animation.AnimatedVisibility(
                    visible = query.trim().isNotEmpty() && filteredSongs.isEmpty(),
                    enter = fadeIn(animationSpec = tween(400)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0x0AFFFFFF), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📭", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No Results Found",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We couldn't find any songs matching \"$query\". Check your spelling or try another search term.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Overlay options bottom sheet
        if (songOptionsTarget != null) {
            SongOptionsBottomSheet(
                song = songOptionsTarget!!,
                playlists = playlists,
                onDismissRequest = { songOptionsTarget = null },
                onPlayNext = { viewModel.playNext(songOptionsTarget!!) },
                onAddToQueue = { viewModel.addToQueue(songOptionsTarget!!) },
                onAddToPlaylist = { playlistId ->
                    playlistViewModel.addSongToPlaylist(playlistId, songOptionsTarget!!.id)
                },
                onDeleteFromLibrary = {
                    showDeleteConfirmationForSong = songOptionsTarget
                    songOptionsTarget = null
                }
            )
        }

        if (showDeleteConfirmationForSong != null) {
            val targetSong = showDeleteConfirmationForSong!!
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationForSong = null },
                title = { Text("Delete Song from Library?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Are you sure you want to remove \"${targetSong.title}\" from your library? This will delete its metadata and downloaded offline files.",
                        color = Color(0xFF888888),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val deletedSong = targetSong
                            viewModel.deleteSong(deletedSong, source = "Search")
                            showDeleteConfirmationForSong = null
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Song deleted",
                                    actionLabel = "UNDO",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreSong(deletedSong)
                                }
                            }
                        }
                    ) {
                        Text("DELETE", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmationForSong = null }) {
                        Text("CANCEL", color = Color.White)
                    }
                },
                containerColor = Color(0xFF141416)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }
}

@Composable
fun LibrarySection(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                LibrarySongCard(song = song) {
                    onSongClick(song)
                }
            }
        }
    }
}

@Composable
fun LibrarySongCard(
    song: Song,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "LibraryCardScale"
    )

    val context = LocalContext.current
    val resolvedArtModel: Any? = remember(song) {
        val defaultLocalArt = File(context.filesDir, "${song.id}.jpg")
        when {
            !song.artworkPath.isNullOrEmpty() && File(song.artworkPath).exists() -> {
                File(song.artworkPath)
            }
            defaultLocalArt.exists() -> {
                defaultLocalArt
            }
            !song.imageUrl.isNullOrEmpty() -> {
                song.imageUrl
            }
            else -> {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .width(110.dp)
            .scale(cardScale)
            .background(Color(0x0FFFFFFF), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E20)),
            contentAlignment = Alignment.Center
        ) {
            if (resolvedArtModel != null) {
                AsyncImage(
                    model = resolvedArtModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.logo),
                    placeholder = painterResource(id = R.drawable.logo)
                )
            } else {
                Text(text = "🎵", fontSize = 28.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = song.artist ?: "Unknown Artist",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AnimatedResultRow(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 35L)
        visible = true
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { 8 },
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        content()
    }
}
