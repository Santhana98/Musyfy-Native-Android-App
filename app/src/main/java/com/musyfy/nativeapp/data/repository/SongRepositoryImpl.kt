package com.musyfy.nativeapp.data.repository

import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor() : SongRepository {

    private val mockSongs = listOf(
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

    override fun getSongs(): Flow<List<Song>> {
        return flowOf(mockSongs)
    }

    override fun getSongById(id: String): Flow<Song?> {
        return flowOf(mockSongs.firstOrNull { it.id == id })
    }
}
