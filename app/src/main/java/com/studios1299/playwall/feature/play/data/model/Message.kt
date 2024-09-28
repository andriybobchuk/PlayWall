package com.studios1299.playwall.feature.play.data.model

data class Message(
    val id: Int,
    val imageUrl: String,
    val caption: String?,
    val timestamp: String,
    val status: MessageStatus,
    val reaction: Reaction?,
    val senderId: Int,
    val recipientId: Int
)

enum class MessageStatus {
    SENT, DELIVERED, READ
}

enum class Reaction(val emoji: String) {
    like("👍"),
    love("❤️"),
    haha("😂"),
    wow("😮"),
    sad("😢"),
    angry("😡");

    override fun toString(): String {
        return emoji
    }
}