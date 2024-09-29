package com.studios1299.playwall.explore.presentation.detail

import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper
import com.studios1299.playwall.feature.play.presentation.play.Friend

data class PostDetailState(
    val wallpapers: List<ExploreWallpaper> = emptyList(),
    val currentPhotoIndex: Int = 0,
    val isLoading: Boolean = false,
    val friends: List<Friend> = emptyList()
)
