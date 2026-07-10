package com.musyfy.nativeapp.feature.home.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import com.musyfy.nativeapp.feature.playlist.presentation.PlaylistViewModel
import com.musyfy.nativeapp.feature.playlist.presentation.ui.PlaylistDetailScreen
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.components.SongOptionsBottomSheet
import com.musyfy.nativeapp.domain.model.Song
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun HomeScreen(
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    scrollState: LazyListState = rememberLazyListState()
) {
    var activeTab by remember { mutableStateOf("all") }
    var swipedSongId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val themeState by authViewModel.theme.collectAsState()

    val songs by viewModel.songs.collectAsState()
    val uiState by viewModel.playbackUiState.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()

    val playlists by playlistViewModel.playlists.collectAsState()

    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var showAddToPlaylistDialog by remember { mutableStateOf<String?>(null) } // songId

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedSongs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var songOptionsTarget by remember { mutableStateOf<Song?>(null) }
    var showDeleteConfirmationForSong by remember { mutableStateOf<Song?>(null) }

    // Filter songs based on active tab
    val filteredSongs = when (activeTab) {
        "liked" -> songs.filter { it.liked }
        "recent" -> songs.take(2) // Mock recent subset
        else -> songs
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (selectedPlaylistId != null) {
            PlaylistDetailScreen(
                playlistId = selectedPlaylistId!!,
                onCollapse = { selectedPlaylistId = null },
                playlistViewModel = playlistViewModel,
                playerViewModel = viewModel
            )
        } else {

            if (isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141416))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter)
                        .zIndex(10f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                isSelectionMode = false
                                selectedSongs = emptySet()
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${selectedSongs.size} selected",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Add 📚",
                            color = Color(0xFFF9423A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                if (selectedSongs.isNotEmpty()) {
                                    showAddToPlaylistDialog = selectedSongs.first()
                                }
                            }
                        )
                        Text(
                            text = "Delete 🗑️",
                            color = Color(0xFFEF5350),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                if (selectedSongs.isNotEmpty()) {
                                    showDeleteConfirmationForSong = songs.find { it.id == selectedSongs.first() }
                                }
                            }
                        )
                    }
                }
            }

            // Scrollable Home Feed content using LazyColumn with shared LazyListState
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.height(76.dp)) // Offset to prevent top bar overlap when unscrolled
                }

                // Welcome Text header section (polished & spacious layout: text left, upload button right)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp), // Spacious padding
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "👋 Welcome Back",
                                color = Color(0x99FFFFFF), // Beautiful low opacity white
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To the Music Club 🎵",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.5).sp,
                                lineHeight = 34.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Polished Floating Upload Button (+)
                        Box(
                            modifier = Modifier
                                .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                                .size(36.dp) // slightly smaller
                                .background(Color(0xFFF9423A), CircleShape)
                                .clickable { onNavigateToUpload() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                // Now playing preview bar (soft premium glass container)
                item {
                    val currentSong = uiState.currentSong
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp) // Consistent 24dp horizontal padding
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0FFFFFFF), RoundedCornerShape(12.dp)) // Premium transparent glass card
                                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (currentSong != null && uiState.isPlaying) "🔊" else "🎵", 
                                fontSize = 15.sp,
                                color = Color(0xCCFFFFFF)
                            )
                            Text(
                                text = if (currentSong != null) {
                                    "${currentSong.title} - ${currentSong.artist ?: "Unknown"}"
                                } else {
                                    "Select a song to play"
                                },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Tabs selection row (Spotify-style chips)
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    ) {
                        val tabs = listOf(
                            Triple("all", "🎵 All", "all"),
                            Triple("liked", "❤️ Liked", "liked"),
                            Triple("recent", "🕐 Recent", "recent")
                        )
                        tabs.forEach { (id, label, _) ->
                            val isActive = activeTab == id
                            val bg by animateColorAsState(
                                targetValue = if (isActive) Color(0xFFF9423A) else Color(0x0FFFFFFF),
                                animationSpec = tween(durationMillis = 200),
                                label = "chipBg"
                            )
                            val borderColor by animateColorAsState(
                                targetValue = if (isActive) Color(0xFFF9423A) else Color(0x12FFFFFF),
                                animationSpec = tween(durationMillis = 200),
                                label = "chipBorder"
                            )
                            val textCol by animateColorAsState(
                                targetValue = if (isActive) Color.White else Color(0xFFCCCCCC),
                                animationSpec = tween(durationMillis = 200),
                                label = "chipText"
                            )

                            Box(
                                modifier = Modifier
                                    .background(bg, RoundedCornerShape(20.dp))
                                    .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
                                    .clickable { activeTab = id }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = textCol,
                                    fontSize = 12.sp,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Your Music Library header (Clean hierarchy & styling)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Your Music Library",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Default
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${filteredSongs.size} songs",
                            color = Color(0xFF9E9E9E), // Lower opacity count
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Song items
                items(filteredSongs) { song ->
                    val status = downloadStatuses[song.id] ?: DownloadStatus.NotDownloaded
                    SwipeToRevealSongRow(
                        song = song,
                        downloadStatus = status,
                        isActive = uiState.currentSong?.id == song.id,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedSongs.contains(song.id),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp), // Consistent 24dp horizontal padding
                        onToggleLike = { viewModel.toggleLikeSong(song) },
                        isRevealed = swipedSongId == song.id,
                        onReveal = { opened -> swipedSongId = if (opened) song.id else null },
                        onClick = {
                            if (isSelectionMode) {
                                selectedSongs = if (selectedSongs.contains(song.id)) {
                                    selectedSongs - song.id
                                } else {
                                    selectedSongs + song.id
                                }
                            } else {
                                viewModel.playSong(song)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedSongs = setOf(song.id)
                            }
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
                            showAddToPlaylistDialog = song.id
                        },
                        onDelete = {
                            showDeleteConfirmationForSong = song
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }

                // Playlists Header (Polished matching spacing)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📚", fontSize = 18.sp)
                            Text(
                                text = "Your Playlists",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Default
                            )
                        }
                        Text(
                            text = "+",
                            color = Color(0xFFF9423A),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showCreatePlaylistDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Playlist items
                if (playlists.isEmpty()) {
                    item {
                        Text(
                            text = "No playlists created yet. Click '+' to make one!",
                            color = Color(0xFF555555),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(playlists) { playlist ->
                        LegacyPlaylistRow(
                            playlist = playlist,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            onClick = { selectedPlaylistId = playlist.id }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp)) // Padding for MiniPlayer dock
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF9423A),
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            playlistViewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    }
                ) {
                    Text("CREATE", color = Color(0xFFF9423A))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newPlaylistName = ""
                        showCreatePlaylistDialog = false
                    }
                ) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = Color(0xFF141416)
        )
    }

    // Add to Playlist Dialog
    if (showAddToPlaylistDialog != null) {
        val songId = showAddToPlaylistDialog!!
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = null },
            title = { Text("Add Song to Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists created yet. Please create a playlist first.", color = Color(0xFF888888))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playlistViewModel.addSongToPlaylist(playlist.id, songId)
                                        showAddToPlaylistDialog = null
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = playlist.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistDialog = null }) {
                    Text("CLOSE", color = Color.White)
                }
            },
            containerColor = Color(0xFF141416)
        )
    }

    // Unified Song Options Bottom Sheet
    if (songOptionsTarget != null) {
        val targetSong = songOptionsTarget!!
        SongOptionsBottomSheet(
            song = targetSong,
            playlists = playlists,
            onDismissRequest = { songOptionsTarget = null },
            onPlayNext = { viewModel.playNext(targetSong) },
            onAddToQueue = { viewModel.addToQueue(targetSong) },
            onAddToPlaylist = { playlistId ->
                playlistViewModel.addSongToPlaylist(playlistId, targetSong.id)
            },
            onDeleteFromLibrary = {
                showDeleteConfirmationForSong = targetSong
                songOptionsTarget = null
            }
        )
    }

    // Delete Song Confirmation Dialog
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
                        viewModel.deleteSong(deletedSong.id)
                        showDeleteConfirmationForSong = null
                        if (isSelectionMode && selectedSongs.contains(deletedSong.id)) {
                            selectedSongs = selectedSongs - deletedSong.id
                            if (selectedSongs.isEmpty()) {
                                isSelectionMode = false
                            }
                        }
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
}
