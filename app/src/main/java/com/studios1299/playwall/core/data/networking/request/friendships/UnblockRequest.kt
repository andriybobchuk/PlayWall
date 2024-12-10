package com.studios1299.playwall.core.data.networking.request.friendships

data class UnblockRequest(
    val friendshipId: Int,
    val userIdToUnblock: Int  // The user ID of the person to be unblocked
)