package com.musyfy.nativeapp.feature.player.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.shape.CircleShape
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel

@Composable
fun QueueOverlayScreen(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.playbackUiState.collectAsState()
    val queue = uiState.queue

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE60A0A0C)) // Semi-transparent dark background
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(30.dp)) // Offset for Top App Bar

            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Play Queue",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${queue.size} songs",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.clearQueue() }
                    ) {
                        Text(text = "CLEAR ALL", color = Color(0xFFE53935), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = onClose
                    ) {
                        Text(text = "CLOSE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your queue is empty.",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(
                        items = queue,
                        key = { index, song -> "${song.id}_$index" }
                    ) { index, song ->
                        val isActive = uiState.currentSong?.id == song.id
                        val bg = if (isActive) Color(0x1AE53935) else Color(0x0AFFFFFF)
                        val borderCol = if (isActive) Color(0x33E53935) else Color(0x1AFFFFFF)
                        var dragOffsetY by remember { mutableStateOf(0f) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg, RoundedCornerShape(10.dp))
                                .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                .clickable { viewModel.playSong(song) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drag Handle (☰)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x0AFFFFFF), CircleShape)
                                    .pointerInput(index) {
                                        detectDragGestures(
                                            onDragStart = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                if (dragOffsetY > 120f) {
                                                    if (index < queue.size - 1) {
                                                        viewModel.reorderQueue(index, index + 1)
                                                    }
                                                    dragOffsetY = 0f
                                                } else if (dragOffsetY < -120f) {
                                                    if (index > 0) {
                                                        viewModel.reorderQueue(index, index - 1)
                                                    }
                                                    dragOffsetY = 0f
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("☰", color = Color(0xFF888888), fontSize = 16.sp)
                            }

                            // Song Metadata
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
                                Text(
                                    text = song.artist ?: "Unknown Artist",
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Remove button
                            Text(
                                text = "✕",
                                color = Color(0xFF666666),
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .clickable { viewModel.removeFromQueue(song.id) }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Dock padding
        }
    }
}
