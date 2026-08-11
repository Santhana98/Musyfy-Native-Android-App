package com.musyfy.nativeapp.feature.download.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.feature.download.data.YoutubeImportCoordinator
import com.musyfy.nativeapp.feature.download.data.YoutubeImportState
import com.musyfy.nativeapp.feature.download.data.YoutubeVideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface PreviewUiState {
    object Empty : PreviewUiState
    object Loading : PreviewUiState
    data class Success(val info: YoutubeVideoInfo) : PreviewUiState
    data class Error(val message: String) : PreviewUiState
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    val importCoordinator: YoutubeImportCoordinator
) : ViewModel() {

    val importState: StateFlow<YoutubeImportState> = importCoordinator.importState
    val currentUrl: StateFlow<String> = importCoordinator.currentUrl

    val previewState: StateFlow<PreviewUiState> = importState.map { state ->
        when (state) {
            is YoutubeImportState.Idle -> PreviewUiState.Empty
            is YoutubeImportState.ExtractingMetadata -> PreviewUiState.Loading
            is YoutubeImportState.MetadataReady -> PreviewUiState.Success(state.info)
            is YoutubeImportState.Importing -> PreviewUiState.Success(state.info)
            is YoutubeImportState.Success -> PreviewUiState.Success(state.info)
            is YoutubeImportState.Error -> PreviewUiState.Error(state.message)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreviewUiState.Empty
    )

    fun onUrlChanged(url: String, force: Boolean = false) {
        importCoordinator.onUrlChanged(url, force = force)
    }

    fun fetchPreview(url: String, force: Boolean = false) {
        importCoordinator.onUrlChanged(url, force = force)
    }

    fun startImport(onSuccess: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        importCoordinator.startImport(onSuccess = onSuccess, onError = onError)
    }

    fun clearPreview() {
        importCoordinator.clear()
    }

    fun retry() {
        importCoordinator.retry()
    }
}
