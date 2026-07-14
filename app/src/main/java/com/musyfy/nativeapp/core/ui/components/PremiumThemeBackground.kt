package com.musyfy.nativeapp.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.theme.BackgroundSource
import com.musyfy.nativeapp.core.ui.theme.LocalAppearanceBackgroundSource

@Composable
fun PremiumThemeBackground(
    themeState: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val bgSource = LocalAppearanceBackgroundSource.current 
        ?: if (themeState == "female") BackgroundSource.YTheme else BackgroundSource.XTheme

    val alignment = if (bgSource == BackgroundSource.YTheme) Alignment.TopCenter else Alignment.Center
    val painter = when (bgSource) {
        BackgroundSource.XTheme -> painterResource(id = R.drawable.bg_male)
        BackgroundSource.YTheme -> painterResource(id = R.drawable.bg_female)
        is BackgroundSource.Custom -> coil.compose.rememberAsyncImagePainter(
            model = coil.request.ImageRequest.Builder(LocalContext.current)
                .data(bgSource.file)
                .memoryCacheKey("custom_wallpaper_${bgSource.version}")
                .diskCacheKey("custom_wallpaper_${bgSource.version}")
                .build()
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Theme Background Image with legacy content scaling and alignment
        Image(
            painter = painter,
            contentDescription = "Theme Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = alignment
        )

        // Linear Gradient Overlay mimicking globals.css (darker for superior contrast and readability)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x80000000), // Softened 50% opacity black at the top
                            Color(0xE6070708), // Softened 90% opacity transition
                            Color(0xFF070708)  // Solid base color
                        )
                    )
                )
        )
        content()
    }
}


