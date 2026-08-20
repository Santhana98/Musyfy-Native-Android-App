package com.musyfy.nativeapp.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SongRepository {

    private val sharedPrefs = context.getSharedPreferences("musyfy_library", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())

    init {
        loadSongs()
    }

    private fun loadSongs() {
        val json = sharedPrefs.getString("custom_songs", null)
        val customSongs = if (json != null) {
            try {
                val type = object : TypeToken<List<Song>>() {}.type
                gson.fromJson<List<Song>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _songs.value = customSongs
    }

    private fun saveCustomSongs(songsList: List<Song>) {
        val json = gson.toJson(songsList)
        sharedPrefs.edit()
            .putString("custom_songs", json)
            .apply()
    }

    override fun getSongs(): Flow<List<Song>> = _songs

    override fun getSongById(id: String): Flow<Song?> {
        return _songs.map { list ->
            val song = list.find { it.id == id }
            android.util.Log.d("MusyfyPlayback", "SongRepository: getSongById($id) returned: $song")
            song
        }
    }

    override suspend fun addSong(song: Song) {
        _songs.update { current ->
            val index = current.indexOfFirst { it.id == song.id }
            val updated = if (index != -1) {
                current.toMutableList().apply { set(index, song) }
            } else {
                current + song
            }
            saveCustomSongs(updated)
            updated
        }
    }

    override suspend fun deleteSong(songId: String) {
        _songs.update { current ->
            val updated = current.filterNot { it.id == songId }
            saveCustomSongs(updated)
            updated
        }
    }

    override suspend fun deleteSongs(songIds: List<String>) {
        if (songIds.isEmpty()) return
        val songIdSet = songIds.toSet()
        _songs.update { current ->
            val updated = current.filterNot { it.id in songIdSet }
            saveCustomSongs(updated)
            updated
        }
    }
}
