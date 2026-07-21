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
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PremiumThemeBackground(
    themeState: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val bgSource = LocalAppearanceBackgroundSource.current 
        ?: if (themeState == "female") BackgroundSource.YTheme else BackgroundSource.XTheme

    val alignment = if (bgSource == BackgroundSource.YTheme) Alignment.TopCenter else Alignment.Center
    val customBitmapState = if (bgSource is BackgroundSource.Custom) {
        produceState<android.graphics.Bitmap?>(initialValue = null, bgSource.file, bgSource.version) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    android.graphics.BitmapFactory.decodeFile(bgSource.file.absolutePath)
                }.getOrNull()
            }
        }
    } else {
        null
    }

    val painter = when (bgSource) {
        BackgroundSource.XTheme -> painterResource(id = R.drawable.bg_male)
        BackgroundSource.YTheme -> painterResource(id = R.drawable.bg_female)
        is BackgroundSource.Custom -> {
            val bitmap = customBitmapState?.value
            if (bitmap != null) {
                BitmapPainter(bitmap.asImageBitmap())
            } else {
                // Fallback to solid color or empty painter while loading
                androidx.compose.ui.graphics.painter.ColorPainter(Color.Black)
            }
        }
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
                        0.0f to Color(0x80000000), // Softened 50% opacity black at the top
                        0.78f to Color(0x80000000), // Maintain themed background through playback controls
                        0.86f to Color(0xFF070708)  // Solid base color before bottom utility section
                    )
                )
        )
        content()
    }
}


