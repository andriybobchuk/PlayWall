package com.studios1299.playwall.explore.presentation.explore

sealed interface ExploreAction {
    data class OnPhotoClick(val photoId: String): ExploreAction
    data object LoadPhotos: ExploreAction
}