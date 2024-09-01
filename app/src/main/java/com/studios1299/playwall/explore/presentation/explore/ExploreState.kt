package com.studios1299.playwall.explore.presentation.explore

data class ExploreState(
    val photos: List<Photo> = emptyList(),
    val isLoading: Boolean = false,
)

data class Photo(
    val id: String,
    val url: String,
    val description: String
)