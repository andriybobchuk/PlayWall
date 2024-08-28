package com.studios1299.playwall.profile.presentation

data class ProfileState(
    val userName: String = "",
    val userEmail: String = "",
    val userAvatar: String = "",
    val isSaveWallpapersEnabled: Boolean = false,
    val selectedWallpaperOption: String = "Lock Screen", // Default option
)




