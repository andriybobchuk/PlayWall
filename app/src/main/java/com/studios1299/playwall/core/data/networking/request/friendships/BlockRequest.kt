package com.studios1299.playwall.core.data.networking.request.friendships

data class BlockRequest(
    val friendshipId: Int,
    val userIdToBlock: Int // The user ID of the person to be blocked
)