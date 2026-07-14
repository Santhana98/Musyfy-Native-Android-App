package com.musyfy.nativeapp.feature.player.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressSection(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSeeking by remember { mutableStateOf(false) }
    var localProgress by remember { mutableStateOf(0f) }

    // Use local progress value if user is seeking, else use current position from engine
    val activePosition = if (isSeeking) localProgress.toLong() else currentPositionMs
    val progressValue = activePosition.coerceIn(0L, durationMs).toFloat()

    val formatTime: (Long) -> String = { ms ->
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        "$minutes:${if (seconds < 10) "0" else ""}$seconds"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // Seek Slider
        Slider(
            value = progressValue,
            onValueChange = {
                isSeeking = true
                localProgress = it
            },
            onValueChangeFinished = {
                isSeeking = false
                onSeek(localProgress.toLong())
            },
            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Time Durations Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(activePosition),
                color = Color(0xFF888888),
                fontSize = 13.sp
            )
            Text(
                text = formatTime(durationMs),
                color = Color(0xFF888888),
                fontSize = 13.sp
            )
        }
    }
}
