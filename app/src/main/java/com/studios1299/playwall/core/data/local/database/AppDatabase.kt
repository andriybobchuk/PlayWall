package com.studios1299.playwall.core.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.studios1299.playwall.core.data.local.dao.ExploreWallpaperDao
import com.studios1299.playwall.core.data.local.dao.FriendDao
import com.studios1299.playwall.core.data.local.entity.ExploreWallpaperEntity
import com.studios1299.playwall.core.data.local.entity.FriendEntity

@Database(entities = [ExploreWallpaperEntity::class, FriendEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exploreWallpaperDao(): ExploreWallpaperDao
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

