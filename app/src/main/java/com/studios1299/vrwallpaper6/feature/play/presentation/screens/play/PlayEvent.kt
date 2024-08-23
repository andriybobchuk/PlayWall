package com.studios1299.vrwallpaper6.feature.play.presentation.screens.play

import com.studios1299.vrwallpaper6.core.presentation.UiText

sealed interface PlayEvent {
    data class ShowError(val error: UiText): PlayEvent
    data class NavigateToChat(val friendId: String): PlayEvent
    data object FriendRequestAccepted: PlayEvent
    data object FriendRequestRejected: PlayEvent
}