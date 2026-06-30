package com.musyfy.nativeapp.feature.download.domain.model

sealed class DownloadStatus {
    object NotDownloaded : DownloadStatus()
    data class Downloading(val progress: Float) : DownloadStatus()
    data class Downloaded(val localPath: String) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}
