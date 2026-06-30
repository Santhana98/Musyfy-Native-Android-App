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

    private val defaultSongs = listOf(
        Song(
            id = "1",
            title = "Vibe Session",
            artist = "Musyfy Artist",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400",
            durationMs = 372000L,
            liked = true
        ),
        Song(
            id = "2",
            title = "Cassette Rewind",
            artist = "Cassette Player",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400",
            durationMs = 425000L,
            liked = false
        ),
        Song(
            id = "3",
            title = "Midnight Ride",
            artist = "Synthwave DJ",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
            durationMs = 344000L,
            liked = false
        ),
        Song(
            id = "4",
            title = "Lo-Fi Coffee",
            artist = "Beatmaker Chill",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            imageUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400",
            durationMs = 302000L,
            liked = true
        ),
        Song(
            id = "5",
            title = "Acoustic Sun",
            artist = "Folksy Singer",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            imageUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=400",
            durationMs = 363000L,
            liked = false
        )
    )

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
        _songs.value = defaultSongs + customSongs
    }

    private fun saveCustomSongs(songsList: List<Song>) {
        val customSongs = songsList.filterNot { defaultSongs.any { default -> default.id == it.id } }
        val json = gson.toJson(customSongs)
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
}
