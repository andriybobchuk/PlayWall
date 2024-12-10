package com.studios1299.playwall.core.data.networking.request.wallpapers

data class MarkMessagesAsReadRequest(
    val friendshipId: Int,
    val lastMessageId: Int
)