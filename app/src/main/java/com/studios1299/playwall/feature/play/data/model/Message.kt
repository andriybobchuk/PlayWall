package com.studios1299.playwall.feature.play.data.model

data class Reaction(
    val userName: String,
    val emoji: String
)

data class Message(
    val id: Int,
    val imageUrl: String,
    val caption: String,
    val timestamp: String,
    val status: MessageStatus,
    val reactions: List<String>,
    val senderId: String,
    val recipientId: String
)

enum class MessageStatus {
    SENT, DELIVERED, READ
}
