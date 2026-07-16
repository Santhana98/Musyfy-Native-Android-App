package com.musyfy.nativeapp.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.keyframes
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

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
                            color = Color(0xFFF9423A), // Brand accent red color
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

@Composable
fun MusyfyBottomNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var shouldGlowHome by remember { mutableStateOf(false) }

    LaunchedEffect(activeTab) {
        if (activeTab == "home") {
            shouldGlowHome = true
            delay(1500)
            shouldGlowHome = false
        }
    }

    val tabs = listOf(
        BottomTab("home", "Home", "🏠"),
        BottomTab("search", "Search", "🔍"),
        BottomTab("liked", "Liked", "🤍"),
        BottomTab("upload", "Upload", "⬆"),
        BottomTab("settings", "Settings", "⚙️")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xEE08080A)) // Translucent premium dark background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0x1FFFFFFF)) // Ultra soft divider
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isActive = activeTab == tab.id
                val tabColor by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.primary else Color(0x7AFFFFFF),
                    animationSpec = tween(durationMillis = 200),
                    label = "tabColor"
                )
                val scaleFactor by animateFloatAsState(
                    targetValue = if (isActive) 1.08f else 1f,
                    animationSpec = tween(durationMillis = 200),
                    label = "tabScale"
                )
 
                val isHomeGlow = tab.id == "home" && shouldGlowHome
                val glowAlpha by animateFloatAsState(
                    targetValue = if (isHomeGlow) 0.5f else 0f,
                    animationSpec = if (isHomeGlow) {
                        keyframes {
                            durationMillis = 1500
                            0.0f at 0
                            0.5f at 300
                            0.5f at 800
                            0.0f at 1500
                        }
                    } else {
                        tween(200)
                    },
                    label = "homeGlowAlpha"
                )
 
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tab.id) }
                        .scale(scaleFactor),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(28.dp)
                    ) {
                        if (glowAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )
                        }
                        Text(
                            text = tab.icon,
                            fontSize = 18.sp,
                            color = tabColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 0.2.sp,
                        color = tabColor
                    )
                }
            }
        }
    }
}

data class BottomTab(val id: String, val label: String, val icon: String)
