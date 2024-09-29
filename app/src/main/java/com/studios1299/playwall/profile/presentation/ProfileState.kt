package com.studios1299.playwall.profile.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper

data class ProfileState @OptIn(ExperimentalFoundationApi::class) constructor(
    val userName: TextFieldState = TextFieldState(),
    val password: TextFieldState = TextFieldState(),
    val email: TextFieldState = TextFieldState(),
    val userAvatar: String = "",
    val isSaveWallpapersEnabled: Boolean = false,
    val selectedWallpaperOption: WallpaperOption = WallpaperOption.HomeScreen,

    override val wallpapers: List<ExploreWallpaper> = emptyList(),
    override val isLoading: Boolean = false,

    val isEditProfileDialogOpen: Boolean = false

) : ImageGridState()




