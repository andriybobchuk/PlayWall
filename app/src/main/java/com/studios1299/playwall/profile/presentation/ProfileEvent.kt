package com.studios1299.playwall.profile.presentation

import com.studios1299.playwall.core.presentation.UiText

sealed interface ProfileEvent {
    data class ShowError(val error: UiText) : ProfileEvent
    data class NavigateTo(val destination: ProfileDestination) : ProfileEvent
    data object WallpaperRolledBack : ProfileEvent
    data object WallpapersSavedSettingToggled : ProfileEvent
    data object SupportContacted : ProfileEvent
}