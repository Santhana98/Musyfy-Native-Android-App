package com.musyfy.nativeapp.core.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.musyfy.nativeapp.MainActivity
import com.musyfy.nativeapp.core.playback.PlayerManager
import com.musyfy.nativeapp.core.playback.PlayerManagerImpl
import com.musyfy.nativeapp.domain.repository.SongRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }



    @Provides
    @Singleton
    fun providePlayerManager(
        player: ExoPlayer,
        songRepository: SongRepository,
        @ApplicationContext context: Context
    ): PlayerManager {
        return PlayerManagerImpl(player, songRepository, context)
    }
}
