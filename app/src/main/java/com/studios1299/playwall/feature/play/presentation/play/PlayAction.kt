package com.studios1299.playwall.feature.play.presentation.play

sealed interface PlayAction {
    data class OnFriendClick(val friendId: Int): PlayAction
    data class OnSelectFriend(val friendId: Int): PlayAction
    data class OnFriendMute(val friendshipId: Int, val userId: Int): PlayAction
    data class OnFriendUnMute(val friendshipId: Int, val userId: Int): PlayAction
    data class OnFriendRemove(val friendshipId: Int): PlayAction
    data object OnEnterSelectMode : PlayAction
    data object OnExitSelectMode : PlayAction
    data class UpdateSelectedFriends(val updatedSelectedFriends: List<Int>) : PlayAction

    data class OnAcceptFriendRequest(val requestId: Int): PlayAction
    data class OnRejectFriendRequest(val requestId: Int): PlayAction
    data object Refresh: PlayAction

    data class OnInviteFriend(val userEmail: String): PlayAction

    data class OnSelectedFromGallery(val filename: String) : PlayAction
    data class OnSelectedFromSaved(val selectedWallpaper: String, val selectedFriends: List<Int>) : PlayAction
    data object LoadPhotos: PlayAction
}