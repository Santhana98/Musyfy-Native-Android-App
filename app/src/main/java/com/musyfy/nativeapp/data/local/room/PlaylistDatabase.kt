package com.musyfy.nativeapp.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.musyfy.nativeapp.data.local.room.dao.PlaylistDao
import com.musyfy.nativeapp.data.local.room.entity.PlaylistEntity

@Database(entities = [PlaylistEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}
