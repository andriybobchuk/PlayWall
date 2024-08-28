package com.studios1299.playwall.explore.presentation

data class ExploreState(
    val photos: List<Photo> = emptyList(),
    val isLoading: Boolean = false,
    val currentPhotoIndex: Int = 0,
    val isImageDetailVisible: Boolean = false
)

data class Photo(
    val id: String,
    val url: String,
    val description: String
)