package com.musyfy.nativeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MusyfyTopBar(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE6070708)) // rgba(7, 7, 8, 0.9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Musy-Fi",
                color = Color(0xFFE53935),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif
            )
        }
        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp) // Subtle border bottom
    }
}

@Composable
fun MusyfyBottomNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
            .background(Color(0xFA0A0505)) // rgba(10, 5, 5, 0.98)
    ) {
        HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp) // 1px solid #1a1a1a
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isActive = activeTab == tab.id
                val tabColor = if (isActive) Color(0xFFE53935) else Color(0xFF555555)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tab.id) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = tab.icon,
                        fontSize = 18.sp,
                        color = tabColor
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = tabColor
                    )
                }
            }
        }
    }
}

data class BottomTab(val id: String, val label: String, val icon: String)
