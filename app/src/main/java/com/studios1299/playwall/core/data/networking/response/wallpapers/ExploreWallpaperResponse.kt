package com.studios1299.playwall.core.data.networking.response.wallpapers

data class ExploreWallpaperResponse(
    val id: Int,
    val fileName: String,
    val type: String,
    val sentCount: Int,
    val savedCount: Int,
    val dateCreated: String,
    val dateUpdated: String
)
