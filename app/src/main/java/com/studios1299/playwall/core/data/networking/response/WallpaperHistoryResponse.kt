package com.studios1299.playwall.core.data.networking.response

import com.studios1299.playwall.feature.play.data.model.Reaction

data class WallpaperHistoryResponse(
    val id: Int,
    val fileName: String,
    val type: String,
    val requesterId: Int,
    val recipientId: Int,
    val comment: String?,
    val reaction: Reaction?,
    val timeSent: String,
)

