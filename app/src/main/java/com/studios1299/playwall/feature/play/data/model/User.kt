package com.studios1299.playwall.feature.play.data.model

import com.studios1299.playwall.feature.play.presentation.play.FriendshipStatus

data class User(
    val id: Int,
    val name: String,
    val profilePictureUrl: String,
    val lastOnline: Long = 0,
    val email: String = "random@email.com",
    val since: String,
    val status: FriendshipStatus,
    val requesterId: Int,
    val friendshipId: Int,
)