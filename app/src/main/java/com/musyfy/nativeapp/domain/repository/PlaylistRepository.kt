package com.musyfy.nativeapp.domain.repository

import com.musyfy.nativeapp.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(id: String): Flow<Playlist?>
    suspend fun createPlaylist(name: String): Playlist
    suspend fun renamePlaylist(id: String, newName: String)
    suspend fun deletePlaylist(id: String)
    suspend fun addSongToPlaylist(playlistId: String, songId: String)
    suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>): Int
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
    suspend fun removeSongFromAllPlaylists(songId: String)
    suspend fun reorderPlaylistSongs(playlistId: String, fromIndex: Int, toIndex: Int)
}
