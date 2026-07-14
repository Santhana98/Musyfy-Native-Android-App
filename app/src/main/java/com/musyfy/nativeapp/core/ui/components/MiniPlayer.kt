package com.musyfy.nativeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musyfy.nativeapp.R

@Composable
fun MiniPlayerPlaceholder(
    modifier: Modifier = Modifier,
    songTitle: String = "Rihanna - Diamonds (Slow...",
    artistName: String = "Ominous",
    isPlaying: Boolean = false,
    progressPct: Float = 0.35f,
    imageUrl: Any? = null,
    currentTimeText: String = "0:00",
    durationText: String = "0:00",
    durationMs: Long = 0L,
    onSeek: (Long) -> Unit = {},
    onExpandClick: () -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    var width by remember { mutableStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xF2121214)) // Translucent premium dark background
    ) {
        // 1. Sleek, ultra-thin Progress Bar (2dp height, sits perfectly at the very top edge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0x1FFFFFFF)) // Subtle track background
                .onGloballyPositioned { width = it.size.width }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0) {
                            val ratio = (offset.x / width).coerceIn(0f, 1f)
                            onSeek((ratio * durationMs).toLong())
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressPct.coerceIn(0f, 1f))
                    .background(MaterialTheme.colorScheme.primary) // Dynamic progress
            )
        }

        // 2. Playback Info & Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // More rounded premium artwork container (48dp)
            AsyncImage(
                model = imageUrl ?: R.drawable.logo,
                contentDescription = "Song Artwork Thumbnail",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1E20))
                    .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )

            // Song Info metadata with clean premium typography
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = songTitle,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = artistName.ifEmpty { "Unknown Artist" },
                        color = Color(0xFFB3B3B3),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "✓",
                        color = Color(0xFFF9423A), // Brand color accent (Category A)
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (songTitle != "No Song Playing" && songTitle.isNotEmpty()) {
                        Text(
                            text = " • $currentTimeText / $durationText",
                            color = Color(0xFF8E8E93),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Controls Buttons Layout (Spotify-style aligned and padded)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Download button (⬇)
                Text(
                    text = "⬇",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 2.dp)
                )

                // Prev button (⏮)
                Text(
                    text = "⏮",
                    color = Color(0xFFB3B3B3),
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { onPrevClick() }
                )

                // Play/Pause button (40dp size with gorgeous premium brand gradient)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))),
                            CircleShape
                        )
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = if (isPlaying) 0.dp else 1.5.dp)
                    )
                }

                // Next button (⏭)
                Text(
                    text = "⏭",
                    color = Color(0xFFB3B3B3),
                    fontSize = 20.sp,
                    modifier = Modifier.clickable { onNextClick() }
                )
            }
        }
    }
}
