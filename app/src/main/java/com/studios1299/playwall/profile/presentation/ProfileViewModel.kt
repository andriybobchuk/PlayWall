package com.studios1299.playwall.profile.presentation

import android.app.ActivityManager
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
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.app.di.AppModule
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.local.database.AppDatabase
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.core.presentation.asUiText
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.explore.presentation.explore.ExploreStateSingleton
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
class ProfileViewModel(
    private val repository: CoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val state: ProfileState
        get() = ProfileStateSingleton.state

    fun updateProfileState(newState: ProfileState) {
        ProfileStateSingleton.updateState(newState)
    }

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                updateProfileState(state.copy(isOnline = online))
               // if (RefreshStateManager.isProfileScreenRefreshRequired) {
                    loadUserProfile(false)
                    loadSavedWallpapers(0, 100)
               // }
            }
        }
       // if (RefreshStateManager.isProfileScreenRefreshRequired) {
            loadUserProfile(false)
            loadSavedWallpapers(0, 100)
            //RefreshStateManager.isProfileScreenRefreshRequired = false
       // }
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
            is ProfileAction.OnPhotoClick -> {
                if (state.wallpapers.isNotEmpty()) {
                    navigateToPhotoDetail(action.photoId)
                }
            }
            is ProfileAction.OnSaveProfileClick -> updateProfile(action.context, action.avatar, action.nick, action.oldPassword, action.newPassword)
            ProfileAction.OnCancelEditProfileClick -> cancelEditProfile()
            ProfileAction.OnDeletePhotoClick -> deletePhoto()
            is ProfileAction.OnPhotoSelected -> updatePhoto(action.uri)
            is ProfileAction.OnNameChanged -> updateProfileState(state.copy(userName = TextFieldState(action.name)))
