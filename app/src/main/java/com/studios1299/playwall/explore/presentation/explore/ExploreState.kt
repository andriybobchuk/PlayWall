package com.studios1299.playwall.explore.presentation.explore

import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState

data class ExploreState(
    override val wallpapers: List<ExploreWallpaper> = emptyList(),
    override val isLoading: Boolean = false,
) : ImageGridState()

data class ExploreWallpaper(
    val id: Int,
    val fileName: String,
    val type: String,
    val sentCount: Int,
    val savedCount: Int,
    val isLiked: Boolean,
    val dateCreated: String,
)
