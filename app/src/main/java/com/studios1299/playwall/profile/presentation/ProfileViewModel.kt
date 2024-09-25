package com.studios1299.playwall.profile.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.core.presentation.asUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

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
            is ProfileAction.OnSaveProfileClick -> updateProfile(action.context, action.avatar, action.nick)
            ProfileAction.OnCancelEditProfileClick -> cancelEditProfile()
            ProfileAction.OnDeletePhotoClick -> deletePhoto()
            is ProfileAction.OnPhotoSelected -> updatePhoto(action.uri)
            is ProfileAction.OnNameChanged -> state = state.copy(userName = TextFieldState(action.name))
            is ProfileAction.OnEmailChanged -> state = state.copy(password = TextFieldState(action.email))
            ProfileAction.OnEditProfileClick -> openEditProfileDialog()
            ProfileAction.OnLogOut -> authRepository.logOut()
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            when (val profileResult = repository.getUserData()) {
                is SmartResult.Success -> {
                    state = state.copy(
                        userName = TextFieldState(profileResult.data.name),
                        email = TextFieldState(profileResult.data.email),
                        userAvatar = profileResult.data.avatarId,
                        selectedWallpaperOption = repository.getWallpaperDestination(),
                        isSaveWallpapersEnabled = repository.shouldSaveIncomingWallpapers(),
                        isLoading = false
                    )
                    Log.d("loadUserProfile()", "email=" + profileResult.data.email)
                    Log.d("loadUserProfile()", "avatarId=" + profileResult.data.avatarId)
                }
                is SmartResult.Error -> {
                    eventChannel.send(ProfileEvent.ShowError(profileResult.error.asUiText()))
                }
            }
        }
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        val fileName = "temp_avatar.jpg"
        val tempFile = File(context.cacheDir, fileName)
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            tempFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            Log.d("uriToFile", "File created at: ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            Log.e("uriToFile", "Error converting Uri to File: ${e.message}", e)
            null
        }
    }

    fun updateProfile(context: Context, avatarUri: Uri?, nick: String?) {
        viewModelScope.launch {
            var avatar: String? = null
            if (avatarUri != null) {
                val avatarFile = uriToFile(context, avatarUri)
                if (avatarFile == null || !avatarFile.exists()) {
                    Log.e("updateProfile", "Failed to convert Uri to File or file does not exist.")
                    return@launch
                }
                val avatarId = repository.uploadAvatar(avatarFile)
                if (avatarId is SmartResult.Success) {
                    avatar = avatarId.data
                    state = state.copy(userAvatar = avatarUri.toString())
                }
            }

            val result = repository.updateProfile(avatar, nick)
            //loadUserProfile()

            if (result is SmartResult.Success) {
                Log.e("updateProfile", "Profile updated successfully")
                eventChannel.send(ProfileEvent.ProfileUpdated)
            } else {
                Log.e("updateProfile", "Error updating profile: $result")
                eventChannel.send(ProfileEvent.ShowError(UiText.DynamicString(result.toString())))
            }
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
