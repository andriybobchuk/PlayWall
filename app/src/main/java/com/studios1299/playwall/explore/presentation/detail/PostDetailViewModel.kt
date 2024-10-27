package com.studios1299.playwall.explore.presentation.detail

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.ChangeWallpaperWorker
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.explore.presentation.explore.ExploreStateSingleton
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper
import com.studios1299.playwall.feature.play.presentation.play.Friend
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val repository: CoreRepository,
    private val photoId: Int,
    fromProfile: Boolean = false
) : ViewModel() {

    val state: ExploreState
    get() = ExploreStateSingleton.state

    // Function to update the singleton state
    fun updateExploreState(newState: ExploreState) {
        ExploreStateSingleton.updateState(newState)
    }

    private val eventChannel = Channel<PostDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        Log.e("PostDetailViewModel", "Init of PostDetailViewModel with photo.id = $photoId")
        if (fromProfile) {
            loadSavedWallpapers()
        } else {
            //loadAllWallpapers(true)
        }
//        viewModelScope.launch {
//            NetworkMonitor.isOnline.collect { online ->
//                updateExploreState(state.copy(isOnline = online))
//            }
//        }
        loadFriends()
    }

    fun onAction(action: PostDetailAction) {
        when (action) {
            is PostDetailAction.OnSwipePhoto -> updateCurrentPhotoIndex(action.newIndex)
            PostDetailAction.ExitImageDetail -> exitImageDetail()
            is PostDetailAction.ToggleLike -> likeWallpaper(action.photoId)
        }
    }

