package com.studios1299.playwall.core.data.networking.response.wallpapers

import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.data.model.Reaction

data class ChangeWallpaperResponse(
    val fileName: String,
    val recipientId: Int,
    val comment: String?,
    val type: String?,
    val id: Int,
    val timestamp: String?,
    val status: MessageStatus?,
    val reaction: Reaction?,
    val senderId: Int?,
)