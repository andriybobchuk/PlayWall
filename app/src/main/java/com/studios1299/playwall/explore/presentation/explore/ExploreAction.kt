package com.studios1299.playwall.explore.presentation.explore


sealed interface ExploreAction {
    data class OnPhotoClick(val photoId: Int): ExploreAction
    data class ToggleLike(val photoId: Int) : ExploreAction
    data class LoadPhotos(val forceRefresh: Boolean): ExploreAction
}