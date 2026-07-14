package com.musyfy.nativeapp.feature.player.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onPlayPauseClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Shuffle Toggle Button
        Text(
            text = "🔀",
            color = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else Color(0xFF666666),
            fontSize = 22.sp,
            modifier = Modifier
                .clickable { onShuffleToggle() }
                .padding(8.dp)
        )

        // 2. Previous Button
        Text(
            text = "⏮",
            color = Color(0xFFCCCCCC),
            fontSize = 32.sp,
            modifier = Modifier
                .clickable { onPrevClick() }
                .padding(8.dp)
        )

        // 3. Play/Pause Button (68dp circular red-gradient button)
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))),
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
                    .size(32.dp)
                    .padding(start = if (isPlaying) 0.dp else 2.5.dp)
            )
        }

        // 4. Next Button
        Text(
            text = "⏭",
            color = Color(0xFFCCCCCC),
            fontSize = 32.sp,
            modifier = Modifier
                .clickable { onNextClick() }
                .padding(8.dp)
        )

        // 5. Repeat Toggle Button
        val repeatText = when (repeatMode) {
            1 -> "🔂"
            else -> "🔁"
        }
        val repeatColor = if (repeatMode != 0) MaterialTheme.colorScheme.primary else Color(0xFF666666)
        Text(
            text = repeatText,
            color = repeatColor,
            fontSize = 22.sp,
            modifier = Modifier
                .clickable { onRepeatToggle() }
                .padding(8.dp)
        )
    }
}
