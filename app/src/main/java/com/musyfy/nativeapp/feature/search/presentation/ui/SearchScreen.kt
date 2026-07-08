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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.components.SongOptionsBottomSheet
import com.musyfy.nativeapp.domain.model.Playlist
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
    val bgImageRes = if (themeState == "male") R.drawable.bg_male else R.drawable.bg_female

    val songs by viewModel.songs.collectAsState()
    val uiState by viewModel.playbackUiState.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()

    var songOptionsTarget by remember { mutableStateOf<Song?>(null) }

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
            .background(Color(0xFF070708))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Hero Header with Theme Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = bgImageRes),
                    contentDescription = "Theme Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Hero Linear Gradient Overlay matching search/page.tsx
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x4D000000), // rgba(0,0,0,0.3)
                                    Color(0xD90A0A0A), // rgba(10,10,10,0.85)
                                    Color(0xFF070708)  // Base dark background
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Logo + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Musy-Fi Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Musy-Fi Search",
                            color = Color(0xFFE53935),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                    }

                    // Input search box
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search by song name or artist...", color = Color(0x8CFFFFFF)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x1AFFFFFF),
                            unfocusedContainerColor = Color(0x1AFFFFFF),
                            focusedBorderColor = Color(0x4DFFFFFF),
                            unfocusedBorderColor = Color(0x26FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Results list area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (query.trim().isEmpty()) {
                    // Empty search screen helper
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 42.sp,
                            color = Color(0xFF555555)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Search your personal music library above",
                            color = Color(0xFF555555),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
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
                                            viewModel.deleteSong(song.id)
                                        },
                                        onToggleLike = {
                                            viewModel.toggleLikeSong(song)
                                        }
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
                onDeleteFromLibrary = { viewModel.deleteSong(songOptionsTarget!!.id) }
            )
        }
    }
}
