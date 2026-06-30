package com.musyfy.nativeapp.core.di

import android.content.Context
import androidx.room.Room
import com.musyfy.nativeapp.data.local.room.PlaylistDatabase
import com.musyfy.nativeapp.data.local.room.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PlaylistDatabase {
        return Room.databaseBuilder(
            context,
            PlaylistDatabase::class.java,
            "musyfy_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(db: PlaylistDatabase): PlaylistDao {
        return db.playlistDao()
    }
}
