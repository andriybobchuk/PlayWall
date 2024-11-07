package com.studios1299.playwall.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studios1299.playwall.core.data.local.entity.ExploreWallpaperEntity

@Dao
interface ExploreWallpaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<ExploreWallpaperEntity>)

    @Query("SELECT * FROM explore_wallpapers ORDER BY `order` ASC")
    fun getAllWallpapersSortedByOrder(): List<ExploreWallpaperEntity>

    @Query("DELETE FROM explore_wallpapers")
    suspend fun deleteAllWallpapers()
}
