package com.musyfy.nativeapp.feature.appearance.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.musyfy.nativeapp.feature.appearance.domain.AppearanceManager
import com.musyfy.nativeapp.feature.appearance.domain.model.AccentState
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val appearanceManager: AppearanceManager
) : ViewModel() {

    val state: StateFlow<AppearanceState> = appearanceManager.state
    val accentState: StateFlow<AccentState> = appearanceManager.accentState

    fun updateState(newState: AppearanceState) {
        appearanceManager.updateState(newState)
    }

    fun saveCustomWallpaper(bitmap: Bitmap) {
        appearanceManager.saveCustomWallpaper(bitmap)
    }

    fun removeCustomWallpaper() {
        appearanceManager.removeCustomWallpaper()
    }
}
