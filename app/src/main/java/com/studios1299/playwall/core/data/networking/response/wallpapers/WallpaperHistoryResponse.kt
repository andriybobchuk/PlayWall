package com.studios1299.playwall.core.data.networking.response.wallpapers

import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.data.model.Reaction

data class WallpaperHistoryResponse(
    val id: Int,
    val fileName: String,
    val type: String,
    val requesterId: Int,
    val recipientId: Int,
    val comment: String?,
    val reaction: Reaction?,
    val timeSent: String,
    val status: MessageStatus?
)

data class WallpaperHistoryApiResponse(
    val data: List<WallpaperHistoryResponse>,
    val page: Int,
    val pageSize: Int,
    val endReached: Boolean
)


