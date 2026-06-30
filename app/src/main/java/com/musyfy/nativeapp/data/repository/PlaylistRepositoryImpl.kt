package com.musyfy.nativeapp.data.repository

import com.musyfy.nativeapp.data.local.room.dao.PlaylistDao
import com.musyfy.nativeapp.data.local.room.entity.PlaylistEntity
import com.musyfy.nativeapp.domain.model.Playlist
import com.musyfy.nativeapp.domain.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPlaylistById(id: String): Flow<Playlist?> {
        return playlistDao.getPlaylistById(id).map { it?.toDomain() }
    }

    override suspend fun createPlaylist(name: String): Playlist = withContext(Dispatchers.IO) {
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            songIds = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist.toEntity())
        playlist
    }

    override suspend fun renamePlaylist(id: String, newName: String) = withContext(Dispatchers.IO) {
        val playlistEntity = playlistDao.getPlaylistById(id).first() ?: return@withContext
        playlistDao.updatePlaylist(playlistEntity.copy(name = newName))
    }

    override suspend fun deletePlaylist(id: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistById(id)
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        val playlistEntity = playlistDao.getPlaylistById(playlistId).first() ?: return@withContext
        if (!playlistEntity.songIds.contains(songId)) {
            val updatedSongs = playlistEntity.songIds + songId
            playlistDao.updatePlaylist(playlistEntity.copy(songIds = updatedSongs))
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        val playlistEntity = playlistDao.getPlaylistById(playlistId).first() ?: return@withContext
        if (playlistEntity.songIds.contains(songId)) {
            val updatedSongs = playlistEntity.songIds.filter { it != songId }
            playlistDao.updatePlaylist(playlistEntity.copy(songIds = updatedSongs))
        }
    }

    override suspend fun reorderPlaylistSongs(playlistId: String, fromIndex: Int, toIndex: Int) = withContext(Dispatchers.IO) {
        val playlistEntity = playlistDao.getPlaylistById(playlistId).first() ?: return@withContext
        val updatedSongs = playlistEntity.songIds.toMutableList()
        if (fromIndex in updatedSongs.indices && toIndex in updatedSongs.indices) {
            val songId = updatedSongs.removeAt(fromIndex)
            updatedSongs.add(toIndex, songId)
            playlistDao.updatePlaylist(playlistEntity.copy(songIds = updatedSongs))
        }
    }

    // Mapper extensions
    private fun PlaylistEntity.toDomain(): Playlist {
        return Playlist(
            id = this.id,
            name = this.name,
            songIds = this.songIds,
            createdAt = this.createdAt
        )
    }

    private fun Playlist.toEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = this.id,
            name = this.name,
            songIds = this.songIds,
            createdAt = this.createdAt
        )
    }
}
