package com.musyfy.nativeapp.feature.player.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.core.analytics.AnalyticsConstants
import com.musyfy.nativeapp.core.analytics.ImportAnalyticsTracker
import com.musyfy.nativeapp.core.analytics.LibraryAnalyticsTracker
import com.musyfy.nativeapp.core.playback.PlaybackUiState
import com.musyfy.nativeapp.core.playback.PlayerManager
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.PlaylistRepository
import com.musyfy.nativeapp.domain.repository.SongRepository
import com.musyfy.nativeapp.feature.download.data.SongDownloaderImpl
import com.musyfy.nativeapp.feature.download.data.YoutubeMetadataExtractor
import com.musyfy.nativeapp.feature.download.domain.SongDownloader
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import com.musyfy.nativeapp.core.protection.InFlightGuard
import com.musyfy.nativeapp.domain.usecase.DeleteSongUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import com.musyfy.nativeapp.core.validation.InputValidator
import com.musyfy.nativeapp.core.validation.ValidationResult
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val songDownloader: SongDownloader,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val importAnalyticsTracker: ImportAnalyticsTracker,
    private val libraryAnalyticsTracker: LibraryAnalyticsTracker,
    private val inFlightGuard: InFlightGuard,
    private val inputValidator: InputValidator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val playbackUiState: StateFlow<PlaybackUiState> = playerManager.playbackUiState

    val songs: StateFlow<List<Song>> = songRepository.getSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = songDownloader.downloadStatuses

    init {
        viewModelScope.launch {
            playerManager.playbackUiState.collect { state ->
                if (state.isPlaying && state.currentSong != null) {
                    importAnalyticsTracker.trackPlaybackReady(state.currentSong.id)
                }
            }
        }
    }

    fun playSong(song: Song, queue: List<Song>? = null) {
        android.util.Log.d("MusyfyPlayback", "PlayerViewModel: Song selected: ID=${song.id}, Title=${song.title}, audioPath=${song.audioPath}, artworkPath=${song.artworkPath}, remoteUrl=${song.url}")
        importAnalyticsTracker.trackPlaybackReady(song.id)
        playerManager.playSong(song, queue)
    }

    fun play() {
        playerManager.play()
    }

    fun dismissPlaybackSession() {
        playerManager.dismissPlaybackSession()
    }

    fun pause() {
        playerManager.pause()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun setQueue(songs: List<Song>) {
        playerManager.setQueue(songs)
    }

    fun addToQueue(song: Song) {
        playerManager.addToQueue(song)
    }

    fun playNext(song: Song) {
        playerManager.playNext(song)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        playerManager.reorderQueue(fromIndex, toIndex)
    }

    fun clearQueue() {
        playerManager.clearQueue()
    }

    fun removeFromQueue(songId: String) {
        playerManager.removeFromQueue(songId)
    }

    fun setShuffleModeEnabled(enabled: Boolean) {
        playerManager.setShuffleModeEnabled(enabled)
    }

    fun setRepeatMode(repeatMode: Int) {
        playerManager.setRepeatMode(repeatMode)
    }

    fun skipToNext() {
        viewModelScope.launch {
            try {
                val songsList = songs.value.ifEmpty { songRepository.getSongs().first() }
                val currentSong = playbackUiState.value.currentSong ?: return@launch
                val currentIndex = songsList.indexOfFirst { it.id == currentSong.id }
                if (currentIndex != -1 && currentIndex < songsList.size - 1) {
                    playSong(songsList[currentIndex + 1])
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            try {
                val songsList = songs.value.ifEmpty { songRepository.getSongs().first() }
                val currentSong = playbackUiState.value.currentSong ?: return@launch
                val currentIndex = songsList.indexOfFirst { it.id == currentSong.id }
                if (currentIndex != -1 && currentIndex > 0) {
                    playSong(songsList[currentIndex - 1])
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    // Downloader Interface controls
    // Architectural Note: PlayerViewModel owns the integration with ImportAnalyticsTracker.
    fun startDownload(song: Song) {
        // Track download started immediately upon initiating download session
        importAnalyticsTracker.trackDownloadStarted(song.id)

        viewModelScope.launch {
            val job = launch {
                songDownloader.downloadSong(song).collect { status ->
                    when (status) {
                        is DownloadStatus.Downloading -> {
                            // Log first_audio_cached when actual downloading progress arrives
                            importAnalyticsTracker.trackFirstAudioCached(song.id)
                        }
                        is DownloadStatus.Downloaded -> {
                            // Log download completed lifecycle event with actual file size on disk
                            val fileSize = java.io.File(status.localPath).length()
                            importAnalyticsTracker.trackDownloadCompleted(song.id, fileSize)

                            // Log import_completed only after the song has been successfully persisted to the local library
                            val persistedSong = songRepository.getSongs().first().find { it.id == song.id }
                            if (persistedSong != null && persistedSong.audioPath != null) {
                                importAnalyticsTracker.trackImportCompleted(song.id)
                            } else {
                                importAnalyticsTracker.trackImportFailedWithStage(
                                    keyOrSongId = song.id,
                                    failureReason = AnalyticsConstants.FailureReasons.STORAGE_ERROR,
                                    failureStage = AnalyticsConstants.FailureStages.STORAGE
                                )
                            }
                        }
                        is DownloadStatus.Error -> {
                            // Log download failed lifecycle event
                            val normalizedDownloadReason = normalizeDownloadErrorForDownload(status.message)
                            importAnalyticsTracker.trackDownloadFailed(song.id, normalizedDownloadReason)

                            // Log import failed lifecycle event with stage
                            val normalizedReason = normalizeDownloadError(status.message)
                            val failureStage = getFailureStageFromReason(normalizedReason)
                            importAnalyticsTracker.trackImportFailedWithStage(
                                keyOrSongId = song.id,
                                failureReason = normalizedReason,
                                failureStage = failureStage
                            )
                        }
                        else -> {}
                    }
                }
            }
            (songDownloader as? SongDownloaderImpl)?.registerJob(song.id, job)
            job.invokeOnCompletion {
                (songDownloader as? SongDownloaderImpl)?.clearJob(song.id)
            }
        }
    }

    private fun getFailureStageFromReason(reason: String): String {
        return when (reason) {
            AnalyticsConstants.FailureReasons.NETWORK_ERROR -> AnalyticsConstants.FailureStages.NETWORK
            AnalyticsConstants.FailureReasons.METADATA_ERROR -> AnalyticsConstants.FailureStages.METADATA
            AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR -> AnalyticsConstants.FailureStages.DOWNLOAD
            AnalyticsConstants.FailureReasons.STORAGE_ERROR -> AnalyticsConstants.FailureStages.STORAGE
            else -> AnalyticsConstants.FailureStages.UNKNOWN
        }
    }

    private fun normalizeDownloadError(message: String?): String {
        val msg = message?.lowercase() ?: return AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        return when {
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") || msg.contains("host") -> 
                AnalyticsConstants.FailureReasons.NETWORK_ERROR
            msg.contains("rename") || msg.contains("permission") || msg.contains("space") || msg.contains("storage") || msg.contains("save") || msg.contains("file") -> 
                AnalyticsConstants.FailureReasons.STORAGE_ERROR
            msg.contains("download") || msg.contains("exit code") || msg.contains("youtube-dl") || msg.contains("server returned code") -> 
                AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR
            else -> 
                AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        }
    }

    private fun normalizeDownloadErrorForDownload(message: String?): String {
        val msg = message?.lowercase() ?: return AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        return when {
            msg.contains("permission") || msg.contains("denied") -> 
                AnalyticsConstants.FailureReasons.PERMISSION_ERROR
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") || msg.contains("host") -> 
                AnalyticsConstants.FailureReasons.NETWORK_ERROR
            msg.contains("rename") || msg.contains("space") || msg.contains("storage") || msg.contains("save") || msg.contains("file") -> 
                AnalyticsConstants.FailureReasons.STORAGE_ERROR
            msg.contains("download") || msg.contains("exit code") || msg.contains("youtube-dl") || msg.contains("server returned code") -> 
                AnalyticsConstants.FailureReasons.DOWNLOAD_ERROR
            else -> 
                AnalyticsConstants.FailureReasons.UNKNOWN_ERROR
        }
    }

    fun pauseDownload(songId: String) {
        songDownloader.pauseDownload(songId)
    }

    fun cancelDownload(songId: String) {
        // Log import_cancelled and download_cancelled ONLY for explicit user-initiated cancellations.
        importAnalyticsTracker.trackDownloadCancelled(songId)
        importAnalyticsTracker.trackImportCancelled(songId)
        songDownloader.cancelDownload(songId)
    }

    fun deleteDownloadedSong(songId: String) {
        songDownloader.deleteDownloadedSong(songId)
    }

    fun deleteSong(songId: String, source: String? = null) {
        viewModelScope.launch {
            inFlightGuard.runIfKeyNotInFlight("delete_$songId") {
                deleteSongUseCase(songId, source)
            }
        }
    }

    fun deleteSong(song: Song, source: String? = null) {
        deleteSong(song.id, source)
    }

    fun toggleLikeSong(song: Song, source: String? = null) {
        viewModelScope.launch {
            inFlightGuard.runIfKeyNotInFlight("like_${song.id}") {
                val isNowLiked = !song.liked
                val updated = song.copy(liked = isNowLiked)
                songRepository.addSong(updated)
                val isCurrentlyPlaying = playerManager.playbackUiState.value.currentSong?.id == song.id
                val songDurationSeconds = if (song.durationMs > 0) song.durationMs / 1000L else null
                if (isNowLiked) {
                    libraryAnalyticsTracker.trackLikedSong(
                        songId = song.id,
                        songTitle = song.title,
                        artist = song.artist ?: "Unknown Artist",
                        source = source,
                        songDurationSeconds = songDurationSeconds,
                        isCurrentlyPlaying = isCurrentlyPlaying
                    )
                } else {
                    libraryAnalyticsTracker.trackRemovedLikedSong(
                        songId = song.id,
                        songTitle = song.title,
                        artist = song.artist ?: "Unknown Artist",
                        source = source,
                        songDurationSeconds = songDurationSeconds,
                        isCurrentlyPlaying = isCurrentlyPlaying
                    )
                }
            }
        }
    }

    fun restoreSong(song: Song) {
        viewModelScope.launch {
            songRepository.addSong(song)
        }
    }

    // YouTube Import Pipeline
    fun importYoutubeSong(url: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val validation = inputValidator.validateYoutubeUrl(url)
        if (validation is ValidationResult.Error) {
            onError(validation.message)
            return
        }
        val sanitizedUrl = (validation as ValidationResult.Success).sanitizedInput

        // Milestone 1: Track Add to Library Clicked
        val sessionId = importAnalyticsTracker.trackAddToLibraryClicked(sanitizedUrl)

        viewModelScope.launch(Dispatchers.IO) {
            val videoId = YoutubeMetadataExtractor.extractVideoId(sanitizedUrl)
            if (videoId == null) {
                importAnalyticsTracker.trackImportFailedWithStage(
                    keyOrSongId = sanitizedUrl,
                    failureReason = AnalyticsConstants.FailureReasons.INVALID_URL,
                    failureStage = AnalyticsConstants.FailureStages.METADATA
                )
                viewModelScope.launch(Dispatchers.Main) {
                    onError("Invalid YouTube URL — please check and try again")
                }
                return@launch
            }

            // Milestone 2: Metadata Extraction Started
            importAnalyticsTracker.trackMetadataExtractionStarted(sanitizedUrl)

            // Prevent duplicate entries in the library and reuse cached content
            val exists = songs.value.any { it.id == videoId }
            if (exists) {
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess()
                }
                return@launch
            }

            // Extract metadata from YouTube
            val info = YoutubeMetadataExtractor.fetchVideoInfo(context, sanitizedUrl)
            if (info == null) {
                importAnalyticsTracker.trackImportFailedWithStage(
                    keyOrSongId = sanitizedUrl,
                    failureReason = AnalyticsConstants.FailureReasons.METADATA_ERROR,
                    failureStage = AnalyticsConstants.FailureStages.METADATA
                )
                viewModelScope.launch(Dispatchers.Main) {
                    onError("Failed to extract metadata. Check connection and try again.")
                }
                return@launch
            }

            val sanitizedTitle = inputValidator.sanitize(info.title).ifEmpty { "Untitled Track" }
            val sanitizedArtist = inputValidator.sanitize(info.uploader).ifEmpty { "Unknown Artist" }

            // Milestone 3: Metadata Extraction Completed
            importAnalyticsTracker.trackMetadataExtractionCompleted(
                keyOrUrl = sanitizedUrl,
                songId = info.id,
                songTitle = sanitizedTitle,
                artist = sanitizedArtist
            )

            // Create song entry using extracted metadata
            val newSong = Song(
                id = info.id,
                title = sanitizedTitle,
                artist = sanitizedArtist,
                url = sanitizedUrl,
                imageUrl = info.thumbnail,
                durationMs = (info.duration ?: 0) * 1000L
            )
            
            songRepository.addSong(newSong)
            
            // Trigger background download (m4a stream + artwork thumbnail) automatically
            startDownload(newSong)

            viewModelScope.launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
