package com.studios1299.playwall.explore.presentation.explore

import android.util.Log
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
            is ExploreAction.OnPhotoClick -> {
                if (state.photos.isNotEmpty()) {
                    navigateToPhotoDetail(action.photoId)
                }
            }
            ExploreAction.LoadPhotos -> loadPhotos()
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
                eventChannel.send(ExploreEvent.NavigateToPhotoDetail(photoId))
            }
        } else {
            viewModelScope.launch {
                eventChannel.send(ExploreEvent.ShowError(UiText.StringResource(R.string.error_photo_not_found)))
            }
        }
    }
}

