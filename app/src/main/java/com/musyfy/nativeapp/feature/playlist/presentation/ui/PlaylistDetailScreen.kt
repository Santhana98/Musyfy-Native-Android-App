package com.musyfy.nativeapp.feature.playlist.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.domain.model.Song
import kotlinx.coroutines.launch
import com.musyfy.nativeapp.feature.home.presentation.ui.SwipeToRevealSongRow
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import com.musyfy.nativeapp.feature.playlist.presentation.PlaylistViewModel
import com.musyfy.nativeapp.core.ui.components.SongOptionsBottomSheet
import com.musyfy.nativeapp.core.ui.components.PremiumThemeBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val haptic = LocalHapticFeedback.current
    val playlistState = playlistViewModel.getPlaylistById(playlistId).collectAsState(initial = null)
    val playlist = playlistState.value

    val songs by playerViewModel.songs.collectAsState()
    val uiState by playerViewModel.playbackUiState.collectAsState()
    val downloadStatuses by playerViewModel.downloadStatuses.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()

    val playlistSongs = remember(songs, playlist) {
        playlist?.songIds?.mapNotNull { id -> songs.find { it.id == id } } ?: emptyList()
    }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var songOptionsTarget by remember { mutableStateOf<Song?>(null) }
    var showDeleteConfirmationForSong by remember { mutableStateOf<Song?>(null) }
    var swipedSongId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedSongs by remember { mutableStateOf<Set<String>>(emptySet()) }

    PremiumThemeBackground(
        themeState = "",
        modifier = modifier.fillMaxSize()
    ) {

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
                        text = "Remove ❌",
                        color = Color(0xFFEF5350),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            selectedSongs.forEach { id ->
                                playlistViewModel.removeSongFromPlaylist(playlistId, id)
                            }
                            isSelectionMode = false
                            selectedSongs = emptySet()
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp) // Premium 24dp horizontal margins
        ) {
            Spacer(modifier = Modifier.height(76.dp)) // Offset for Top App Bar

            // Back Arrow Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Back",
                    color = MaterialTheme.colorScheme.primary, // Brand Red back action
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onCollapse() }
                )
            }

            if (playlist == null) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = playlist.name,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold, // SemiBold weight
                    letterSpacing = (-0.5).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${playlistSongs.size} songs",
                    color = Color(0xFF9E9E9E), // Lower opacity count
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Play All Button (gorgeous brand red)
                    Button(
                        onClick = {
                            if (playlistSongs.isNotEmpty() && playlist != null) {
                                val startingSong = playlistSongs.first()
                                val repeatModeStr = when (uiState.repeatMode) {
                                    1 -> "one"
                                    2 -> "all"
                                    else -> "off"
                                }
                                playlistViewModel.trackPlaylistPlayStarted(
                                    playlist = playlist,
                                    startingSong = startingSong,
                                    positionIndex = 0,
                                    playSource = "Playlist",
                                    shuffleEnabled = uiState.shuffleModeEnabled,
                                    repeatMode = repeatModeStr
                                )
                                playerViewModel.playSong(startingSong, queue = playlistSongs)
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "▶ PLAY ALL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Edit Button (Rename)
                    Button(
                        onClick = {
                            renameValue = playlist.name
                            showRenameDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x0FFFFFFF)),
                        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "✏️ RENAME", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    // Delete Button
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x0FFFFFFF)),
                        border = BorderStroke(1.dp, Color(0x12FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "🗑 DELETE", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Songs List
                if (playlistSongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs inside this playlist yet.\nGo to Home tab and tap options to add songs!",
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
                        itemsIndexed(
                            items = playlistSongs,
                            key = { _, song -> song.id }
                        ) { index, song ->
                            val status = downloadStatuses[song.id] ?: DownloadStatus.NotDownloaded
                            var dragOffsetY by remember { mutableStateOf(0f) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Drag Handle on Left of Row
                                if (!isSelectionMode) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(36.dp)
                                            .background(Color(0x0AFFFFFF), CircleShape)
                                            .pointerInput(index) {
                                                detectDragGestures(
                                                    onDragStart = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        dragOffsetY = 0f
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffsetY += dragAmount.y
                                                        // Move item up/down on substantial drag
                                                        if (dragOffsetY > 120f) {
                                                            if (index < playlistSongs.size - 1) {
                                                                playlistViewModel.reorderPlaylistSongs(playlistId, index, index + 1)
                                                            }
                                                            dragOffsetY = 0f
                                                        } else if (dragOffsetY < -120f) {
                                                            if (index > 0) {
                                                                playlistViewModel.reorderPlaylistSongs(playlistId, index, index - 1)
                                                            }
                                                            dragOffsetY = 0f
                                                        }
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("☰", color = Color(0xFF888888), fontSize = 16.sp)
                                    }
                                }

                                SwipeToRevealSongRow(
                                    song = song,
                                    downloadStatus = status,
                                    isActive = uiState.currentSong?.id == song.id,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedSongs.contains(song.id),
                                    onToggleLike = { playerViewModel.toggleLikeSong(song, source = "Playlist") },
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
                                            if (playlist != null) {
                                                val repeatModeStr = when (uiState.repeatMode) {
                                                    1 -> "one"
                                                    2 -> "all"
                                                    else -> "off"
                                                }
                                                playlistViewModel.trackPlaylistPlayStarted(
                                                    playlist = playlist,
                                                    startingSong = song,
                                                    positionIndex = index,
                                                    playSource = "Playlist",
                                                    shuffleEnabled = uiState.shuffleModeEnabled,
                                                    repeatMode = repeatModeStr
                                                )
                                            }
                                            playerViewModel.playSong(song, queue = playlistSongs)
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
                                            playerViewModel.deleteDownloadedSong(song.id)
                                        } else if (status !is DownloadStatus.Downloading) {
                                            playerViewModel.startDownload(song)
                                        }
                                    },
                                    onOptionClick = {
                                        songOptionsTarget = song
                                    },
                                    onPlayNext = {
                                        playerViewModel.playNext(song)
                                    },
                                    onAddToPlaylist = {
                                        songOptionsTarget = song
                                    },
                                    onDelete = {
                                        playlistViewModel.removeSongFromPlaylist(playlistId, song.id)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
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

    // Rename Playlist Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameValue.isNotBlank()) {
                            playlistViewModel.renamePlaylist(playlistId, renameValue)
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("SAVE", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = Color(0xFF141416)
        )
    }

    // Delete Playlist Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Playlist?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this playlist? This action cannot be undone.", color = Color(0xFF888888)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        playlistViewModel.deletePlaylist(playlistId)
                        showDeleteConfirmDialog = false
                        onCollapse()
                    }
                ) {
                    Text("DELETE", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = Color(0xFF141416)
        )
    }

    // Unified Song Options Bottom Sheet (with Remove from Playlist action)
    if (songOptionsTarget != null) {
        val targetSong = songOptionsTarget!!
        SongOptionsBottomSheet(
            song = targetSong,
            playlists = playlists,
            onDismissRequest = { songOptionsTarget = null },
            onPlayNext = { playerViewModel.playNext(targetSong) },
            onAddToQueue = { playerViewModel.addToQueue(targetSong) },
            onAddToPlaylist = { pId ->
                playlistViewModel.addSongToPlaylist(pId, targetSong.id)
            },
            onDeleteFromLibrary = {
                showDeleteConfirmationForSong = targetSong
                songOptionsTarget = null
            },
            onRemoveFromPlaylist = {
                playlistViewModel.removeSongFromPlaylist(playlistId, targetSong.id)
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
                        playerViewModel.deleteSong(deletedSong.id)
                        showDeleteConfirmationForSong = null
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Song deleted",
                                actionLabel = "UNDO",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                playerViewModel.restoreSong(deletedSong)
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
