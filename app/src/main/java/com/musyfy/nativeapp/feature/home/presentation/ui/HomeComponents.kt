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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.collectIsPressedAsState

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
    val bg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color(0x0AFFFFFF)
    val border = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color(0x12FFFFFF)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
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
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E1E20))
                .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp)),
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
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "▶", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // Title and Artist with clean modern typography
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist ?: "Unknown",
                color = Color(0xFF9E9E9E),
                fontSize = 13.sp,
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
                        Text(text = "⬇", color = Color(0xFF9E9E9E), fontSize = 16.sp)
                    }
                    is DownloadStatus.Downloading -> {
                        CircularProgressIndicator(
                            progress = { downloadStatus.progress },
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
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
                color = if (song.liked) MaterialTheme.colorScheme.primary else Color(0xFF9E9E9E),
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { onToggleLike() }
                    .padding(4.dp)
            )

            // Dropdown option menu
            Text(
                text = "⋮",
                color = Color(0xFF9E9E9E),
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
    onToggleLike: () -> Unit = {},
    isRevealed: Boolean = false,
    onReveal: (Boolean) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxRevealWidth = with(density) { 180.dp.toPx() }
    val animatableOffset = remember { Animatable(0f) }

    LaunchedEffect(isSelectionMode, isRevealed) {
        if (isSelectionMode || !isRevealed) {
            animatableOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
        } else if (isRevealed && animatableOffset.value == 0f) {
            animatableOffset.animateTo(-maxRevealWidth, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (animatableOffset.value < 0f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color(0xD90F0F11), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val buttonWidth = 60.dp

                // Action 1: Like / Unlike
                val likeInteraction = remember { MutableInteractionSource() }
                val isLikePressed by likeInteraction.collectIsPressedAsState()
                val isLiked = song.liked
                val likeIconColor = if (isLikePressed || isLiked) MaterialTheme.colorScheme.primary else Color.White
                Box(
                    modifier = Modifier
                        .width(buttonWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = likeInteraction,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleLike()
                                coroutineScope.launch {
                                    animatableOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    onReveal(false)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLiked) "❤️" else "🤍",
                        fontSize = 18.sp,
                        color = likeIconColor
                    )
                }

                // Action 2: Add to Playlist
                val addInteraction = remember { MutableInteractionSource() }
                val isAddPressed by addInteraction.collectIsPressedAsState()
                val addIconColor = if (isAddPressed) MaterialTheme.colorScheme.primary else Color.White
                Box(
                    modifier = Modifier
                        .width(buttonWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = addInteraction,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddToPlaylist()
                                coroutineScope.launch {
                                    animatableOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    onReveal(false)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "➕",
                        fontSize = 16.sp,
                        color = addIconColor
                    )
                }

                // Action 3: Delete Download / Delete
                val deleteInteraction = remember { MutableInteractionSource() }
                val isDeletePressed by deleteInteraction.collectIsPressedAsState()
                val deleteIconColor = if (isDeletePressed) Color(0xFFF9423A) else Color.White
                Box(
                    modifier = Modifier
                        .width(buttonWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = deleteInteraction,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDelete()
                                coroutineScope.launch {
                                    animatableOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    onReveal(false)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗑️",
                        fontSize = 18.sp,
                        color = deleteIconColor
                    )
                }
            }
        }

        val dragState = rememberDraggableState { delta ->
            if (!isSelectionMode) {
                coroutineScope.launch {
                    val newOffset = (animatableOffset.value + delta).coerceIn(-maxRevealWidth, 0f)
                    animatableOffset.snapTo(newOffset)
                }
            }
        }

        LegacySongRow(
            song = song,
            downloadStatus = downloadStatus,
            isActive = isActive,
            onClick = {
                if (animatableOffset.value < -10f) {
                    coroutineScope.launch {
                        animatableOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                        onReveal(false)
                    }
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
                .offset { IntOffset(animatableOffset.value.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = !isSelectionMode,
                    onDragStopped = { velocity ->
                        val targetValue = if (animatableOffset.value < -maxRevealWidth / 2f) -maxRevealWidth else 0f
                        coroutineScope.launch {
                            animatableOffset.animateTo(
                                targetValue = targetValue,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            onReveal(targetValue == -maxRevealWidth)
                        }
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
            .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(0xFF1E1E20), RoundedCornerShape(10.dp))
                .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📚", fontSize = 22.sp)
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${playlist.songIds.size} ${if (playlist.songIds.size == 1) "song" else "songs"}",
                color = Color(0xFF9E9E9E),
                fontSize = 13.sp
            )
        }
    }
}
