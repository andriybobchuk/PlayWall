package com.studios1299.playwall.explore.presentation.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.R
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.explore.presentation.explore.Photo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val repository: CoreRepository,
    private val photoId: String
) : ViewModel() {

    var state by mutableStateOf(PostDetailState())
        private set

    private val eventChannel = Channel<PostDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        loadPhotos()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            Log.e("PostDetailViewModel", "Loading photos...")
            state = state.copy(isLoading = true)

            try {
                val photos = repository.getExploreItems() // Fetches all photos
                Log.e("PostDetailViewModel", "Fetched ${photos.size} photos")
                photos.forEach {
                    Log.e("PostDetailViewModel", "Photos ${it.id} ")
                }

                val initialPhotoIndex = photos.indexOfFirst { it.id == photoId }
                Log.e("PostDetailViewModel", "Initial photo index for photoId $photoId: $initialPhotoIndex")

                if (initialPhotoIndex != -1) {
                    state = state.copy(photos = photos, currentPhotoIndex = initialPhotoIndex, isLoading = false)
                    Log.e("PostDetailViewModel", "Photos loaded successfully. Current photo index: $initialPhotoIndex")
                } else {
                    Log.e("PostDetailViewModel", "Photo not found for photoId: $photoId")
                    eventChannel.send(PostDetailEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
                }
            } catch (e: Exception) {
                Log.e("PostDetailViewModel", "Error loading photos: ${e.message}")
                eventChannel.send(PostDetailEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun onAction(action: PostDetailAction) {
        when (action) {
            is PostDetailAction.OnSwipePhoto -> updateCurrentPhotoIndex(action.newIndex)
            PostDetailAction.ExitImageDetail -> exitImageDetail()
            is PostDetailAction.ToggleLike -> toggleLike(action.photoId)
        }
    }

    private fun updateCurrentPhotoIndex(newIndex: Int) {
        if (newIndex in state.photos.indices) {
            state = state.copy(currentPhotoIndex = newIndex)
        }
    }

    private fun exitImageDetail() {
        viewModelScope.launch {
            eventChannel.send(PostDetailEvent.ExitImageDetail)
        }
    }

    private fun toggleLike(photoId: String) {
        viewModelScope.launch {
          //  repository.toggleLike(photoId)
        }
    }

    fun getLikeCount(photoId: String): Int {
        return repository.getLikeCount(photoId)
    }

    fun isLiked(photoId: String): Boolean {
        //return repository.isLiked(photoId)
        return true
    }
}
