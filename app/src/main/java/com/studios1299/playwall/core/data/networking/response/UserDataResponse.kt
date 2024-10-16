package com.studios1299.playwall.core.data.networking.response

import com.studios1299.playwall.feature.play.presentation.play.FriendshipStatus

data class UserDataResponse(
    val id: Int,
    val name: String,
    val email: String,
    val avatarId: String,
    val since: String? = "",
    val status: FriendshipStatus? = FriendshipStatus.accepted,
    val requesterId: Int = -1,
    val friendshipId: Int = -1,
    val screenRatio: Float = 2f
)