package com.studios1299.playwall.feature.play.data.model

data class Reaction(
    val userName: String,
    val emoji: String
)

data class Message(
    val id: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: Long,
    val status: MessageStatus,
    val reactions: List<Reaction>,
    val senderId: String,
    val recipientId: String
)

enum class MessageStatus {
    SENT, DELIVERED, READ
}
