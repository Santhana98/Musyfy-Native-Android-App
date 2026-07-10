package com.musyfy.nativeapp.feature.auth.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: (isLoggedIn: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val logoAlpha = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        // Logo fades in over 250ms
        logoAlpha.animateTo(1f, animationSpec = tween(durationMillis = 250))
        // App Name fades in over 200ms
        nameAlpha.animateTo(1f, animationSpec = tween(durationMillis = 200))
        // Tagline fades in over 200ms
        taglineAlpha.animateTo(1f, animationSpec = tween(durationMillis = 200))
        
        onSplashComplete(authState.isLoggedIn)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black), // Pure black background (#000000)
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Musyfy Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Musyfy",
                color = Color(0xFFE53935),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.alpha(nameAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🎧 Your Music. Your Vibe 🍁.",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
