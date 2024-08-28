package com.studios1299.playwall.profile.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.domain.CoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            state = state.copy(
                userName = profile.name,
                userEmail = profile.email,
                userAvatar = profile.avatarUrl
            )
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.ToggleSaveWallpapers -> toggleSaveWallpapers()
            is ProfileAction.ChangeWallpaperScreen -> changeWallpaper(action.option)
            ProfileAction.OnContactClick -> contactSupport()
            ProfileAction.OnCpClick -> navigateTo(ProfileDestination.ContentPolicy)
            ProfileAction.OnFaqClick -> navigateTo(ProfileDestination.Faq)
            ProfileAction.OnHelpClick -> {} // Handled in the composable
            ProfileAction.OnInstagramClick -> navigateTo(ProfileDestination.Instagram)
            ProfileAction.OnPpClick -> navigateTo(ProfileDestination.PrivacyPolicy)
            ProfileAction.OnSocialClick -> {} // Handled in the composable
            ProfileAction.OnTikTikClick -> navigateTo(ProfileDestination.TikTok)
            ProfileAction.OnTosClick -> navigateTo(ProfileDestination.TermsOfService)
            ProfileAction.RollbackDefaultWallpaper -> rollbackWallpaper()
            ProfileAction.RollbackPreviousWallpaper -> rollbackWallpaper()
        }
    }

    private fun contactSupport() {
        viewModelScope.launch {
            // Logic to contact support
            eventChannel.send(ProfileEvent.SupportContacted)
        }
    }

    private fun rollbackWallpaper() {
        viewModelScope.launch {
            // Logic to rollback wallpaper
            eventChannel.send(ProfileEvent.WallpaperRolledBack)
        }
    }

    private fun changeWallpaper(option: WallpaperOption) {
        state = state.copy(selectedWallpaperOption = option.toString())
    }

    private fun toggleSaveWallpapers() {
        state = state.copy(isSaveWallpapersEnabled = !state.isSaveWallpapersEnabled)
        viewModelScope.launch {
            // Logic to save this preference in repository
            eventChannel.send(ProfileEvent.WallpapersSavedSettingToggled)
        }
    }

    private fun navigateTo(destination: ProfileDestination) {
        viewModelScope.launch {
            Log.e("fd", destination.toString())
           // eventChannel.send(ProfileEvent.NavigateTo(destination))
        }
    }
}
