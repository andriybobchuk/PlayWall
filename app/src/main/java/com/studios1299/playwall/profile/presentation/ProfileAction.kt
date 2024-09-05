package com.studios1299.playwall.profile.presentation

import android.net.Uri

sealed interface ProfileAction {
    data object OnHelpClick : ProfileAction
    data object OnContactClick : ProfileAction
    data object OnFaqClick : ProfileAction
    data object OnTosClick : ProfileAction
    data object OnPpClick : ProfileAction
    data object OnCpClick : ProfileAction

    data object OnSocialClick : ProfileAction
    data object OnInstagramClick : ProfileAction
    data object OnTikTikClick : ProfileAction

    data class ChangeWallpaperScreen(val option: WallpaperOption) : ProfileAction
    data object ToggleSaveWallpapers : ProfileAction
    data object RollbackPreviousWallpaper : ProfileAction
    data object RollbackDefaultWallpaper : ProfileAction

    data class OnPhotoClick(val photoId: String): ProfileAction
    data object LoadPhotos: ProfileAction
   // data class NavigateTo(val destination: ProfileDestination) : ProfileAction


    data object OnEditProfileClick : ProfileAction
    data object OnLogOut : ProfileAction
    data object OnSaveProfileClick : ProfileAction
    data object OnCancelEditProfileClick : ProfileAction
    data object OnDeletePhotoClick : ProfileAction
    data class OnPhotoSelected(val uri: Uri) : ProfileAction
    data class OnNameChanged(val name: String) : ProfileAction
    data class OnEmailChanged(val email: String) : ProfileAction
}



enum class WallpaperOption(private val displayName: String) {
    HomeScreen("Home Screen"),
    LockScreen("Lock Screen"),
    Both("Both");

    companion object {
        fun getEnumByDisplayName(name: String): WallpaperOption? {
            return entries.find { it.displayName == name }
        }
        fun getDisplayNames(): List<String> {
            return entries.map { it.displayName }
        }
    }
    override fun toString(): String {
        return displayName
    }
}

enum class ProfileDestination {
    TermsOfService,
    PrivacyPolicy,
    ContentPolicy,
    Faq,
    Instagram,
    TikTok,
}
