package com.musyfy.nativeapp.feature.appearance.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode

@Composable
fun LivePreviewMockup(
    state: AppearanceState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(220.dp)
            .height(440.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF070708))
            .border(4.dp, Color(0xFF1E1E20), RoundedCornerShape(32.dp))
    ) {
        // Layer 1: Theme Gradient Background Mock
        val wallpaperBrush = when (state.themeMode) {
            ThemeMode.X -> Brush.verticalGradient(
                colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
            )
            ThemeMode.Y -> Brush.verticalGradient(
                colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
            )
            ThemeMode.CUSTOM -> Brush.linearGradient(
                colors = listOf(Color(0xFF00c6ff), Color(0xFF0072ff), Color(0xFFf75990))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(wallpaperBrush)
                .then(
                    if (state.blurAmount > 0f) Modifier.blur(state.blurAmount.dp) else Modifier
                )
        )

        // Layer 2: Dark Overlay Mock
        if (state.darkOverlay > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = state.darkOverlay))
            )
        }

        // Layer 3: Mock UI Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Mock Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "12:30",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 6.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                    )
                }
            }

            // Mock Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good evening",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Premium Member",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(accentColor, CircleShape)
                )
            }

            // Mock Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Search songs, artists...",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mock Song Cards Row
            Text(
                text = "Recently Played",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1
                Column(modifier = Modifier.width(56.dp)) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.6f))
                    )
                }
                // Card 2
                Column(modifier = Modifier.width(56.dp)) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.6f))
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Mock Mini Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xF2121214))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                        Column {
                            Text(
                                text = "Diamonds",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rihanna",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 7.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mock Bottom Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("🏠", "🔍", "📚", "⚙️").forEachIndexed { index, icon ->
                    Text(
                        text = icon,
                        fontSize = 11.sp,
                        color = if (index == 3) accentColor else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
