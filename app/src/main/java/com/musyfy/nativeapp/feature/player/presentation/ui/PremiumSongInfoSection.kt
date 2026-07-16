package com.musyfy.nativeapp.feature.player.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.domain.model.Song

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumSongInfoSection(
    song: Song?,
    onLikeToggle: () -> Unit,
    onMoreClick: () -> Unit,
    textColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val title = song?.title ?: "No Song Playing"
    val artist = song?.artist ?: "Unknown Artist"
    val isLiked = song?.liked ?: false

    val heartColor by animateColorAsState(
        targetValue = if (isLiked) accentColor else textColor.copy(alpha = 0.5f),
        label = "HeartColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = artist,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Heart (Like) button
            IconButton(
                onClick = onLikeToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = if (isLiked) "♥" else "♡",
                    color = heartColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // More Options button
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = "⋮",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
