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
    val wallpaperVersion: Long = 0L,
    val blurAmount: Float = 0f,         // 0f default for maximum sharpness
    val brightness: Float = 0f,         // -0.5f to 0.5f
    val darkOverlay: Float = 0f,        // 0f default to prevent grey/washed out look
    val visibility: Float = 1.0f,       // 0.0f to 1.0f
    val saturation: Float = 1.0f,       // 0.0f to 2.0f
    val scale: WallpaperScale = WallpaperScale.FILL,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val noiseTexture: Boolean = false,
    val cornerFade: Boolean = false,    // false default to preserve original X/Y identity
    val parallax: ParallaxLevel = ParallaxLevel.MEDIUM
)
