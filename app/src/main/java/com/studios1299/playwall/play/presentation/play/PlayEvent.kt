package com.studios1299.playwall.play.presentation.play

import com.studios1299.playwall.core.presentation.UiText

sealed interface PlayEvent {
    data class ShowError(val error: UiText): PlayEvent
    data class NavigateToChat(val friendId: Int): PlayEvent
    data object NavigateToDiamonds: PlayEvent
    data object FriendRequestAccepted: PlayEvent
    data object FriendRequestRejected: PlayEvent
    data object InviteLinkParsedSuccessfully: PlayEvent
    data object WallpaperSent : PlayEvent
}