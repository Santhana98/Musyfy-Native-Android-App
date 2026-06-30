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

@Composable
fun HomeScreen(
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    var activeTab by remember { mutableStateOf("all") }
    val scrollState = rememberScrollState()

    val songs by viewModel.songs.collectAsState()
    val uiState by viewModel.playbackUiState.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()

    // Filter songs based on active tab
    val filteredSongs = when (activeTab) {
        "liked" -> songs.filter { it.liked }
        "recent" -> songs.take(2) // Mock recent subset
        else -> songs
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Theme Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_male),
            contentDescription = "Theme Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Linear Gradient Overlay mimicking globals.css
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

        // 3. Scrollable Home Feed content (scrolled beneath the fixed "+" button)
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
                    LegacySongRow(
                        song = song,
                        downloadStatus = status,
                        isActive = uiState.currentSong?.id == song.id,
                        onClick = { viewModel.playSong(song) },
                        onDownloadClick = {
                            if (status is DownloadStatus.Downloaded) {
                                viewModel.deleteDownloadedSong(song.id)
                            } else if (status !is DownloadStatus.Downloading) {
                                viewModel.startDownload(song)
                            }
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
                        .clickable { }
                        .padding(horizontal = 8.dp)
                )
            }

            // Playlist items
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mockPlaylistsList.forEach { playlist ->
                    LegacyPlaylistRow(playlist = playlist)
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
