package com.musyfy.nativeapp.domain.repository

import com.musyfy.nativeapp.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongs(): Flow<List<Song>>
    fun getSongById(id: String): Flow<Song?>
    suspend fun addSong(song: Song)
    suspend fun deleteSong(songId: String)
    suspend fun deleteSongs(songIds: List<String>)
}
