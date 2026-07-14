package com.musyfy.nativeapp.feature.settings.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val themeState by viewModel.theme.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF070708))
                .statusBarsPadding()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure your account details",
                    color = Color(0xFF666666),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Profile Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111112), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF1A1A1C), RoundedCornerShape(14.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFFFF7043))
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👤", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = authState.userName ?: "User",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = authState.userEmail ?: "No email available",
                                color = Color(0xFF666666),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Personalized Theme Settings Card
                val customSettingsCard = com.musyfy.nativeapp.core.ui.theme.LocalAppearanceSettingsCard.current
                if (customSettingsCard != null) {
                    customSettingsCard()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111112), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF1A1A1C), RoundedCornerShape(14.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Text(text = "🎨", fontSize = 16.sp)
                            Text(
                                text = "Personalized Theme Settings",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "Choose Theme Background — personalizes the interface with your preferred artwork.",
                            color = Color(0xFF666666),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Male Theme Toggle Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (themeState == "male") Color(0xFF1A1A1C) else Color.Transparent
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (themeState == "male") MaterialTheme.colorScheme.primary else Color(0xFF2A2A2E),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable { viewModel.saveTheme("male") }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🧑 X Theme",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
         
                            // Female Theme Toggle Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (themeState == "female") {
                                            Brush.linearGradient(
                                                colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFFFF7043))
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(Color.Transparent, Color.Transparent)
                                            )
                                        }
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (themeState == "female") MaterialTheme.colorScheme.primary else Color(0xFF2A2A2E),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable { viewModel.saveTheme("female") }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👩 Y Theme",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sign Out Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.logout(onLogout) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Out",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Overlay Hook for Appearance screen
        val appearanceOverlay = com.musyfy.nativeapp.core.ui.theme.LocalAppearanceOverlay.current
        if (appearanceOverlay != null) {
            appearanceOverlay()
        }
    }
}
