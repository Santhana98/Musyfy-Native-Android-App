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
import androidx.compose.material3.Text
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
            .background(Color(0xF60F0505)) // rgba(15,5,5,0.97)
    ) {
        HorizontalDivider(color = Color(0xFF2A1010), thickness = 1.dp) // border-top: 1px solid #2a1010
        Spacer(modifier = Modifier.height(6.dp)) // 6-8dp top padding above the progress indicator

        // 1. Interactive Thin Red Progress Bar (visually 2dp-4dp, with seek gestures)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp) // visual height
                .background(Color(0xFF2A1010)) // track background
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
                    .background(Color(0xFFE53935)) // active progress track
            )
        }

        // 2. Playback Info & Controls Row (exactly matches original proportions)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp), // Revert to exact legacy padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail artwork container (44dp exactly, supports remote & local sources)
            AsyncImage(
                model = imageUrl ?: R.drawable.logo,
                contentDescription = "Song Artwork Thumbnail",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )

            // Song Info metadata
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = songTitle,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = artistName.ifEmpty { "Unknown Artist" },
                        color = Color(0xFF666666),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "✓",
                        color = Color(0xFF1DB954),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (songTitle != "No Song Playing" && songTitle.isNotEmpty()) {
                        Text(
                            text = " • $currentTimeText / $durationText",
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Controls Buttons Layout (Revert to exact legacy gap 12dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download button (⬇)
                Text(
                    text = "⬇",
                    color = Color(0xFFE53935),
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 2.dp)
                )

                // Prev button (⏮)
                Text(
                    text = "⏮",
                    color = Color(0xFFAAAAAA),
                    fontSize = 22.sp,
                    modifier = Modifier.clickable { onPrevClick() }
                )

                // Play/Pause button (38dp exactly)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFE53935), Color(0xFFC62828))),
                            CircleShape
                        )
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Next button (⏭)
                Text(
                    text = "⏭",
                    color = Color(0xFFAAAAAA),
                    fontSize = 22.sp,
                    modifier = Modifier.clickable { onNextClick() }
                )
            }
        }
    }
}
