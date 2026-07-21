package com.musyfy.nativeapp.feature.appearance.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import com.musyfy.nativeapp.data.local.datastore.PreferencesManager
import com.musyfy.nativeapp.feature.appearance.data.AppearancePrefs
import com.musyfy.nativeapp.feature.appearance.domain.model.AccentState
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.musyfy.nativeapp.core.analytics.SettingsAnalyticsTracker
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppearanceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appearancePrefs: AppearancePrefs,
    private val preferencesManager: PreferencesManager,
    private val settingsAnalyticsTracker: SettingsAnalyticsTracker
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    val state: StateFlow<AppearanceState> = appearancePrefs.appearanceState
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppearanceState()
        )

    private val _accentState = MutableStateFlow(AccentState())
    val accentState: StateFlow<AccentState> = _accentState.asStateFlow()

    private val paletteCache = ConcurrentHashMap<String, Int>()

    private var lastModeForPalette: ThemeMode? = null
    private var lastPathForPalette: String? = null
    private var lastVersionForPalette: Long? = null

    init {
        scope.launch {
            preferencesManager.theme.collect { legacyTheme ->
                val currentMode = state.value.themeMode
                val targetMode = if (legacyTheme == "male") ThemeMode.X else ThemeMode.Y
                if (currentMode != ThemeMode.CUSTOM && currentMode != targetMode) {
                    appearancePrefs.saveAppearanceState(state.value.copy(themeMode = targetMode))
                }
            }
        }

        scope.launch {
            state.collect { appearanceState ->
                extractPaletteIfNeeded(appearanceState)
            }
        }

        scope.launch(Dispatchers.Default) {
            state.collect { appearanceState ->
                settingsAnalyticsTracker.trackThemeChanged(appearanceState.themeMode)
                settingsAnalyticsTracker.trackPlayerModeChanged(appearanceState.playerBackgroundMode)
                settingsAnalyticsTracker.trackWallpaperChanged(
                    appearanceState.customWallpaperPath,
                    appearanceState.wallpaperVersion
                )
                settingsAnalyticsTracker.trackAccentChanged(appearanceState.accentMode)
            }
        }
    }

    fun updateState(newState: AppearanceState) {
        scope.launch {
            appearancePrefs.saveAppearanceState(newState)
            if (newState.themeMode == ThemeMode.X) {
                preferencesManager.saveTheme("male")
            } else if (newState.themeMode == ThemeMode.Y) {
                preferencesManager.saveTheme("female")
            }
        }
    }

    fun saveCustomWallpaper(bitmap: Bitmap) {
        scope.launch(Dispatchers.IO) {
            val file = WallpaperProcessor.processAndSaveWallpaper(context, bitmap)
            if (file != null) {
                val newState = state.value.copy(
                    themeMode = ThemeMode.CUSTOM,
                    customWallpaperPath = file.absolutePath,
                    wallpaperVersion = System.currentTimeMillis()
                )
                appearancePrefs.saveAppearanceState(newState)
                preferencesManager.saveTheme("male")
            }
        }
    }

    fun removeCustomWallpaper() {
        scope.launch(Dispatchers.IO) {
            val path = state.value.customWallpaperPath
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            val newState = state.value.copy(
                themeMode = ThemeMode.X,
                customWallpaperPath = null,
                wallpaperVersion = System.currentTimeMillis()
            )
            appearancePrefs.saveAppearanceState(newState)
            preferencesManager.saveTheme("male")
        }
    }

    private fun extractPaletteIfNeeded(state: AppearanceState) {
        if (state.themeMode == lastModeForPalette &&
            state.customWallpaperPath == lastPathForPalette &&
            state.wallpaperVersion == lastVersionForPalette
        ) {
            return
        }
        lastModeForPalette = state.themeMode
        lastPathForPalette = state.customWallpaperPath
        lastVersionForPalette = state.wallpaperVersion

        val cacheKey = when (state.themeMode) {
            ThemeMode.X -> "XTheme"
            ThemeMode.Y -> "YTheme"
            ThemeMode.CUSTOM -> "custom_${state.customWallpaperPath.orEmpty()}_${state.wallpaperVersion}"
        }

        val cachedColor = paletteCache[cacheKey]
        if (cachedColor != null) {
            _accentState.value = AccentState(
                primaryAccent = cachedColor,
                isLoading = false,
                isFallback = (cachedColor == 0xFFF9423A.toInt()),
                sourceMode = state.themeMode,
                wallpaperVersion = state.wallpaperVersion
            )
            return
        }

        _accentState.value = _accentState.value.copy(
            isLoading = true,
            sourceMode = state.themeMode,
            wallpaperVersion = state.wallpaperVersion
        )

        scope.launch(Dispatchers.IO) {
            try {
                val bitmap = when (state.themeMode) {
                    ThemeMode.X -> {
                        val options = BitmapFactory.Options().apply { inSampleSize = 8 }
                        BitmapFactory.decodeResource(context.resources, com.musyfy.nativeapp.R.drawable.bg_male, options)
                    }
                    ThemeMode.Y -> {
                        val options = BitmapFactory.Options().apply { inSampleSize = 8 }
                        BitmapFactory.decodeResource(context.resources, com.musyfy.nativeapp.R.drawable.bg_female, options)
                    }
                    ThemeMode.CUSTOM -> {
                        state.customWallpaperPath?.let { path ->
                            val file = File(path)
                            if (file.exists()) {
                                val options = BitmapFactory.Options().apply { inSampleSize = 8 }
                                BitmapFactory.decodeFile(path, options)
                            } else null
                        }
                    }
                }

                val color = if (bitmap != null) {
                    val palette = Palette.from(bitmap).generate()
                    val accent = selectAccentFromPalette(palette)
                    bitmap.recycle()
                    accent
                } else {
                    0xFFF9423A.toInt()
                }

                paletteCache[cacheKey] = color
                _accentState.value = AccentState(
                    primaryAccent = color,
                    isLoading = false,
                    isFallback = (color == 0xFFF9423A.toInt()),
                    sourceMode = state.themeMode,
                    wallpaperVersion = state.wallpaperVersion
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _accentState.value = AccentState(
                    primaryAccent = 0xFFF9423A.toInt(),
                    isLoading = false,
                    isFallback = true,
                    sourceMode = state.themeMode,
                    wallpaperVersion = state.wallpaperVersion
                )
            }
        }
    }

    private fun selectAccentFromPalette(palette: Palette): Int {
        val swatches = listOfNotNull(
            palette.vibrantSwatch,
            palette.lightVibrantSwatch,
            palette.darkVibrantSwatch,
            palette.mutedSwatch,
            palette.lightMutedSwatch
        )
        for (swatch in swatches) {
            val hsl = swatch.hsl
            val saturation = hsl[1]
            val lightness = hsl[2]
            if (lightness in 0.35f..0.85f && saturation > 0.15f) {
                return swatch.rgb
            }
        }
        return 0xFFF9423A.toInt()
    }
}
