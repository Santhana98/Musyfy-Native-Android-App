package com.musyfy.nativeapp.feature.appearance.domain

import android.content.Context
import android.graphics.Bitmap
import com.musyfy.nativeapp.data.local.datastore.PreferencesManager
import com.musyfy.nativeapp.feature.appearance.data.AppearancePrefs
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppearanceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appearancePrefs: AppearancePrefs,
    private val preferencesManager: PreferencesManager
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    val state: StateFlow<AppearanceState> = appearancePrefs.appearanceState
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppearanceState()
        )

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
                val file = java.io.File(path)
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
}
