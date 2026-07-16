package com.musyfy.nativeapp.feature.player.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumProgressSection(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    textColor: Color,
    accentColor: Color,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    var isSeeking by remember { mutableStateOf(false) }
    var seekProgressPct by remember { mutableStateOf(0f) }

    val safeDurationMs = durationMs.coerceAtLeast(1L)
    val displayPositionMs = if (isSeeking) {
        (seekProgressPct * safeDurationMs).toLong()
    } else {
        currentPositionMs
    }

    val progressPct = if (isSeeking) {
        seekProgressPct
    } else {
        (currentPositionMs.toFloat() / safeDurationMs).coerceIn(0f, 1f)
    }

    // Animate thumb scale and track thickness based on dragging state
    val thumbRadiusPx by animateFloatAsState(
        targetValue = if (isSeeking) 8f * 3f else 5f * 3f, // 8dp vs 5dp
        animationSpec = tween(durationMillis = 150),
        label = "ThumbRadius"
    )
    val trackHeightPx by animateFloatAsState(
        targetValue = if (isSeeking) 5f else 3f, // 5px vs 3px
        animationSpec = tween(durationMillis = 150),
        label = "TrackHeight"
    )

    val formatTime: (Long) -> String = { ms ->
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        "$minutes:${if (seconds < 10) "0" else ""}$seconds"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Custom Canvas Slider with large touch target (height = 36.dp)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(safeDurationMs) {
                    detectTapGestures(
                        onPress = { offset ->
                            isSeeking = true
                            seekProgressPct = (offset.x / size.width).coerceIn(0f, 1f)
                            tryAwaitRelease()
                            isSeeking = false
                            onSeek((seekProgressPct * safeDurationMs).toLong())
                        }
                    )
                }
                .pointerInput(safeDurationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isSeeking = true
                            seekProgressPct = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isSeeking = false
                            onSeek((seekProgressPct * safeDurationMs).toLong())
                        },
                        onDragCancel = {
                            isSeeking = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            // Update percentage based on absolute position
                            val currentX = change.position.x
                            seekProgressPct = (currentX / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            
            // Draw background track
            drawLine(
                color = textColor.copy(alpha = 0.15f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round
            )

            // Draw active track
            val activeEndX = progressPct * width
            if (activeEndX > 0) {
                drawLine(
                    color = accentColor,
                    start = Offset(0f, centerY),
                    end = Offset(activeEndX, centerY),
                    strokeWidth = trackHeightPx,
                    cap = StrokeCap.Round
                )
            }

            // Draw thumb with subtle white center/glow
            drawCircle(
                color = accentColor,
                radius = thumbRadiusPx,
                center = Offset(activeEndX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx * 0.4f,
                center = Offset(activeEndX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(displayPositionMs),
                color = textColor.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
            Text(
                text = formatTime(durationMs),
                color = textColor.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
    }
}
