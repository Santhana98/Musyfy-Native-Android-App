package com.musyfy.nativeapp.feature.appearance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.feature.appearance.domain.AppearanceManager
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val appearanceManager: AppearanceManager
) : ViewModel() {

    val state: StateFlow<AppearanceState> = appearanceManager.state

    fun updateState(newState: AppearanceState) {
        appearanceManager.updateState(newState)
    }
}
