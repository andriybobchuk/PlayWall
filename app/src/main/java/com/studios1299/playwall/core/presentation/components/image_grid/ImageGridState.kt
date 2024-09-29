package com.studios1299.playwall.core.presentation.components.image_grid

import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper

abstract class ImageGridState {
    abstract val wallpapers: List<ExploreWallpaper>
    abstract val isLoading: Boolean
}