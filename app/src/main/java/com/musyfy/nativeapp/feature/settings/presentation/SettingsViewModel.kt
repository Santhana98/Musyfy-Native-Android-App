package com.musyfy.nativeapp.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.feature.download.domain.DownloaderEngineManager
import com.musyfy.nativeapp.feature.download.domain.model.EngineUpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val downloaderEngineManager: DownloaderEngineManager
) : ViewModel() {

    val engineState: StateFlow<EngineUpdateState> = downloaderEngineManager.engineState

    fun getCurrentEngineVersion(): String = downloaderEngineManager.getCurrentEngineVersion()

    fun checkForUpdates() {
        val currentState = engineState.value
        if (currentState is EngineUpdateState.Checking || downloaderEngineManager.isUpdateInProgress()) {
            return
        }
        viewModelScope.launch {
            downloaderEngineManager.checkForUpdates(force = true)
        }
    }

    fun updateEngine() {
        if (downloaderEngineManager.isUpdateInProgress()) {
            return
        }
        viewModelScope.launch {
            downloaderEngineManager.updateEngine(triggerSource = "settings_manual")
        }
    }
}