//            is ProfileAction.OnEmailChanged -> state = state.copy(password = TextFieldState(action.email))
//            is ProfileAction.OnEmailChanged -> state = state.copy(password = TextFieldState(action.email))
            ProfileAction.OnEditProfileClick -> openEditProfileDialog()
            ProfileAction.OnLogOut -> {
                logOut()
            }
        }
    }

    private fun loadUserProfile(forceUpdate: Boolean) {
        viewModelScope.launch {
            updateProfileState(state.copy(isLoading = true))
            when (val profileResult = repository.getUserData(forceUpdate)) {
                is SmartResult.Success -> {
                    updateProfileState(state.copy(
                        userName = TextFieldState(profileResult.data!!.name),
                        email = TextFieldState(profileResult.data.email),
                        userAvatar = profileResult.data.avatarId,
                        selectedWallpaperOption = repository.getWallpaperDestination(),
                        isSaveWallpapersEnabled = repository.shouldSaveIncomingWallpapers(),
                        isLoading = false
                    ))
                    Log.d("loadUserProfile()", "email=" + profileResult.data.email)
                    Log.d("loadUserProfile()", "avatarId=" + profileResult.data.avatarId)

                   // RefreshStateManager.isProfileScreenRefreshRequired = false
                }
                is SmartResult.Error -> {
                    if (state.isOnline) {
                        eventChannel.send(ProfileEvent.ShowError(UiText.DynamicString(profileResult.errorBody?:"")))
                    } else {
                        eventChannel.send(ProfileEvent.ShowError(DataError.Network.NO_INTERNET.asUiText()))
                    }
                }
            }
        }
    }



    fun updateProfile(context: Context, avatarUri: Uri?, nick: String?, oldPassword: String?, newPassword: String?) {
        viewModelScope.launch {
            var avatar: String? = null

            if (avatarUri != null) {
                // delete uri from db
                if (avatarUri == Uri.EMPTY) {
                    avatar = ""
                    updateProfileState(state.copy(userAvatar = avatarUri.toString()))
                } else {
                    val avatarFile = uriToFile(context, avatarUri)
                    if (avatarFile == null || !avatarFile.exists()) {
                        Log.e("updateProfile", "Failed to convert Uri to File or file does not exist.")
                        return@launch
                    }
                    val avatarId = repository.uploadFile(avatarFile, S3Handler.Folder.AVATARS)
                    if (avatarId is SmartResult.Success) {
                        avatar = avatarId.data
                        updateProfileState(state.copy(userAvatar = avatarUri.toString()))
                    }
                }
            }

            if (!oldPassword.isNullOrEmpty() && !newPassword.isNullOrEmpty()) {
                changePassword(oldPassword, newPassword)
            }

            val result = repository.updateProfile(avatar, nick?:"")

            if (result is SmartResult.Success) {
                Log.e("updateProfile", "Profile updated successfully")
                eventChannel.send(ProfileEvent.ProfileUpdated)
            } else if(result is SmartResult.Error) {
                Log.e("updateProfile", "Error updating profile: $result")
                if(result.code == 409) {
                    eventChannel.send(ProfileEvent.ShowError(UiText.DynamicString(
                        context.getString(
                            R.string.this_username_already_exists
                        ))))
                } else {
                    eventChannel.send(ProfileEvent.ShowError(UiText.DynamicString(result.message.toString())))
                }
                loadUserProfile(true)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            Log.e("A", "password: $currentPassword")
            val result = authRepository.updatePassword(currentPassword, newPassword)
            when (result) {
                is SmartResult.Success -> {
                    // Notify the UI about success
                    eventChannel.send(ProfileEvent.ShowError(UiText.DynamicString("Password changed!")))
                }
                is SmartResult.Error -> {
                    eventChannel.send(ProfileEvent.ShowError(DataError.Network.NO_INTERNET.asUiText()))
                }
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
            updateProfileState(state.copy(userAvatar = ""))
            eventChannel.send(ProfileEvent.ProfileUpdated)
        }
    }

    private fun updatePhoto(uri: Uri) {
        viewModelScope.launch {
            // Logic to update the profile photo
            updateProfileState(state.copy(userAvatar = uri.toString()))
            eventChannel.send(ProfileEvent.ProfileUpdated)
        }
    }

    private fun openEditProfileDialog() {
        updateProfileState(state.copy(isEditProfileDialogOpen = true))
    }

    fun loadSavedWallpapers(page: Int, pageSize: Int) {
        viewModelScope.launch {
            updateProfileState(state.copy(isLoading = true))
            val result = repository.loadSavedWallpapers(page, pageSize)

            if (result is SmartResult.Success) {
                val savedWallpapers = result.data!!.map {
                    ExploreWallpaper(
                        id = it.id,
                        fileName = it.fileName,
                        type = it.type,
                        sentCount = it.sentCount,
                        savedCount = it.savedCount,
                        isLiked = Preferences.isWallpaperLiked(it.id),
                        dateCreated = it.dateCreated
                    )
                }
                Log.e("loadSavedWallpapers", "saved wpps: " + savedWallpapers.forEach{it.id})
                updateProfileState(state.copy(wallpapers = savedWallpapers, isLoading = false))
            } else {
                Log.e("loadSavedWallpapers", "Shit saved wpps: " + result)
                //state = state.copy(isLoading = false)
                // Handle error, if needed
            }
        }
    }

    /**
     * @param photId is index?
     */
    private fun navigateToPhotoDetail(photoId: Int) {
        val photoIndex = state.wallpapers.indexOfFirst { it.id == photoId }
//        if (photoIndex in state.wallpapers.indices) {
//            updateExploreState(state.copy(currentPhotoIndex = photoIndex))
//        }
        Log.e("ProfileViewModel", "Photo clicked with id $photoId and index $photoIndex, currentPhotoIndex was updated")

        if (photoIndex != -1) {
            viewModelScope.launch {
                eventChannel.send(ProfileEvent.NavigateToPhotoDetail(photoIndex))
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
        updateProfileState(state.copy(selectedWallpaperOption = option))
    }

    private fun toggleSaveWallpapers() {
        if (repository.shouldSaveIncomingWallpapers()) {
            repository.setSaveIncomingWallpapers(false)
        } else {
            repository.setSaveIncomingWallpapers(true)
        }
        updateProfileState(state.copy(isSaveWallpapersEnabled = !state.isSaveWallpapersEnabled))
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

    fun clearAllAppData() {
        Log.e("ProfileViewModel", "clearAllAppData() clearing data started")
        val context = MyApp.appModule.context
        Preferences.clear()

        // Here we fucking go, memory leaks. But it's justified cause I need the db to be fully
        // cleared regardless of whether VM is active or not
        GlobalScope.launch {
            try {
                AppDatabase.clearAllTables()
                Log.d("ProfileViewModel", "All database tables cleared.")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to clear database tables: ${e.message}")
            }
        }
        // Delete other app storage files
        deleteRecursively(context.filesDir, "Files directory")
        deleteRecursively(context.cacheDir, "Cache directory")
        context.getExternalFilesDir(null)?.let {
            deleteRecursively(it, "External files directory")
        }
        Log.e("ProfileViewModel", "clearAllAppData() completed")
    }

    private fun deleteRecursively(file: File, description: String) {
        val deleted = file.deleteRecursively()
        if (deleted) {
            Log.e("ProfileViewModel", "$description cleared successfully.")
        } else {
            Log.e("ProfileViewModel", "Failed to clear $description.")
        }
    }

    private fun logOut() {
        try {
            clearAllAppData()
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "clearAllAppData() crashed: ${e.message}")
        }
        viewModelScope.launch {
            authRepository.deletePushToken()
        }
        authRepository.logOut()
    }

}
