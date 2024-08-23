package com.studios1299.vrwallpaper6.feature.play.presentation.screens.play

sealed interface PlayAction {
    data class OnFriendClick(val friendId: String): PlayAction
    data object OnFriendMute: PlayAction
    data object OnFriendUnMute: PlayAction
    data object OnFriendRemove: PlayAction
    data object OnSelectFriend: PlayAction

    data object OnSelectClick: PlayAction
    data object OnInviteClick: PlayAction
    data object OnSetWallpaperClick: PlayAction

    data class OnAcceptFriendRequest(val requestId: String): PlayAction
    data class OnRejectFriendRequest(val requestId: String): PlayAction
    data object Refresh: PlayAction
}