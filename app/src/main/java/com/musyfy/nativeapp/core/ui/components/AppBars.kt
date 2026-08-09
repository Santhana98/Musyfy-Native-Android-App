package com.musyfy.nativeapp.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R

@Composable
fun MusyfyTopBar(
    userName: String,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(top = 18.dp, start = 24.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: App Logo + App Name (centered & aligned)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Default,
                letterSpacing = 1.2.sp
            )
        }

        // Right: User Profile Pill (soft glassmorphism)
        Box {
            Row(
                modifier = Modifier
                    .background(Color(0x0CFFFFFF), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(24.dp))
                    .clickable { isMenuExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "👤", fontSize = 13.sp)
                Text(
                    text = "$userName ▾",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                modifier = Modifier
                    .background(Color(0xFA121214), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "👤   Profile",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                    }
                )
                HorizontalDivider(color = Color(0x1AFFFFFF))
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "🚪   Logout",
                            color = MaterialTheme.colorScheme.primary, // Dynamic theme accent color
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        onLogoutClick()
                    }
                )
            }
        }
    }
}

/**
 * Premium Floating Glassmorphism Bottom Navigation Bar for Musyfy Native App.
 * Active State Refinement:
 * - Dynamic Theme Accent (`MaterialTheme.colorScheme.primary`) visually fills the active icon vector shape internally.
 * - Zero radial glow, outer light, or luminous halos.
 * - Active label uses dynamic accent color; inactive items remain neutral and subtle.
 * - Retains 1.08x spring scale animation and tactile haptic feedback.
 */
@Composable
fun MusyfyBottomNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Single source of truth for the dynamic Musyfy accent color (Auto Palette, Musyfy Orange, Classic Red)
    val accentColor = MaterialTheme.colorScheme.primary

    val tabs = listOf(
        BottomTab("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomTab("search", "Search", Icons.Filled.Search, Icons.Outlined.Search),
        BottomTab("playlists", "Playlists", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
        BottomTab("upload", "Upload", Icons.Filled.FileUpload, Icons.Outlined.FileUpload),
        BottomTab("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating glassmorphism rounded container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF218181E),
                            Color(0xF2111115)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x35FFFFFF),
                            Color(0x10FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEach { tab ->
                    val isActive = activeTab == tab.id

                    val scaleFactor by animateFloatAsState(
                        targetValue = if (isActive) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "tabScale"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isActive) accentColor else Color(0x80FFFFFF),
                        animationSpec = tween(durationMillis = 200),
                        label = "iconColor"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isActive) accentColor else Color(0x75FFFFFF),
                        animationSpec = tween(durationMillis = 200),
                        label = "textColor"
                    )

                    val dotAlpha by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "dotAlpha"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isActive) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onTabSelected(tab.id)
                            }
                            .scale(scaleFactor),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(28.dp)
                        ) {
                            // Clean vector icon: filled shape when active (accent-colored), outlined when inactive
                            Icon(
                                imageVector = if (isActive) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.label,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.2.sp,
                            color = textColor
                        )

                        // Subtle accent-colored indicator dot below active label
                        if (dotAlpha > 0f) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = accentColor.copy(alpha = dotAlpha),
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

data class BottomTab(
    val id: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)
