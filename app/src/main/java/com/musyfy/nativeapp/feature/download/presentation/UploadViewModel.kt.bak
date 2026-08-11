package com.musyfy.nativeapp.feature.download.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.feature.download.data.YoutubeMetadataExtractor
import com.musyfy.nativeapp.feature.download.data.YoutubeVideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface PreviewUiState {
    object Empty : PreviewUiState
    object Loading : PreviewUiState
    data class Success(val info: YoutubeVideoInfo) : PreviewUiState
    data class Error(val message: String) : PreviewUiState
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _previewState = MutableStateFlow<PreviewUiState>(PreviewUiState.Empty)
    val previewState: StateFlow<PreviewUiState> = _previewState.asStateFlow()

    private var activeJob: Job? = null
    private var lastFetchedUrl: String = ""

    fun fetchPreview(url: String, force: Boolean = false) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            clearPreview()
            return
        }

        val videoId = YoutubeMetadataExtractor.extractVideoId(trimmed)
        if (videoId == null) {
            _previewState.value = PreviewUiState.Error("Invalid YouTube URL format")
            return
        }

        if (!force && trimmed == lastFetchedUrl) return
        lastFetchedUrl = trimmed

        activeJob?.cancel()
        _previewState.value = PreviewUiState.Loading

        activeJob = viewModelScope.launch {
            val fullUrl = if (trimmed.startsWith("http")) trimmed else "https://www.youtube.com/watch?v=$videoId"
            val info = withContext(Dispatchers.IO) {
                YoutubeMetadataExtractor.fetchVideoInfo(context, fullUrl)
            }
            if (info != null) {
                _previewState.value = PreviewUiState.Success(info)
            } else {
                _previewState.value = PreviewUiState.Error("Unable to fetch song details. Check your connection.")
            }
        }
    }

    fun clearPreview() {
        activeJob?.cancel()
        lastFetchedUrl = ""
        _previewState.value = PreviewUiState.Empty
    }
}
