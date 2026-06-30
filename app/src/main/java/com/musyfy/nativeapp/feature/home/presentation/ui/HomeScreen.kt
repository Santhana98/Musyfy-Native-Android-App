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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.components.SongOptionsBottomSheet
import com.musyfy.nativeapp.domain.model.Song
import androidx.compose.ui.zIndex

@Composable
fun HomeScreen(
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    var activeTab by remember { mutableStateOf("all") }
    val scrollState = rememberScrollState()

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
            // Theme Background Image
            Image(
                painter = painterResource(id = R.drawable.bg_male),
                contentDescription = "Theme Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Linear Gradient Overlay mimicking globals.css
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x8C000000), // rgba(0,0,0,0.55)
                                Color(0xF20A0A0A)  // rgba(10,10,10,0.95)
                            )
                        )
                    )
            )

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
                            color = Color(0xFFE53935),
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

            // Scrollable Home Feed content (scrolled beneath the fixed "+" button)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(56.dp)) // Offset to prevent top bar overlap when unscrolled
                
                // Welcome Text header section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Welcome back",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    // Now playing preview pill chip
                    val currentSong = uiState.currentSong
                    if (currentSong != null) {
                        Row(
                            modifier = Modifier
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(10.dp))
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = if (uiState.isPlaying) "🔊" else "🎵", fontSize = 18.sp)
                            Text(
                                text = "${currentSong.title} - ${currentSong.artist ?: "Unknown"}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(10.dp))
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🎵", fontSize = 18.sp)
                            Text(
                                text = "Select a song to play",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs selection row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val tabs = listOf(
                        Triple("all", "🎵 All", "all"),
                        Triple("liked", "❤️ Liked", "liked"),
                        Triple("recent", "🕐 Recent", "recent")
                    )
                    tabs.forEach { (id, label, _) ->
                        val isActive = activeTab == id
                        val bg = if (isActive) Color(0xFFE53935) else Color(0x0DFFFFFF)
                        val border = if (isActive) Color.Transparent else Color(0xFF2A2A2A)
                        val textCol = if (isActive) Color.White else Color(0xFF888888)

                        Box(
                            modifier = Modifier
                                .background(bg, RoundedCornerShape(20.dp))
                                .border(1.dp, border, RoundedCornerShape(20.dp))
                                .clickable { activeTab = id }
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = textCol,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Your Music Library header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Music Library",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${filteredSongs.size} songs",
                        color = Color(0xFF555555),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Song items
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    filteredSongs.forEach { song ->
                        val status = downloadStatuses[song.id] ?: DownloadStatus.NotDownloaded
                        com.musyfy.nativeapp.feature.home.presentation.ui.SwipeToRevealSongRow(
                            song = song,
                            downloadStatus = status,
                            isActive = uiState.currentSong?.id == song.id,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedSongs.contains(song.id),
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
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Playlists Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "+",
                        color = Color(0xFFE53935),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clickable { showCreatePlaylistDialog = true }
                            .padding(horizontal = 8.dp)
                    )
                }

                // Playlist items
                if (playlists.isEmpty()) {
                    Text(
                        text = "No playlists created yet. Click '+' to make one!",
                        color = Color(0xFF555555),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        playlists.forEach { playlist ->
                            LegacyPlaylistRow(
                                playlist = playlist,
                                onClick = { selectedPlaylistId = playlist.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Padding for MiniPlayer dock
            }

            // 4. Fixed + (Add to Library) Button at top-right aligned with "Welcome back" text (accounting for Top App Bar)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 20.dp) // Repositioned to 80dp top padding to align with "Welcome back"
                    .size(42.dp)
                    .background(Color(0xFFE53935), CircleShape)
                    .clickable { onNavigateToUpload() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
                        focusedBorderColor = Color(0xFFE53935),
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
                    Text("CREATE", color = Color(0xFFE53935))
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
                        viewModel.deleteSong(targetSong.id)
                        showDeleteConfirmationForSong = null
                        if (isSelectionMode && selectedSongs.contains(targetSong.id)) {
                            selectedSongs = selectedSongs - targetSong.id
                            if (selectedSongs.isEmpty()) {
                                isSelectionMode = false
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
