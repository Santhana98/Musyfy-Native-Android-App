package com.musyfy.nativeapp.feature.download.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: PlayerViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0x4DF9423A), // Subtle brand halo border
        unfocusedBorderColor = Color(0x12FFFFFF), // Soft glass border
        focusedLabelColor = Color(0xFFF9423A),
        unfocusedLabelColor = Color(0x8CFFFFFF),
        cursorColor = Color(0xFFF9423A),
        focusedContainerColor = Color(0x1AFFFFFF), // Glass background
        unfocusedContainerColor = Color(0x0FFFFFFF),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xAA000000), // 66% black top
                        Color(0xFB070708), // 98% opacity
                        Color(0xFF070708)  // Solid base color
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag Bar indicator mimic
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Header Section
            Text(
                text = "🎵 Add YouTube Song",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold, // SemiBold typography
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Song appears in your library instantly — streams while saving in background",
                color = Color(0xFF9E9E9E), // Higher contrast grey
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (loading) {
                // Premium Red tinted loading block matching legacy
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0DF9423A), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x26F9423A), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = Color(0xFFF9423A),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Adding to your library...",
                        color = Color(0xFFF9423A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Song will appear instantly and start playing!",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                // Input Section
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; error = "" },
                    label = { Text("YouTube URL *") },
                    placeholder = { Text("https://youtube.com/watch?v=...") },
                    singleLine = true,
                    colors = colors,
                    shape = RoundedCornerShape(12.dp), // Consistent 12dp rounded corner
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    )
                )

                if (error.isNotEmpty()) {
                    // Alert layout matching legacy red background panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1AEF5350), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x4DEF5350), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Import trigger button
                val isUrlFilled = url.trim().isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isUrlFilled) {
                                    listOf(Color(0xFFF9423A), Color(0xFFD32F2F)) // brand red gradient
                                } else {
                                    listOf(Color(0xFF3A3A3C), Color(0xFF2C2C2E)) // premium dark grey gradient
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isUrlFilled) {
                            loading = true
                            error = ""
                            viewModel.importYoutubeSong(
                                url = url.trim(),
                                onSuccess = {
                                    loading = false
                                    onNavigateToHome()
                                },
                                onError = { errMsg ->
                                    loading = false
                                    error = errMsg
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add to Library →",
                        color = if (isUrlFilled) Color.White else Color(0xFF888888),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "✓ Streams from your IP  ·  ✓ Cached for offline  ·  ✓ Download anytime",
                color = Color(0xFF555558),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
