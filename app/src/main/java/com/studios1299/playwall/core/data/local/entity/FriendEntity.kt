package com.studios1299.playwall.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.presentation.play.FriendshipStatus

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: Int,
    val friendshipId: Int,
    val nick: String?,
    val email: String,
    val avatarId: String?,
    val status: FriendshipStatus,
    val requesterId: Int,
    val lastMessageDate: String?,
    val lastMessageStatus: MessageStatus?,
    val lastMessageSender: Int?,
    val screenRatio: Float,
    val orderIndex: Int
)