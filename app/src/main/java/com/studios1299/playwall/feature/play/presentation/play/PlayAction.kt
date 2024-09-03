package com.studios1299.playwall.feature.play.presentation.play

import android.net.Uri
import com.studios1299.playwall.explore.presentation.explore.ExploreAction

sealed interface PlayAction {
    data class OnFriendClick(val friendId: String): PlayAction
    data class OnSelectFriend(val friendId: String): PlayAction
    data class OnFriendMute(val friendId: String): PlayAction
    data class OnFriendUnMute(val friendId: String): PlayAction
    data class OnFriendRemove(val friendId: String): PlayAction
    data object OnEnterSelectMode : PlayAction
    data object OnExitSelectMode : PlayAction

    data object OnInviteClick: PlayAction

    data class OnAcceptFriendRequest(val requestId: String): PlayAction
    data class OnRejectFriendRequest(val requestId: String): PlayAction
    data object Refresh: PlayAction

    data class OnSearchUser(val userEmail: String): PlayAction

    data class OnSelectedFromGallery(val uri: Uri) : PlayAction
    data class OnSelectedFromSaved(val selectedWallpaper: String) : PlayAction
    data object LoadPhotos: PlayAction
}