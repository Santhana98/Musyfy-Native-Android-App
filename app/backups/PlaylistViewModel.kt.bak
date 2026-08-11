package com.musyfy.nativeapp.feature.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musyfy.nativeapp.core.analytics.PlaylistAnalyticsTracker
import com.musyfy.nativeapp.domain.model.Playlist
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.PlaylistRepository
import com.musyfy.nativeapp.core.protection.OperationProtector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import com.musyfy.nativeapp.core.validation.InputValidator
import com.musyfy.nativeapp.core.validation.ValidationResult
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playlistAnalyticsTracker: PlaylistAnalyticsTracker,
    private val operationProtector: OperationProtector,
    private val inputValidator: InputValidator
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createPlaylist(name: String, source: String? = null, initialSongIds: List<String> = emptyList()) {
        val validation = inputValidator.validatePlaylistName(name)
        if (validation is ValidationResult.Error) {
            return
        }
        val sanitizedName = (validation as ValidationResult.Success).sanitizedInput

        viewModelScope.launch {
            operationProtector.withOperationLock("create_playlist") {
                val created = playlistRepository.createPlaylist(sanitizedName)
                if (initialSongIds.isNotEmpty()) {
                    initialSongIds.forEach { songId ->
                        if (inputValidator.validateId(songId)) {
                            playlistRepository.addSongToPlaylist(created.id, songId)
                        }
                    }
                }
                playlistAnalyticsTracker.trackPlaylistCreated(
                    playlistId = created.id,
                    playlistName = created.name,
                    initialSongCount = initialSongIds.size,
                    creationSource = source
                )
            }
        }
    }

    fun renamePlaylist(id: String, newName: String) {
        val validation = inputValidator.validatePlaylistName(newName)
        if (validation is ValidationResult.Error || !inputValidator.validateId(id)) {
            return
        }
        val sanitizedNewName = (validation as ValidationResult.Success).sanitizedInput

        viewModelScope.launch {
            operationProtector.withOperationLock("rename_$id") {
                val currentPlaylist = playlistRepository.getPlaylistById(id).first()
                val oldName = currentPlaylist?.name ?: ""
                playlistRepository.renamePlaylist(id, sanitizedNewName)
                if (currentPlaylist != null) {
                    playlistAnalyticsTracker.trackPlaylistRenamed(
                        playlistId = id,
                        oldName = oldName,
                        newName = sanitizedNewName
                    )
                }
            }
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            operationProtector.withOperationLock("delete_$id") {
                val currentPlaylist = playlistRepository.getPlaylistById(id).first()
                playlistRepository.deletePlaylist(id)
                if (currentPlaylist != null) {
                    playlistAnalyticsTracker.trackPlaylistDeleted(
                        playlistId = id,
                        playlistName = currentPlaylist.name,
                        songCountBeforeDelete = currentPlaylist.songIds.size
                    )
                }
            }
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        addSongToPlaylist(playlistId = playlistId, songId = songId, songTitle = "Unknown", artist = "Unknown", source = null)
    }

    fun addSongToPlaylist(playlistId: String, song: Song, source: String? = null) {
        addSongToPlaylist(
            playlistId = playlistId,
            songId = song.id,
            songTitle = song.title,
            artist = song.artist ?: "Unknown Artist",
            source = source
        )
    }

    fun addSongToPlaylist(
        playlistId: String,
        songId: String,
        songTitle: String,
        artist: String,
        source: String? = null
    ) {
        viewModelScope.launch {
            operationProtector.withOperationLock("add_${playlistId}_$songId") {
                val currentPlaylist = playlistRepository.getPlaylistById(playlistId).first()
                playlistRepository.addSongToPlaylist(playlistId, songId)
                if (currentPlaylist != null) {
                    val newCount = if (currentPlaylist.songIds.contains(songId)) {
                        currentPlaylist.songIds.size
                    } else {
                        currentPlaylist.songIds.size + 1
                    }
                    playlistAnalyticsTracker.trackPlaylistSongAdded(
                        playlistId = playlistId,
                        playlistName = currentPlaylist.name,
                        songId = songId,
                        songTitle = songTitle,
                        artist = artist,
                        currentPlaylistSongCount = newCount,
                        addSource = source
                    )
                }
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        removeSongFromPlaylist(playlistId = playlistId, songId = songId, songTitle = "Unknown", artist = "Unknown")
    }

    fun removeSongFromPlaylist(playlistId: String, song: Song) {
        removeSongFromPlaylist(
            playlistId = playlistId,
            songId = song.id,
            songTitle = song.title,
            artist = song.artist ?: "Unknown Artist"
        )
    }

    fun removeSongFromPlaylist(
        playlistId: String,
        songId: String,
        songTitle: String,
        artist: String
    ) {
        viewModelScope.launch {
            operationProtector.withOperationLock("remove_${playlistId}_$songId") {
                val currentPlaylist = playlistRepository.getPlaylistById(playlistId).first()
                playlistRepository.removeSongFromPlaylist(playlistId, songId)
                if (currentPlaylist != null) {
                    val newCount = (currentPlaylist.songIds.size - 1).coerceAtLeast(0)
                    playlistAnalyticsTracker.trackPlaylistSongRemoved(
                        playlistId = playlistId,
                        playlistName = currentPlaylist.name,
                        songId = songId,
                        songTitle = songTitle,
                        artist = artist,
                        currentPlaylistSongCount = newCount
                    )
                }
            }
        }
    }

    fun reorderPlaylistSongs(playlistId: String, fromIndex: Int, toIndex: Int) {
        if (!inputValidator.validateId(playlistId)) return
        viewModelScope.launch {
            operationProtector.withOperationLock("reorder_$playlistId") {
                val currentPlaylist = playlistRepository.getPlaylistById(playlistId).first()
                if (currentPlaylist != null) {
                    val size = currentPlaylist.songIds.size
                    if (inputValidator.isValidIndex(fromIndex, size) && inputValidator.isValidIndex(toIndex, size)) {
                        playlistRepository.reorderPlaylistSongs(playlistId, fromIndex, toIndex)
                        playlistAnalyticsTracker.trackPlaylistReordered(
                            playlistId = playlistId,
                            playlistName = currentPlaylist.name,
                            fromIndex = fromIndex,
                            toIndex = toIndex,
                            songCount = size
                        )
                    }
                }
            }
        }
    }

    fun trackPlaylistPlayStarted(
        playlist: Playlist,
        startingSong: Song,
        positionIndex: Int,
        playSource: String? = null,
        shuffleEnabled: Boolean? = null,
        repeatMode: String? = null
    ) {
        playlistAnalyticsTracker.trackPlaylistPlayStarted(
            playlistId = playlist.id,
            playlistName = playlist.name,
            songCount = playlist.songIds.size,
            songId = startingSong.id,
            songTitle = startingSong.title,
            artist = startingSong.artist ?: "Unknown Artist",
            positionIndex = positionIndex,
            playSource = playSource,
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode
        )
    }

    fun getPlaylistById(id: String) = playlistRepository.getPlaylistById(id)
}
