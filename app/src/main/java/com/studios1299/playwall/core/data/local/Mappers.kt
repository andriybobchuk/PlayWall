package com.studios1299.playwall.core.data.local

import android.util.Log
import com.studios1299.playwall.core.data.local.entity.FriendEntity
import com.studios1299.playwall.core.data.local.entity.MessageEntity
import com.studios1299.playwall.core.data.local.entity.UserEntity
import com.studios1299.playwall.core.data.networking.response.user.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.ChangeWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.WallpaperHistoryResponse
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.data.model.Reaction
import com.studios1299.playwall.feature.play.presentation.play.Friend
import com.studios1299.playwall.feature.play.presentation.play.FriendshipStatus

fun FriendEntity.toDomain(): Friend {
    return Friend(
        friendshipId = this.friendshipId,
        id = this.id,
        nick = this.nick,
        email = this.email,
        avatarId = this.avatarId,
        status = this.status,
        requesterId = this.requesterId,
        lastMessageDate = this.lastMessageDate,
        lastMessageStatus = this.lastMessageStatus,
        lastMessageSender = this.lastMessageSender,
        screenRatio = this.screenRatio
    )
}

fun Friend.toEntity(): FriendEntity {
    return FriendEntity(
        friendshipId = this.friendshipId,
        id = this.id,
        nick = this.nick,
        email = this.email,
        avatarId = this.avatarId,
        status = this.status,
        requesterId = this.requesterId,
        lastMessageDate = this.lastMessageDate,
        lastMessageStatus = this.lastMessageStatus,
        lastMessageSender = this.lastMessageSender,
        screenRatio = this.screenRatio,
        orderIndex = 0
    )
}

fun MessageEntity.toDomain(): WallpaperHistoryResponse {
    return WallpaperHistoryResponse(
        id = id,
        fileName = fileName,
        type = type,
        requesterId = requesterId,
        recipientId = recipientId,
        comment = comment,
        reaction = reaction?.let { Reaction.valueOf(it) },
        timeSent = timeSent,
        status = status?.let { MessageStatus.valueOf(it) }
    )
}

fun WallpaperHistoryResponse.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        fileName = fileName,
        type = type,
        requesterId = requesterId,
        recipientId = recipientId,
        comment = comment,
        reaction = reaction?.name,
        timeSent = timeSent,
        status = status?.name
    )
}

fun ChangeWallpaperResponse.toMessageEntity(): MessageEntity {
    return MessageEntity(
        id = this.id,
        fileName = this.fileName,
        type = this.type ?: "",
        requesterId = this.senderId ?: -1,
        recipientId = this.recipientId,
        comment = this.comment,
        reaction = this.reaction?.toString(),
        timeSent = this.timestamp ?: "",
        status = this.status?.toString()
    )
}

fun Message.toMessageEntity(): MessageEntity {
    return MessageEntity(
        id = this.id,
        fileName = this.imageUrl,
        type = "",
        requesterId = this.senderId,
        recipientId = this.recipientId,
        comment = this.caption,
        reaction = this.reaction?.toString(),
        timeSent = this.timestamp,
        status = this.status.toString()
    )
}

fun UserEntity.toUserDataResponse(): UserDataResponse {
    val status= if (this.status != null && this.status != "" && this.status != "null") {
        this.status
    } else {
        Log.e("Mappers", "Lol, you had invalid status, new status: accepted")
        "accepted"
    }
    return UserDataResponse(
        id = this.id,
        name = this.name,
        email = this.email,
        avatarId = this.avatarId,
        since = this.since,
        status = FriendshipStatus.valueOf(status),
        requesterId = this.requesterId,
        friendshipId = this.friendshipId,
        screenRatio = this.screenRatio
    )
}

fun UserDataResponse.toUserEntity(): UserEntity {
    Log.e("Mappers", "status(FriendshipStatus): ${this.status.toString()}")
    return UserEntity(
        id = this.id,
        name = this.name,
        email = this.email,
        avatarId = this.avatarId,
        since = this.since?:"",
        status = this.status.toString(),
        requesterId = this.requesterId,
        friendshipId = this.friendshipId,
        screenRatio = this.screenRatio
    )
}



