package com.musyfy.nativeapp.feature.appearance.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.musyfy.nativeapp.feature.appearance.di.AppearanceDataStore
import com.musyfy.nativeapp.feature.appearance.domain.model.AccentMode
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
import com.musyfy.nativeapp.feature.appearance.domain.model.ParallaxLevel
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode
import com.musyfy.nativeapp.feature.appearance.domain.model.WallpaperScale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppearancePrefs @Inject constructor(
    @AppearanceDataStore private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_MODE = stringPreferencesKey("accent_mode")
        val WALLPAPER_PATH = stringPreferencesKey("wallpaper_path")
        val WALLPAPER_VERSION = longPreferencesKey("wallpaper_version")
        val BLUR_AMOUNT = floatPreferencesKey("blur_amount")
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val DARK_OVERLAY = floatPreferencesKey("dark_overlay")
        val VISIBILITY = floatPreferencesKey("visibility")
        val SATURATION = floatPreferencesKey("saturation")
        val SCALE = stringPreferencesKey("scale")
        val POSITION_X = floatPreferencesKey("position_x")
        val POSITION_Y = floatPreferencesKey("position_y")
        val NOISE_TEXTURE = booleanPreferencesKey("noise_texture")
        val CORNER_FADE = booleanPreferencesKey("corner_fade")
        val PARALLAX = stringPreferencesKey("parallax")
    }

    val appearanceState: Flow<AppearanceState> = dataStore.data.map { preferences ->
        val customPath = preferences[Keys.WALLPAPER_PATH]
        val savedThemeMode = runCatching {
            ThemeMode.valueOf(preferences[Keys.THEME_MODE] ?: ThemeMode.X.name)
        }.getOrDefault(ThemeMode.X)

        // Failsafe: Revert to X Theme if Custom is active but file is missing
        val resolvedThemeMode = if (savedThemeMode == ThemeMode.CUSTOM && 
            (customPath == null || !java.io.File(customPath).exists())
        ) {
            ThemeMode.X
        } else {
            savedThemeMode
        }

        AppearanceState(
            themeMode = resolvedThemeMode,
            accentMode = runCatching {
                AccentMode.valueOf(preferences[Keys.ACCENT_MODE] ?: AccentMode.AUTO.name)
            }.getOrDefault(AccentMode.AUTO),
            customWallpaperPath = customPath,
            wallpaperVersion = preferences[Keys.WALLPAPER_VERSION] ?: 0L,
            blurAmount = preferences[Keys.BLUR_AMOUNT] ?: 0f,
            brightness = preferences[Keys.BRIGHTNESS] ?: 0f,
            darkOverlay = preferences[Keys.DARK_OVERLAY] ?: 0f,
            visibility = preferences[Keys.VISIBILITY] ?: 1.0f,
            saturation = preferences[Keys.SATURATION] ?: 1.0f,
            scale = runCatching {
                WallpaperScale.valueOf(preferences[Keys.SCALE] ?: WallpaperScale.FILL.name)
            }.getOrDefault(WallpaperScale.FILL),
            positionX = preferences[Keys.POSITION_X] ?: 0f,
            positionY = preferences[Keys.POSITION_Y] ?: 0f,
            noiseTexture = preferences[Keys.NOISE_TEXTURE] ?: false,
            cornerFade = preferences[Keys.CORNER_FADE] ?: false,

            parallax = runCatching {
                ParallaxLevel.valueOf(preferences[Keys.PARALLAX] ?: ParallaxLevel.MEDIUM.name)
            }.getOrDefault(ParallaxLevel.MEDIUM)
        )
    }

    suspend fun saveAppearanceState(state: AppearanceState) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = state.themeMode.name
            preferences[Keys.ACCENT_MODE] = state.accentMode.name
            if (state.customWallpaperPath != null) {
                preferences[Keys.WALLPAPER_PATH] = state.customWallpaperPath
            } else {
                preferences.remove(Keys.WALLPAPER_PATH)
            }
            preferences[Keys.WALLPAPER_VERSION] = state.wallpaperVersion
            preferences[Keys.BLUR_AMOUNT] = state.blurAmount
            preferences[Keys.BRIGHTNESS] = state.brightness
            preferences[Keys.DARK_OVERLAY] = state.darkOverlay
            preferences[Keys.VISIBILITY] = state.visibility
            preferences[Keys.SATURATION] = state.saturation
            preferences[Keys.SCALE] = state.scale.name
            preferences[Keys.POSITION_X] = state.positionX
            preferences[Keys.POSITION_Y] = state.positionY
            preferences[Keys.NOISE_TEXTURE] = state.noiseTexture
            preferences[Keys.CORNER_FADE] = state.cornerFade
            preferences[Keys.PARALLAX] = state.parallax.name
        }
    }
}

