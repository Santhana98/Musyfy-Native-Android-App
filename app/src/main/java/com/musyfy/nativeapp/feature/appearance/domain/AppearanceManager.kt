package com.musyfy.nativeapp.feature.appearance.domain

import com.musyfy.nativeapp.feature.appearance.data.AppearancePrefs
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
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
    private val appearancePrefs: AppearancePrefs
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    val state: StateFlow<AppearanceState> = appearancePrefs.appearanceState
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppearanceState()
        )

    fun updateState(newState: AppearanceState) {
        scope.launch {
            appearancePrefs.saveAppearanceState(newState)
        }
    }
}
