package com.musyfy.nativeapp.feature.home.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import coil.compose.AsyncImage
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.domain.model.Playlist
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import java.io.File
import kotlin.math.roundToInt

data class MockSong(
    val id: String,
    val title: String,
    val artist: String?,
    val liked: Boolean = false,
    val importStatus: String = "ready"
)

val mockSongsList = listOf(
    MockSong("1", "Vibe Session", "Musyfy Artist", true),
    MockSong("2", "Cassette Rewind", "Cassette Player", false),
    MockSong("3", "Midnight Ride", "Synthwave DJ", false),
    MockSong("4", "Lo-Fi Coffee", "Beatmaker Chill", true),
    MockSong("5", "Acoustic Sun", "Folksy Singer", false)
)

data class MockPlaylist(
    val id: String,
    val name: String,
    val songCount: Int
)

val mockPlaylistsList = listOf(
    MockPlaylist("1", "My Playlist #1", 5),
    MockPlaylist("2", "Chill Mix", 12),
    MockPlaylist("3", "Workout Beats", 8)
)

@Composable
fun LegacyHomeHeader(
    userName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Brand Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Musy-Fi Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Musyfy",
                color = Color(0xFFF9423A),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )
        }
        
        // Profile Pill
        Row(
            modifier = Modifier
                .background(Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "👤", fontSize = 13.sp)
            Text(
                text = userName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LegacySongRow(
    song: Song,
    downloadStatus: DownloadStatus,
    isActive: Boolean = false,
    onClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onOptionClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleLike: () -> Unit = {}
) {
    val bg = if (isSelected) Color(0x26F9423A) else if (isActive) Color(0x1AF9423A) else Color.Transparent
    val border = if (isSelected) Color(0x66F9423A) else if (isActive) Color(0x33F9423A) else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFF9423A),
                    uncheckedColor = Color(0x4DFFFFFF),
                    checkmarkColor = Color.White
                )
            )
        }

        val context = LocalContext.current
        val defaultLocalArt = File(context.filesDir, "${song.id}.jpg")
        val resolvedArtModel: Any? = when {
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

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            if (resolvedArtModel != null) {
                AsyncImage(
                    model = resolvedArtModel,
                    contentDescription = "Song Artwork Thumbnail",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.logo),
                    placeholder = painterResource(id = R.drawable.logo)
                )
            } else {
                Text(text = "🎵", fontSize = 20.sp)
            }

            if (isActive && !isSelectionMode) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x99F9423A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "▶", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = if (isActive) Color(0xFFF9423A) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist ?: "Unknown",
                color = Color(0xFF666666),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!isSelectionMode) {
            // Download Action Button
            Box(
                modifier = Modifier
                    .clickable(onClick = onDownloadClick)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                when (downloadStatus) {
                    is DownloadStatus.NotDownloaded -> {
                        Text(text = "⬇", color = Color(0xFF666666), fontSize = 16.sp)
                    }
                    is DownloadStatus.Downloading -> {
                        CircularProgressIndicator(
                            progress = { downloadStatus.progress },
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFFF9423A),
                            strokeWidth = 2.dp
                        )
                    }
                    is DownloadStatus.Downloaded -> {
                        Text(text = "✅", color = Color(0xFF4CAF50), fontSize = 16.sp)
                    }
                    is DownloadStatus.Error -> {
                        Text(text = "⚠️", color = Color(0xFFEF5350), fontSize = 16.sp)
                    }
                }
            }

            // Like Button
            Text(
                text = if (song.liked) "❤️" else "🤍",
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { onToggleLike() }
                    .padding(4.dp)
            )

            // Dropdown option menu
            Text(
                text = "⋮",
                color = Color(0xFF666666),
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable { onOptionClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SwipeToRevealSongRow(
    song: Song,
    downloadStatus: DownloadStatus,
    isActive: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onOptionClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleLike: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var offsetX by remember { mutableStateOf(0f) }
    val maxRevealWidth = 160f // Total width of hidden actions

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
    ) {
        // Revealed Actions (only rendered when swiped to prevent showing through the transparent card)
        if (offsetX < 0f) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayNext()
                        offsetX = 0f
                    },
                    modifier = Modifier.size(40.dp).background(Color(0x1AF9423A), CircleShape)
                ) {
                    Text("⏭️", fontSize = 14.sp)
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddToPlaylist()
                        offsetX = 0f
                    },
                    modifier = Modifier.size(40.dp).background(Color(0x1AF9423A), CircleShape)
                ) {
                    Text("📚", fontSize = 14.sp)
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                        offsetX = 0f
                    },
                    modifier = Modifier.size(40.dp).background(Color(0x1AEF5350), CircleShape)
                ) {
                    Text("🗑️", fontSize = 14.sp)
                }
            }
        }

        val dragState = rememberDraggableState { delta ->
            if (!isSelectionMode) {
                offsetX = (offsetX + delta).coerceIn(-maxRevealWidth, 0f)
            }
        }

        LegacySongRow(
            song = song,
            downloadStatus = downloadStatus,
            isActive = isActive,
            onClick = {
                if (offsetX < -10f) {
                    offsetX = 0f
                } else {
                    onClick()
                }
            },
            onDownloadClick = onDownloadClick,
            onOptionClick = onOptionClick,
            isSelectionMode = isSelectionMode,
            isSelected = isSelected,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            },
            onToggleLike = onToggleLike,
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = !isSelectionMode,
                    onDragStopped = {
                        offsetX = if (offsetX < -maxRevealWidth / 2f) -maxRevealWidth else 0f
                    }
                )
        )
    }
}

@Composable
fun LegacyPlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0x0AFFFFFF), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📚", fontSize = 20.sp)
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${playlist.songIds.size} ${if (playlist.songIds.size == 1) "song" else "songs"}",
                color = Color(0xFF666666),
                fontSize = 12.sp
            )
        }
    }
}
