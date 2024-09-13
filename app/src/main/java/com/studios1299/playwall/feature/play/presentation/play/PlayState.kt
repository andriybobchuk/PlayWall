package com.studios1299.playwall.feature.play.presentation.play

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.studios1299.playwall.explore.presentation.explore.Photo
import com.studios1299.playwall.feature.play.data.model.User

data class PlayState @OptIn(ExperimentalFoundationApi::class) constructor(
    val friends: List<Friend> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = false,
    val friendId: TextFieldState = TextFieldState(),
    val searchResults: List<User> = emptyList(),
    val isSelectMode: Boolean = false,
    val selectedFriends: List<String> = emptyList(),
    val photos: List<Photo> = emptyList(),
)

data class Friend(
    val id: String,
    val name: String,
    val avatar: String,
    val lastMessage: String?,
    val unreadMessages: Int,
    val muted: Boolean,
)

data class FriendRequest(
    val id: String,
    val name: String,
    val avatar: String
)
