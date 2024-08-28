package com.studios1299.playwall.explore.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.R
import com.studios1299.playwall.core.domain.CoreRepository
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
            is ExploreAction.OnPhotoClick -> navigateToPhotoDetail(action.photoId)
            is ExploreAction.OnSwipePhoto -> updateCurrentPhotoIndex(action.newIndex)
            ExploreAction.LoadPhotos -> loadPhotos()
            ExploreAction.ExitImageDetail -> exitPhotoDetail()
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
        val initialPhotoIndex = state.photos.indexOfFirst { it.id == photoId }
        if (initialPhotoIndex != -1) {
            state = state.copy(currentPhotoIndex = initialPhotoIndex, isImageDetailVisible = true)
            viewModelScope.launch {
                eventChannel.send(ExploreEvent.NavigateToPhotoDetail(initialPhotoIndex))
            }
        } else {
            viewModelScope.launch {
                eventChannel.send(ExploreEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
            }
        }
    }

    private fun updateCurrentPhotoIndex(newIndex: Int) {
        if (newIndex >= 0 && newIndex < state.photos.size) {
            state = state.copy(currentPhotoIndex = newIndex)
        }
    }

    private fun exitPhotoDetail() {
        state = state.copy(isImageDetailVisible = false)
        viewModelScope.launch {
            eventChannel.send(ExploreEvent.ExitPhotoDetail)
        }
    }

    fun getLikeCount(photoId: String): Int {
        return repository.getLikeCount(photoId)
    }

    // Get the like status from shared preferences
    fun isLiked(photoId: String): Boolean {
        return sharedPrefs.getBoolean("like_$photoId", false)
    }

    // Toggle the like status and update shared preferences
    fun toggleLike(photoId: String) {
        val isLiked = isLiked(photoId)
        sharedPrefs.edit().putBoolean("like_$photoId", !isLiked).apply()
        // You may also want to notify the server or update the database
    }
}
