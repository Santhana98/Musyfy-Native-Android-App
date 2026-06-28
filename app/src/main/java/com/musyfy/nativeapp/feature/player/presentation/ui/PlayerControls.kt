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
import androidx.compose.material3.Text
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
    onPlayPauseClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Previous Button
        Text(
            text = "⏮",
            color = Color(0xFFCCCCCC),
            fontSize = 32.sp,
            modifier = Modifier
                .clickable { onPrevClick() }
                .padding(8.dp)
        )

        // 2. Play/Pause Button (68dp circular red-gradient button)
        Box(
            modifier = Modifier
                .size(68.dp)
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. Next Button
        Text(
            text = "⏭",
            color = Color(0xFFCCCCCC),
            fontSize = 32.sp,
            modifier = Modifier
                .clickable { onNextClick() }
                .padding(8.dp)
        )
    }
}
