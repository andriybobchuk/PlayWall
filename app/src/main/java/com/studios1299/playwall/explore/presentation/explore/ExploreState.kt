package com.studios1299.playwall.explore.presentation.explore

import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState

data class ExploreState(
    override val photos: List<Photo> = emptyList(),
    override val isLoading: Boolean = false,
) : ImageGridState()

data class Photo(
    val id: String,
    val url: String,
    val description: String
)