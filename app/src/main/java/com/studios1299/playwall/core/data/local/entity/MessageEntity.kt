package com.studios1299.playwall.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Int,
    val fileName: String,
    val type: String,
    val requesterId: Int,
    val recipientId: Int,
    val comment: String?,
    val reaction: String?,
    val timeSent: String,
    val status: String?
)
