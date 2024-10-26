package com.studios1299.playwall.core.data.local

import com.studios1299.playwall.core.data.local.entity.FriendEntity
import com.studios1299.playwall.feature.play.presentation.play.Friend

// Mapping from FriendEntity to Domain
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

// Mapping from Domain to FriendEntity
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
