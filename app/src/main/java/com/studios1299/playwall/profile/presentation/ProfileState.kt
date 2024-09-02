package com.studios1299.playwall.profile.presentation

import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState
import com.studios1299.playwall.explore.presentation.explore.Photo

data class ProfileState(
    val userName: String = "",
    val userEmail: String = "",
    val userAvatar: String = "",
    val isSaveWallpapersEnabled: Boolean = false,
    val selectedWallpaperOption: String = "Lock Screen", // Default option

    override val photos: List<Photo> = emptyList(),
    override val isLoading: Boolean = false,
) : ImageGridState()




