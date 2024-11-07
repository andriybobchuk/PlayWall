package com.studios1299.playwall.profile.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper

data class ProfileState @OptIn(ExperimentalFoundationApi::class) constructor(
    val userName: TextFieldState = TextFieldState(),
    val oldPassword: TextFieldState = TextFieldState(),
    val newPassword: TextFieldState = TextFieldState(),
    val email: TextFieldState = TextFieldState(),
    override val currentPhotoIndex: Int = 0,
    val userAvatar: String = "",
    val isSaveWallpapersEnabled: Boolean = false,
    val selectedWallpaperOption: WallpaperOption = WallpaperOption.HomeScreen,

    override val wallpapers: List<ExploreWallpaper> = emptyList(),
    override val isLoading: Boolean = false,

    val isEditProfileDialogOpen: Boolean = false,
    val isOnline: Boolean = true

) : ImageGridState()

object ProfileStateSingleton {
    var state: ProfileState by mutableStateOf(ProfileState())

    fun updateState(newState: ProfileState) {
        state = newState
    }
}




