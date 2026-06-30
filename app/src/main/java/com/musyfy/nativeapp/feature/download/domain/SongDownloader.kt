package com.musyfy.nativeapp.feature.download.domain

import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SongDownloader {
    val downloadStatuses: StateFlow<Map<String, DownloadStatus>>
    fun getDownloadStatus(songId: String): Flow<DownloadStatus>
    fun downloadSong(song: Song): Flow<DownloadStatus>
    fun pauseDownload(songId: String)
    fun cancelDownload(songId: String)
    fun deleteDownloadedSong(songId: String): Boolean
}
