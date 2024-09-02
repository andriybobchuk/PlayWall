package com.studios1299.playwall.core.presentation.components.image_grid

import com.studios1299.playwall.explore.presentation.explore.Photo

abstract class ImageGridState {
    abstract val photos: List<Photo>
    abstract val isLoading: Boolean
}