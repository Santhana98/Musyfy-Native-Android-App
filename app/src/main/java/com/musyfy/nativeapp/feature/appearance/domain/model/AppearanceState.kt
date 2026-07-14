package com.musyfy.nativeapp.feature.appearance.domain.model

enum class ThemeMode {
    X, Y, CUSTOM
}

enum class AccentMode {
    AUTO, MUSYFY_ORANGE
}

enum class WallpaperScale {
    FIT, FILL, ZOOM
}

enum class ParallaxLevel {
    OFF, LOW, MEDIUM, HIGH
}

data class AppearanceState(
    val themeMode: ThemeMode = ThemeMode.X,
    val accentMode: AccentMode = AccentMode.AUTO,
    val customWallpaperPath: String? = null,
    val blurAmount: Float = 8f,         // 0dp to 20dp
    val brightness: Float = 0f,         // -0.5f to 0.5f (-50% to +50%)
    val darkOverlay: Float = 0.4f,      // 0.0f to 0.8f (0% to 80%)
    val visibility: Float = 1.0f,       // 0.0f to 1.0f (0% to 100%)
    val saturation: Float = 1.0f,       // 0.0f to 2.0f (0% to 200%)
    val scale: WallpaperScale = WallpaperScale.FILL,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val noiseTexture: Boolean = false,
    val cornerFade: Boolean = true,
    val parallax: ParallaxLevel = ParallaxLevel.MEDIUM
)
