package com.studios1299.playwall.core.data.networking.response

data class WallpaperHistoryResponse(
    val id: Int,
    val fileName: String,
    val requesterId: String,
    val recipientId: String,
    val comment: String?,
    val reaction: String?,
    val dateCreated: String,
    val dateUpdated: String
)
