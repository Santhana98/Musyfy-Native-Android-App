package com.musyfy.nativeapp.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import android.app.Activity
import androidx.core.view.WindowCompat

// Safe default CompositionLocals to enable future modular appearance system integration
val LocalAccentColor = androidx.compose.runtime.staticCompositionLocalOf { androidx.compose.ui.graphics.Color(0xFFF9423A) }

val LocalAppearanceBackground = androidx.compose.runtime.staticCompositionLocalOf<(@Composable androidx.compose.foundation.layout.BoxScope.(themeState: String, modifier: androidx.compose.ui.Modifier, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) -> Unit)?> { null }

val LocalAppearanceSettingsCard = androidx.compose.runtime.staticCompositionLocalOf<(@Composable () -> Unit)?> { null }

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun MusyfyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val accentColor = LocalAccentColor.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val base = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            base.copy(primary = accentColor)
        }
        darkTheme -> DarkColorScheme.copy(primary = accentColor)
        else -> LightColorScheme.copy(primary = accentColor)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false // White status bar icons
            insetsController.isAppearanceLightNavigationBars = false // White navigation bar icons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