//    private fun loadAllWallpapers(forceRefresh: Boolean) {
//        viewModelScope.launch {
//            Log.e("PostDetailViewModel", "Loading photos...")
//            updateExploreState(state.copy(isLoading = true))
//
//            try {
//                val photos = repository.loadExploreWallpapers(0, 18, forceRefresh) // Fetches all photos
//
//                if (photos is SmartResult.Success) {
//                    updateExploreState(state.copy(wallpapers =
//                    photos.data!!.map {
//                        ExploreWallpaper(
//                            id = it.id,
//                            fileName = it.fileName,
//                            type = it.type,
//                            sentCount = it.sentCount,
//                            savedCount = it.savedCount,
//                            isLiked = Preferences.isWallpaperLiked(it.id),
//                            dateCreated = it.dateCreated,
//                        )
//                    }, isLoading = false))
//                }
//
//                Log.e("PostDetailViewModel", "Fetched ${state.wallpapers.size} photos")
//
//                val initialPhotoIndex = state.wallpapers.indexOfFirst { it.id == photoId }
//                Log.e("PostDetailViewModel", "Initial photo index for photoId $photoId: $initialPhotoIndex")
//
//                if (initialPhotoIndex != -1) {
//                    updateExploreState(state.copy(wallpapers = state.wallpapers, currentPhotoIndex = initialPhotoIndex, isLoading = false))
//                    Log.e("PostDetailViewModel", "Photos loaded successfully. Current photo index: $initialPhotoIndex")
//                } else {
//                    Log.e("PostDetailViewModel", "Photo not found for photoId: $photoId")
//                    eventChannel.send(PostDetailEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
//                }
//            } catch (e: Exception) {
//                Log.e("PostDetailViewModel", "Error loading photos: ${e.message}")
//                eventChannel.send(PostDetailEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
//            } finally {
//                updateExploreState(state.copy(isLoading = false))
//            }
//        }
//    }

    fun loadSavedWallpapers() {
        viewModelScope.launch {
            updateExploreState(state.copy(isLoading = true))
            val result = repository.loadSavedWallpapers(0, 18)

            if (result is SmartResult.Success) {
                updateExploreState(state.copy(wallpapers = result.data!!.map {
                    ExploreWallpaper(
                        id = it.id,
                        fileName = it.fileName,
                        type = it.type,
                        sentCount = it.sentCount,
                        savedCount = it.savedCount,
                        isLiked = Preferences.isWallpaperLiked(it.id),
                        dateCreated = it.dateCreated
                    )
                }, isLoading = false))
                val initialPhotoIndex = state.wallpapers.indexOfFirst { it.id == photoId }
                if (initialPhotoIndex != -1) {
                    updateExploreState(state.copy(wallpapers = state.wallpapers, currentPhotoIndex = initialPhotoIndex, isLoading = false))
                    Log.e("PostDetailViewModel", "Photos loaded successfully. Current photo index: $initialPhotoIndex")
                } else {
                    Log.e("PostDetailViewModel", "Photo not found for photoId: $photoId")
                    eventChannel.send(PostDetailEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
                }
            } else {
                Log.e("loadSavedWallpapers", "Shit saved wpps: " + result)
                updateExploreState(state.copy(isLoading = false))
                // Handle error, if needed
            }
        }
    }

    private fun loadFriends() {
        viewModelScope.launch {
            val result = repository.getFriends(true)
            if (result is SmartResult.Success) {
                updateExploreState(state.copy(friends = result.data!!))
            }
        }
    }

    private fun updateCurrentPhotoIndex(newIndex: Int) {
        Log.e("PostDetailViewModel", "updating current photo index to $newIndex")
        if (newIndex in state.wallpapers.indices) {
            updateExploreState(state.copy(currentPhotoIndex = newIndex))
        }
    }

    private fun exitImageDetail() {
        viewModelScope.launch {
            eventChannel.send(PostDetailEvent.ExitImageDetail)
        }
    }

    private fun likeWallpaper(wallpaperId: Int) {
        val updatedWallpapers = state.wallpapers.map { wallpaper ->
            if (wallpaper.id == wallpaperId) {
                val isLikedNow = !wallpaper.isLiked
                val countIncrement = if (isLikedNow) 1 else -1
                wallpaper.copy(
                    isLiked = isLikedNow,
                    savedCount = wallpaper.savedCount + countIncrement
                ).also {
                    Preferences.setWallpaperLiked(wallpaperId, isLikedNow)

                    viewModelScope.launch {
                        if (isLikedNow) {
                            val result = repository.saveWallpaper(wallpaperId)
                            if (result is SmartResult.Error) {
                                Log.e("likeWallpaper", "Error saving wallpaper: ${result.errorBody}")
                            }
                        } else {
                            val result = repository.removeSavedWallpaper(wallpaperId)
                            if (result is SmartResult.Error) {
                                Log.e("likeWallpaper", "Error removing saved wallpaper: ${result.errorBody}")
                            }
                        }
                    }
                }
            } else {
                wallpaper
            }
        }
        updateExploreState(state.copy(wallpapers = updatedWallpapers))
    }

    fun setAsWallpaper(s3Link: String, context: Context) {

        val pathToS3 = S3Handler.downloadableLinkToPath(s3Link)
        val workData = workDataOf(
            "file_name" to pathToS3,
            "from_device" to false // its from explorer, not device
        )
        Log.e("setAsWallpaper", "filename:  " + s3Link)


        val changeWallpaperWork = OneTimeWorkRequestBuilder<ChangeWallpaperWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(changeWallpaperWork)

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(changeWallpaperWork.id)
            .observeForever { workInfo ->
                if (workInfo.state.isFinished) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        Log.d("setAsWallpaper", "Wallpaper set successfully.")
                    } else {
                        Log.e("setAsWallpaper", "Failed to set wallpaper.")
                    }
                }
            }
    }

    fun sendWallpaperToFriends(friends: List<Friend>, fileName: String) {
        viewModelScope.launch {

            val pathTobeSent = S3Handler.downloadableLinkToPath(presignedUrl = fileName)
            if(pathTobeSent.isNullOrBlank()) {
                Log.e("sendWallpaperToFriends", "path to be sent is null")
                return@launch
            }

            friends.forEach { friend ->
                val result = repository.changeWallpaper(
                    ChangeWallpaperRequest(
                        fileName = pathTobeSent,
                        recipientId = friend.id.toString(),
                        comment = null,
                        reaction = null,
                        type = "friend_wallpaper"
                    )
                )
                if (result is SmartResult.Success) {
                    Log.e("sendWallpaperToFriends", "Wallpapers sent to ${friends.size} friends!!")

                } else {
                    Log.e("sendWallpaperToFriends", "filename:" + pathTobeSent)
                    Log.e("sendWallpaperToFriends", "couldnt send wallpapers" + result)
                }
            }
        }
    }



}
