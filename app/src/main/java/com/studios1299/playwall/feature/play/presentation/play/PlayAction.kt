package com.studios1299.playwall.feature.play.presentation.play

sealed interface PlayAction {
    data class OnFriendClick(val friendId: String): PlayAction
    data class OnSelectFriend(val friendId: String): PlayAction
    data object OnFriendMute: PlayAction
    data object OnFriendUnMute: PlayAction
    data object OnFriendRemove: PlayAction
    data object OnEnterSelectMode : PlayAction
    data object OnExitSelectMode : PlayAction

    data object OnInviteClick: PlayAction

    data class OnAcceptFriendRequest(val requestId: String): PlayAction
    data class OnRejectFriendRequest(val requestId: String): PlayAction
    data object Refresh: PlayAction

    data class OnSearchUser(val userEmail: String): PlayAction

}