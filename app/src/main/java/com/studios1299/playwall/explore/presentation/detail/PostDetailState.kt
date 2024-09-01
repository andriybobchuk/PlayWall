package com.studios1299.playwall.explore.presentation.detail

import com.studios1299.playwall.explore.presentation.explore.Photo

data class PostDetailState(
    val photos: List<Photo> = emptyList(),
    val currentPhotoIndex: Int = 0,
    val isLoading: Boolean = false
)
