package com.studios1299.playwall.play.presentation.play

import androidx.browser.customtabs.CustomTabsIntent.ShareState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.studios1299.playwall.core.data.networking.response.friendships.LinkRequestData
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper
import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.data.model.User

data class PlayState @OptIn(ExperimentalFoundationApi::class) constructor(
    val friends: List<Friend> = emptyList(),
    val friendRequests: List<Friend> = emptyList(),
    val isLoading: Boolean = false,
    val friendId: TextFieldState = TextFieldState(),
    val searchResults: List<User> = emptyList(),
    val isSelectMode: Boolean = false,
    val selectedFriends: List<Int> = emptyList(),
    val exploreWallpapers: List<ExploreWallpaper> = emptyList(),
    val linkInvite: LinkRequestData = LinkRequestData("", "", ""),
    //val inviteLink: String = "",
    val isOnline: Boolean = true
)

data class Friend(
    val friendshipId: Int,
    val id: Int,
    val nick: String?,
    val email: String,
    val avatarId: String?,
    //val lastMessage: String?,
    //val unreadMessages: Int,
    val status: FriendshipStatus,
    val requesterId: Int,
    val lastMessageDate: String?,      // Date of the last message
    val lastMessageStatus: MessageStatus?,    // Status of the last message (read/unread)
    val lastMessageSender: Int?,
    val screenRatio: Float
)

enum class FriendshipStatus {
    blocked,
    accepted,
    declined,
    pending
}

//data class FriendRequest(
//    val id: String,
//    val name: String,
//    val avatar: String
//)
