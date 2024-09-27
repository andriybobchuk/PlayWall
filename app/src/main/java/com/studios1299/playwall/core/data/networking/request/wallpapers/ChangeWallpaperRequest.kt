package com.studios1299.playwall.core.data.networking.request.wallpapers

data class ChangeWallpaperRequest(
    val fileName: String,
    val recipientId: String,
    val comment: String?,
    val reaction: String?
)