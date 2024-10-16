package com.studios1299.playwall.core.data.networking.response.wallpapers

data class ChangeWallpaperResponse(
    val fileName: String,
    val recipientId: String,
    val comment: String?,
    val reaction: String?,
    val type: String?
)