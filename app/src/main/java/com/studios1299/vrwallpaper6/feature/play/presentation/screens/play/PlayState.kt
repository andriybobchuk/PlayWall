package com.studios1299.vrwallpaper6.feature.play.presentation.screens.play

data class PlayState(
    val friends: List<Friend> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = false
)

data class Friend(
    val id: String,
    val name: String,
    val avatar: String,
    val lastMessage: String?,
    val unreadMessages: Int
)

data class FriendRequest(
    val id: String,
    val name: String,
    val avatar: String
)