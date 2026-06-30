package com.musyfy.nativeapp.core.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.musyfy.nativeapp.domain.model.Song

import android.content.Context
import java.io.File

object SongMapper {
    fun toMediaItem(song: Song, context: Context): MediaItem {
        val defaultLocalM4a = File(context.filesDir, "${song.id}.m4a")
        val defaultLocalMp3 = File(context.filesDir, "${song.id}.mp3")
        val resolvedAudioUriString = when {
            !song.audioPath.isNullOrEmpty() && File(song.audioPath).exists() && File(song.audioPath).length() > 0 -> {
                android.util.Log.d("MusyfyPlayback", "SongMapper: Local audioPath source selected for song ${song.id}: ${song.audioPath}")
                Uri.fromFile(File(song.audioPath)).toString()
            }
            defaultLocalM4a.exists() && defaultLocalM4a.length() > 0 -> {
                android.util.Log.d("MusyfyPlayback", "SongMapper: Local filesDir m4a source selected for song ${song.id}: ${defaultLocalM4a.absolutePath}")
                Uri.fromFile(defaultLocalM4a).toString()
            }
            defaultLocalMp3.exists() && defaultLocalMp3.length() > 0 -> {
                android.util.Log.d("MusyfyPlayback", "SongMapper: Local filesDir mp3 source selected for song ${song.id}: ${defaultLocalMp3.absolutePath}")
                Uri.fromFile(defaultLocalMp3).toString()
            }
            else -> {
                android.util.Log.d("MusyfyPlayback", "SongMapper: Remote URL source selected for song ${song.id}: ${song.url}")
                song.url
            }
        }
        android.util.Log.d("MusyfyPlayback", "SongMapper: Final URI passed to MediaItem for song ${song.id}: $resolvedAudioUriString")

        val defaultLocalArt = File(context.filesDir, "${song.id}.jpg")
        val resolvedArtworkUri = when {
            !song.artworkPath.isNullOrEmpty() && File(song.artworkPath).exists() && File(song.artworkPath).length() > 0 -> {
                Uri.fromFile(File(song.artworkPath))
            }
            defaultLocalArt.exists() && defaultLocalArt.length() > 0 -> {
                Uri.fromFile(defaultLocalArt)
            }
            else -> song.imageUrl?.let { Uri.parse(it) }
        }

        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(resolvedAudioUriString)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(resolvedArtworkUri)
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
