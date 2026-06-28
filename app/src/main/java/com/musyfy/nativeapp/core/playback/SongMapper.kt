package com.musyfy.nativeapp.core.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.musyfy.nativeapp.domain.model.Song

object SongMapper {
    fun toMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(song.imageUrl?.let { Uri.parse(it) })
                    .build()
            )
            .build()
    }

    fun toSong(mediaItem: MediaItem): Song {
        val metadata = mediaItem.mediaMetadata
        return Song(
            id = mediaItem.mediaId,
            title = metadata.title?.toString() ?: "",
            artist = metadata.artist?.toString() ?: "",
            url = mediaItem.localConfiguration?.uri?.toString() ?: "",
            imageUrl = metadata.artworkUri?.toString()
        )
    }
}
