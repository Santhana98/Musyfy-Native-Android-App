package com.musyfy.nativeapp.feature.player.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PremiumPlayerControls(
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onPlayPauseClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    isLightMode: Boolean,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Shuffle Button
        val shuffleInteraction = remember { MutableInteractionSource() }
        val shufflePressed by shuffleInteraction.collectIsPressedAsState()
        val shuffleScale by animateFloatAsState(
            targetValue = if (shufflePressed) 0.85f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "ShuffleScale"
        )
        val shuffleColor = if (shuffleModeEnabled) accentColor else textColor.copy(alpha = 0.4f)

        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(shuffleScale)
                .clip(CircleShape)
                .clickable(
                    interactionSource = shuffleInteraction,
                    indication = null,
                    onClick = onShuffleToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                tint = shuffleColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // 2. Previous Button
        val prevInteraction = remember { MutableInteractionSource() }
        val prevPressed by prevInteraction.collectIsPressedAsState()
        val prevScale by animateFloatAsState(
            targetValue = if (prevPressed) 0.85f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "PrevScale"
        )

        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(prevScale)
                .clip(CircleShape)
                .clickable(
                    interactionSource = prevInteraction,
                    indication = null,
                    onClick = onPrevClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = textColor.copy(alpha = 0.85f),
                modifier = Modifier.size(30.dp)
            )
        }

        // 3. Play/Pause Button
        val playInteraction = remember { MutableInteractionSource() }
        val playPressed by playInteraction.collectIsPressedAsState()
        val playScale by animateFloatAsState(
            targetValue = if (playPressed) 0.9f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "PlayScale"
        )

        // Design:
        // Light Mode: White button with soft drop shadow, black icon
        // Dark Mode: Dark glassmorphic button with thin white border, white icon
        val playBgColor = if (isLightMode) Color.White else Color(0x2EFFFFFF)
        val playIconColor = if (isLightMode) Color.Black else Color.White
        val playBorderColor = if (isLightMode) Color.Transparent else Color(0x17FFFFFF)
        
        // Large outer glass ring effect (only in Dark Mode, matching reference)
        val outerRingModifier = if (!isLightMode) {
            Modifier
                .size(76.dp)
                .scale(playScale)
                .border(1.dp, Color(0x0AFFFFFF), CircleShape)
                .background(Color(0x05FFFFFF), CircleShape)
                .padding(4.dp)
        } else {
            Modifier
                .size(76.dp)
                .scale(playScale)
        }

        val playButtonModifier = if (isLightMode) {
            Modifier
                .size(68.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    clip = false
                )
                .clip(CircleShape)
                .background(playBgColor, CircleShape)
                .clickable(
                    interactionSource = playInteraction,
                    indication = null,
                    onClick = onPlayPauseClick
                )
        } else {
            Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(playBgColor)
                .border(1.dp, playBorderColor, CircleShape)
                .clickable(
                    interactionSource = playInteraction,
                    indication = null,
                    onClick = onPlayPauseClick
                )
        }

        Box(
            modifier = outerRingModifier,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = playButtonModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = playIconColor,
                    modifier = Modifier
                        .size(34.dp)
                        .padding(start = if (isPlaying) 0.dp else 3.dp)
                )
            }
        }

        // 4. Next Button
        val nextInteraction = remember { MutableInteractionSource() }
        val nextPressed by nextInteraction.collectIsPressedAsState()
        val nextScale by animateFloatAsState(
            targetValue = if (nextPressed) 0.85f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "NextScale"
        )

        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(nextScale)
                .clip(CircleShape)
                .clickable(
                    interactionSource = nextInteraction,
                    indication = null,
                    onClick = onNextClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = textColor.copy(alpha = 0.85f),
                modifier = Modifier.size(30.dp)
            )
        }

        // 5. Repeat Button
        val repeatInteraction = remember { MutableInteractionSource() }
        val repeatPressed by repeatInteraction.collectIsPressedAsState()
        val repeatScale by animateFloatAsState(
            targetValue = if (repeatPressed) 0.85f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "RepeatScale"
        )
        
        val repeatIcon = if (repeatMode == 1) Icons.Filled.RepeatOne else Icons.Filled.Repeat
        val repeatColor = if (repeatMode != 0) accentColor else textColor.copy(alpha = 0.4f)

        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(repeatScale)
                .clip(CircleShape)
                .clickable(
                    interactionSource = repeatInteraction,
                    indication = null,
                    onClick = onRepeatToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat",
                tint = repeatColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
