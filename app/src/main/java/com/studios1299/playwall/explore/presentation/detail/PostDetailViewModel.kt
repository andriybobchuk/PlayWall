package com.studios1299.playwall.explore.presentation.detail

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.studios1299.playwall.core.data.ChangeWallpaperWorker
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.explore.presentation.explore.ExploreStateSingleton
import com.studios1299.playwall.feature.play.presentation.play.Friend
import com.studios1299.playwall.feature.play.presentation.play.WallpaperNotificationForPlay
import com.studios1299.playwall.profile.presentation.ProfileState
import com.studios1299.playwall.profile.presentation.ProfileStateSingleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
class PostDetailViewModel(
    private val repository: CoreRepository,
    private val photoId: Int,
    val fromProfile: Boolean = false
) : ViewModel() {

    val exploreState: ExploreState
        get() = ExploreStateSingleton.state
    fun updateExploreState(newState: ExploreState) {
        ExploreStateSingleton.updateState(newState)
    }

    val profileState: ProfileState
        get() = ProfileStateSingleton.state
    fun updateProfileState(newState: ProfileState) {
        ProfileStateSingleton.updateState(newState)
    }

    private val eventChannel = Channel<PostDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        Log.e("PostDetailViewModel", "Init of PostDetailViewModel with photo.id = $photoId")
        if (fromProfile) {
            updateProfileState(profileState.copy(currentPhotoIndex = photoId))
        }
        loadFriends()
    }

    fun onAction(action: PostDetailAction) {
        when (action) {
            is PostDetailAction.OnSwipePhoto -> updateCurrentPhotoIndex(action.newIndex)
            PostDetailAction.ExitImageDetail -> exitImageDetail()
            is PostDetailAction.ToggleLike -> likeWallpaper(action.photoId)
        }
    }

    private fun loadFriends() {
        viewModelScope.launch {
            val result = repository.getFriends(true)
            if (result is SmartResult.Success) {
                updateExploreState(exploreState.copy(friends = result.data!!))
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun updateCurrentPhotoIndex(newIndex: Int) {
        Log.e("PostDetailViewModel", "updating current photo index to $newIndex")

        if (fromProfile) {
            if (newIndex in profileState.wallpapers.indices) {
                updateProfileState(profileState.copy(currentPhotoIndex = newIndex))
            }
        } else {
            if (newIndex in exploreState.wallpapers.indices) {
                updateExploreState(exploreState.copy(currentPhotoIndex = newIndex))
            }
        }
    }

    private fun exitImageDetail() {
        viewModelScope.launch {
            eventChannel.send(PostDetailEvent.ExitImageDetail)
        }
    }

    private fun likeWallpaper(wallpaperId: Int) {
        val updatedExploreWallpapers = exploreState.wallpapers.map { wallpaper ->
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
        if (fromProfile) {
            val updatedWallpapers = profileState.wallpapers.map { wallpaper ->
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
                                    Log.e(
                                        "likeWallpaper",
                                        "Error saving wallpaper: ${result.errorBody}"
                                    )
                                }
                            } else {
                                val result = repository.removeSavedWallpaper(wallpaperId)
                                if (result is SmartResult.Error) {
                                    Log.e(
                                        "likeWallpaper",
                                        "Error removing saved wallpaper: ${result.errorBody}"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    wallpaper
                }
            }
            updateProfileState(profileState.copy(wallpapers = updatedWallpapers))
        }
        updateExploreState(exploreState.copy(wallpapers = updatedExploreWallpapers))
    }

    fun setAsWallpaper(s3Link: String, context: Context) {

        val pathToS3 = S3Handler.downloadableLinkToPath(s3Link)
        val workData = workDataOf(
            "file_name" to pathToS3,
            "from_device" to false // its from explorer, not device
        )
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
            WallpaperNotificationForPlay.setNewWallpaperReceived(true)
        }
    }
}
