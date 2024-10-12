package com.studios1299.playwall.explore.presentation.explore

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.map
import com.studios1299.playwall.core.presentation.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    var state by mutableStateOf(ExploreState())
        private set

    private val eventChannel = Channel<ExploreEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        onAction(ExploreAction.LoadPhotos)
    }

    fun onAction(action: ExploreAction) {
        when (action) {
            is ExploreAction.OnPhotoClick -> {
                if (state.wallpapers.isNotEmpty()) {
                    navigateToPhotoDetail(action.photoId)
                }
            }
            ExploreAction.LoadPhotos -> loadPhotos()
            is ExploreAction.ToggleLike -> likeWallpaper(action.photoId)
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val photos = repository.loadExploreWallpapers(0, 18)
            if (photos is SmartResult.Success) {
                state = state.copy(wallpapers =
                photos.data.map {
                    ExploreWallpaper(
                        id = it.id,
                        fileName = it.fileName,
                        type = it.type,
                        sentCount = it.sentCount,
                        savedCount = it.savedCount,
                        isLiked = Preferences.isWallpaperLiked(it.id),
                        dateCreated = it.dateCreated,
                    )
                }, isLoading = false)
            }
        }
    }

    // TODO: Boilerplate from PostDetailViewModel
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
                                Log.e("likeWallpaper", "Error saving wallpaper: ${result.error}")
                            }
                        } else {
                            val result = repository.removeSavedWallpaper(wallpaperId)
                            if (result is SmartResult.Error) {
                                Log.e("likeWallpaper", "Error removing saved wallpaper: ${result.error}")
                            }
                        }
                    }
                }
            } else {
                wallpaper
            }
        }
        state = state.copy(wallpapers = updatedWallpapers)
    }

    private fun navigateToPhotoDetail(photoId: Int) {

        if (photoId != -1) {
            viewModelScope.launch {
                eventChannel.send(ExploreEvent.NavigateToPhotoDetail(photoId))
            }
        } else {
            viewModelScope.launch {
                eventChannel.send(ExploreEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
            }
        }
    }
}

