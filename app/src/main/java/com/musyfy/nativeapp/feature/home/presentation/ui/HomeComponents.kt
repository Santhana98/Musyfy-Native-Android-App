package com.musyfy.nativeapp.feature.home.presentation.ui

import androidx.compose.foundation.Image
import com.musyfy.nativeapp.domain.model.Song
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R

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
                text = "Musy-Fi",
                color = Color(0xFFE53935),
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

@Composable
fun LegacySongRow(
    song: Song,
    downloadStatus: DownloadStatus,
    isActive: Boolean = false,
    onClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) Color(0x1AE53935) else Color.Transparent
    val border = if (isActive) Color(0x33E53935) else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        val defaultLocalArt = File(context.filesDir, "${song.id}.jpg")
        val resolvedArtModel: Any? = when {
            !song.artworkPath.isNullOrEmpty() && File(song.artworkPath).exists() -> {
                android.util.Log.d("MusyfyPlayback", "Artwork [HomeComponents]: song.id=${song.id}, artworkPath=${song.artworkPath}, exists=true, loading from local artworkPath")
                File(song.artworkPath)
            }
            defaultLocalArt.exists() -> {
                android.util.Log.d("MusyfyPlayback", "Artwork [HomeComponents]: song.id=${song.id}, defaultLocalArtPath=${defaultLocalArt.absolutePath}, exists=true, loading from local filesDir")
                defaultLocalArt
            }
            !song.imageUrl.isNullOrEmpty() -> {
                android.util.Log.d("MusyfyPlayback", "Artwork [HomeComponents]: song.id=${song.id}, remoteUrl=${song.imageUrl}, loading from remote imageUrl")
                song.imageUrl
            }
            else -> {
                android.util.Log.d("MusyfyPlayback", "Artwork [HomeComponents]: song.id=${song.id}, no artwork available, showing placeholder")
                null
            }
        }

        if (resolvedArtModel != null) {
            AsyncImage(
                model = resolvedArtModel,
                contentDescription = "Song Artwork Thumbnail",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.logo),
                placeholder = painterResource(id = R.drawable.logo)
            )
        } else {
            // Album art placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎵", fontSize = 20.sp)
            }
        }

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = if (isActive) Color(0xFFE53935) else Color.White,
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
                        color = Color(0xFFE53935),
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
                .clickable { }
                .padding(4.dp)
        )

        // Dropdown option menu
        Text(
            text = "⋮",
            color = Color(0xFF666666),
            fontSize = 18.sp,
            modifier = Modifier
                .clickable { }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LegacyPlaylistRow(
    playlist: MockPlaylist,
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
                text = "${playlist.songCount} ${if (playlist.songCount == 1) "song" else "songs"}",
                color = Color(0xFF666666),
                fontSize = 12.sp
            )
        }
    }
}
