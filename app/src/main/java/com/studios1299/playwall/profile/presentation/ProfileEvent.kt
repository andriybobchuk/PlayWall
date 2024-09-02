package com.studios1299.playwall.profile.presentation

import com.studios1299.playwall.core.data.UserProfile
import com.studios1299.playwall.core.presentation.UiText

sealed interface ProfileEvent {
    data class ShowError(val error: UiText) : ProfileEvent
    data class NavigateTo(val destination: ProfileDestination) : ProfileEvent
    data object WallpaperRolledBack : ProfileEvent
    data object WallpapersSavedSettingToggled : ProfileEvent
    data object SupportContacted : ProfileEvent

    data class NavigateToPhotoDetail(val initialPhotoIndex: String): ProfileEvent

//    data object CloseEditProfileDialog : ProfileEvent
//    data object ProfileUpdated : ProfileEvent
    data object ProfileUpdated : ProfileEvent
    data object ProfileEditCancelled : ProfileEvent
}