package com.studios1299.playwall.profile.presentation

import android.app.WallpaperManager
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.R
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.core.presentation.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
class ProfileViewModel(
    private val repository: CoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        loadUserProfile()
        onAction(ProfileAction.LoadPhotos)
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
            ProfileAction.LoadPhotos -> loadPhotos()
            is ProfileAction.OnPhotoClick -> {
                if (state.photos.isNotEmpty()) {
                    navigateToPhotoDetail(action.photoId)
                }
            }
            ProfileAction.OnSaveProfileClick -> saveProfile()
            ProfileAction.OnCancelEditProfileClick -> cancelEditProfile()
            ProfileAction.OnDeletePhotoClick -> deletePhoto()
            is ProfileAction.OnPhotoSelected -> updatePhoto(action.uri)
            is ProfileAction.OnNameChanged -> state = state.copy(userName = TextFieldState(action.name))
            is ProfileAction.OnEmailChanged -> state = state.copy(password = TextFieldState(action.email))
            ProfileAction.OnEditProfileClick -> openEditProfileDialog()
            ProfileAction.OnLogOut -> authRepository.logOut()
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            // Logic to save profile (e.g., repository.updateProfile(state))
            eventChannel.send(ProfileEvent.ProfileUpdated)
        }
    }

    private fun cancelEditProfile() {
        viewModelScope.launch {
            eventChannel.send(ProfileEvent.ProfileEditCancelled)
        }
    }

    private fun deletePhoto() {
        viewModelScope.launch {
            // Logic to delete photo (e.g., state = state.copy(userAvatar = ""))
            state = state.copy(userAvatar = "")
            eventChannel.send(ProfileEvent.ProfileUpdated)
        }
    }

    private fun updatePhoto(uri: Uri) {
        viewModelScope.launch {
            // Logic to update the profile photo
            state = state.copy(userAvatar = uri.toString())
            eventChannel.send(ProfileEvent.ProfileUpdated)
        }
    }

    private fun openEditProfileDialog() {
        state = state.copy(isEditProfileDialogOpen = true)
    }

    private fun closeEditProfileDialog() {
        state = state.copy(isEditProfileDialogOpen = false)
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            state = state.copy(
                userName = TextFieldState(profile.name),
                password = TextFieldState(""),
                userAvatar = profile.avatarUrl,
                selectedWallpaperOption = repository.getWallpaperDestination(),
                isSaveWallpapersEnabled = repository.shouldSaveIncomingWallpapers()
            )
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val photos = repository.getExploreItems()
            state = state.copy(photos = photos, isLoading = false)
        }
    }

    private fun navigateToPhotoDetail(photoId: String) {

        if (photoId.isNotEmpty()) {
            viewModelScope.launch {
                eventChannel.send(ProfileEvent.NavigateToPhotoDetail(photoId))
            }
        } else {
            viewModelScope.launch {
                eventChannel.send(ProfileEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
            }
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
            eventChannel.send(ProfileEvent.WallpaperRolledBack)
        }
    }

    private fun changeWallpaper(option: WallpaperOption) {
        repository.setWallpaperDestination(option)
        state = state.copy(selectedWallpaperOption = option)
    }

    private fun toggleSaveWallpapers() {
        if (repository.shouldSaveIncomingWallpapers()) {
            repository.setSaveIncomingWallpapers(false)
        } else {
            repository.setSaveIncomingWallpapers(true)
        }
        state = state.copy(isSaveWallpapersEnabled = !state.isSaveWallpapersEnabled)
        viewModelScope.launch {
            // Logic to save this preference in repository
            eventChannel.send(ProfileEvent.WallpapersSavedSettingToggled)
        }
    }

    private fun navigateTo(destination: ProfileDestination) {
        viewModelScope.launch {
            eventChannel.send(ProfileEvent.NavigateTo(destination))
        }
    }
}