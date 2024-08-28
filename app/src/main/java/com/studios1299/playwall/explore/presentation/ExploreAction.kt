package com.studios1299.playwall.explore.presentation

sealed interface ExploreAction {
    data class OnPhotoClick(val photoId: String): ExploreAction
    data class OnSwipePhoto(val newIndex: Int): ExploreAction
    data object LoadPhotos: ExploreAction
    data object ExitImageDetail: ExploreAction
}