package com.musyfy.nativeapp.core.di

import com.musyfy.nativeapp.data.repository.UserRepositoryImpl
import com.musyfy.nativeapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindSongRepository(
        songRepositoryImpl: com.musyfy.nativeapp.data.repository.SongRepositoryImpl
    ): com.musyfy.nativeapp.domain.repository.SongRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: com.musyfy.nativeapp.data.repository.PlaylistRepositoryImpl
    ): com.musyfy.nativeapp.domain.repository.PlaylistRepository
}
