package com.studios1299.playwall.explore.presentation.detail

sealed interface PostDetailAction {
    data class OnSwipePhoto(val newIndex: Int) : PostDetailAction
    data object ExitImageDetail : PostDetailAction
    data class ToggleLike(val photoId: Int) : PostDetailAction
}
