package com.musyfy.nativeapp.feature.download.di

import com.musyfy.nativeapp.feature.download.data.SongDownloaderImpl
import com.musyfy.nativeapp.feature.download.domain.SongDownloader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloaderModule {

    @Binds
    @Singleton
    abstract fun bindSongDownloader(
        downloaderImpl: SongDownloaderImpl
    ): SongDownloader
}
