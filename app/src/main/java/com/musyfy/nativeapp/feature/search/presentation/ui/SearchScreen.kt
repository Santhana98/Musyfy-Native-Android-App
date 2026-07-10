package com.musyfy.nativeapp.feature.search.presentation.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.components.SongOptionsBottomSheet
import com.musyfy.nativeapp.core.ui.components.PremiumThemeBackground
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import com.musyfy.nativeapp.feature.home.presentation.ui.SwipeToRevealSongRow
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import com.musyfy.nativeapp.feature.playlist.presentation.PlaylistViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val themeState by authViewModel.theme.collectAsState()

    val songs by viewModel.songs.collectAsState()
    val uiState by viewModel.playbackUiState.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()

    var songOptionsTarget by remember { mutableStateOf<Song?>(null) }
    var swipedSongId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteConfirmationForSong by remember { mutableStateOf<Song?>(null) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        PremiumThemeBackground(themeState = themeState)

        // Content Area matching LikedScreen.kt hierarchy exactly
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(76.dp)) // Offset for Top App Bar matching LikedScreen

            // Header Title Block
            Column {
                Text(
                    text = "Search",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Search your personal music library",
                    color = Color(0xFF9E9E9E), // Higher contrast gray
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // Spacing matching LikedScreen

            // Polished Spotify/Nothing OS-style capsule search field (shorter, glassmorphic)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by song name or artist...", color = Color(0x66FFFFFF)) },
                leadingIcon = { Text("🔍", fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { query = "" }
                                .padding(12.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp), // Premium Material 3/Nothing OS corner radius
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1AFFFFFF), // Glass transparency
                    unfocusedContainerColor = Color(0x0FFFFFFF),
                    focusedBorderColor = Color(0x4DF9423A), // Subtle brand halo border
                    unfocusedBorderColor = Color(0x10FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp)) // Spacing matching LikedScreen

            // Results list area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (query.trim().isEmpty()) {
                    // Premium Empty State Design
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0x1AE53935), Color(0x05FFFFFF))
                                    ),
                                    CircleShape
                                )
                                .border(1.dp, Color(0x1AE53935), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔍",
                                fontSize = 38.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Discover Your Music",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Search your personal music library by song title or artist to find your favorites.",
                            color = Color(0xFF666666),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Search Results (${filteredSongs.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (filteredSongs.isEmpty()) {
                            Text(
                                text = "No songs found matching your search.",
                                color = Color(0xFF555555),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(filteredSongs) { index, song ->
                                    val status = downloadStatuses[song.id] ?: DownloadStatus.NotDownloaded
                                    SwipeToRevealSongRow(
                                        song = song,
                                        downloadStatus = status,
                                        isActive = uiState.currentSong?.id == song.id,
                                        onClick = {
                                            viewModel.playSong(song)
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
                                            viewModel.toggleLikeSong(song)
                                        },
                                        isRevealed = swipedSongId == song.id,
                                        onReveal = { opened -> swipedSongId = if (opened) song.id else null }
                                    )
                                }
                            }
                        }
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
                            viewModel.deleteSong(deletedSong.id)
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
