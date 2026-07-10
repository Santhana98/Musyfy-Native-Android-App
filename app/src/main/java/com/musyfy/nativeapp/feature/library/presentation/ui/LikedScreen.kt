package com.musyfy.nativeapp.feature.library.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import kotlinx.coroutines.launch
import com.musyfy.nativeapp.feature.home.presentation.ui.SwipeToRevealSongRow
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import com.musyfy.nativeapp.feature.playlist.presentation.PlaylistViewModel
import com.musyfy.nativeapp.core.ui.components.PremiumThemeBackground
import com.musyfy.nativeapp.core.ui.components.SongOptionsBottomSheet
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import com.musyfy.nativeapp.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("title") } // "title" or "artist" or "duration"
    var showSortMenu by remember { mutableStateOf(false) }
    var swipedSongId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val songs by viewModel.songs.collectAsState()
    val uiState by viewModel.playbackUiState.collectAsState()
    val themeState by authViewModel.theme.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedSongs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var songOptionsTarget by remember { mutableStateOf<Song?>(null) }
    var showDeleteConfirmationForSong by remember { mutableStateOf<Song?>(null) }

    // Filter by search query and only show liked or downloaded songs (library subset)
    val filteredSongs = remember(songs, searchQuery, sortBy, downloadStatuses) {
        songs.filter { song ->
            val isDownloaded = downloadStatuses[song.id] is DownloadStatus.Downloaded
            (song.liked || isDownloaded) && (
                song.title.contains(searchQuery, ignoreCase = true) ||
                (song.artist?.contains(searchQuery, ignoreCase = true) == true)
            )
        }.sortedWith { a, b ->
            when (sortBy) {
                "artist" -> (a.artist ?: "").compareTo(b.artist ?: "", ignoreCase = true)
                "duration" -> b.durationMs.compareTo(a.durationMs)
                else -> a.title.compareTo(b.title, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        PremiumThemeBackground(themeState = themeState)

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
                                songOptionsTarget = songs.find { it.id == selectedSongs.first() }
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

        // Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp) // Premium 24dp horizontal margins
        ) {
            Spacer(modifier = Modifier.height(76.dp)) // Offset for Top App Bar

            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Library",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${filteredSongs.size} songs",
                        color = Color(0xFF9E9E9E), // Lower opacity count
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Sort Options Trigger Button
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Text(text = "Sort ↕", color = Color(0xFFF9423A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(Color(0xFF1E1E20))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Title", color = Color.White) },
                            onClick = {
                                sortBy = "title"
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Artist", color = Color.White) },
                            onClick = {
                                sortBy = "artist"
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Duration", color = Color.White) },
                            onClick = {
                                sortBy = "duration"
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(10.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedContainerColor = Color(0x1AFFFFFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFE53935)
                ),
                placeholder = {
                    Text(
                        text = "Search in your library...",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Song List
            if (filteredSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Your library is empty.\nLike or download songs to see them here!" else "No matching songs found.",
                        color = Color(0xFF666666),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = filteredSongs,
                        key = { it.id }
                    ) { song ->
                        val status = downloadStatuses[song.id] ?: DownloadStatus.NotDownloaded
                        SwipeToRevealSongRow(
                            song = song,
                            downloadStatus = status,
                            isActive = uiState.currentSong?.id == song.id,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedSongs.contains(song.id),
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
                                songOptionsTarget = song
                            },
                            onDelete = {
                                showDeleteConfirmationForSong = song
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Padding for mini-player overlap
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
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
