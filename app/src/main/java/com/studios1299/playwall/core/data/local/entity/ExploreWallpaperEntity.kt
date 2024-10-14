package com.studios1299.playwall.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "explore_wallpapers")
data class ExploreWallpaperEntity(
    @PrimaryKey val id: Int,
    val fileName: String,
    val type: String,
    val sentCount: Int,
    val savedCount: Int,
    val isLiked: Boolean,
    val dateCreated: String,
    val order: Int,
)
