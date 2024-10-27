package com.studios1299.playwall.explore.presentation.explore

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.map
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.feature.play.data.DefaultPaginator
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.presentation.chat.viewmodel.PaginationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExploreViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    companion object {
        private const val LOG_TAG = "ChatViewModel"
        private const val EXPLORE_PAGE_SIZE = 11
    }

    val state: ExploreState
        get() = ExploreStateSingleton.state

    var paginationState by mutableStateOf(ExplorePaginationState())
    private val paginator = DefaultPaginator(
        initialKey = paginationState.page,
        onLoadUpdated = { isLoading ->
            paginationState = paginationState.copy(isLoading = isLoading)
        },
        onRequest = { nextPage ->
            Log.e("Paginator", "Requesting next page: $nextPage")
            repository.loadExploreWallpapers(
                page = nextPage,
                pageSize = EXPLORE_PAGE_SIZE,
                forceRefresh = true
            ).also {
                Log.e("Paginator", "Request result for page $nextPage: $it")
            }
        },
        getNextKey = { items ->
            Log.e("Paginator", "Current page: ${paginationState.page}, moving to next page.")
            paginationState.page + 1
        },
        onError = { error ->
            Log.e("Paginator", "Error during pagination: ${error?.localizedMessage}")
            paginationState = paginationState.copy(error = error?.localizedMessage)
        },
        onSuccess = { response, newKey ->
            Log.e(
                "Paginator",
                "Success - Loaded ${response.size} messages for page $newKey"
            )

            val wallpapers = response.map {
                ExploreWallpaper(
                    id = it.id,
                    fileName = it.fileName,
                    type = it.type,
                    sentCount = it.sentCount,
                    savedCount = it.savedCount,
                    isLiked = Preferences.isWallpaperLiked(it.id),
                    dateCreated = it.dateCreated,
                )
            }

            Log.e("Paginator", "Processed ${wallpapers.size} new wallpapers.")

            paginationState = paginationState.copy(
                page = newKey,
                endReached = wallpapers.isEmpty() || wallpapers.size < EXPLORE_PAGE_SIZE
            )

            val current = state.wallpapers
            Log.e("Paginator", "Current message count: ${current.size}")
            val new = wallpapers.filterNot { new ->
                current.any { it.id == new.id }
            }
            Log.e("Paginator", "Added ${new.size} new unique papers.")

            updateExploreState(state.copy(
                wallpapers = current + new
            ))
            Log.e("Paginator", "Final element count: ${state.wallpapers.size}")
        }
    )

    // Function to update the singleton state
    fun updateExploreState(newState: ExploreState) {
        ExploreStateSingleton.updateState(newState)
    }

    private val eventChannel = Channel<ExploreEvent>()
    val events = eventChannel.receiveAsFlow()

    private var isInitialLoad = true
    init {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                updateExploreState(state.copy(isOnline = online))
                if (online && !isInitialLoad) {
                    onAction(ExploreAction.LoadPhotos(forceRefresh = true))
                }
                isInitialLoad = false
            }
        }
        onAction(ExploreAction.LoadPhotos(forceRefresh = false))
    }

    fun onAction(action: ExploreAction) {
        when (action) {
            is ExploreAction.OnPhotoClick -> {
                if (state.wallpapers.isNotEmpty()) {
                    navigateToPhotoDetail(action.photoId)
                }
            }
            is ExploreAction.LoadPhotos -> loadPhotos()
            is ExploreAction.ToggleLike -> likeWallpaper(action.photoId)
        }
    }
    fun loadPhotos() {
        Log.e("ExploreViewModel", "Screen refresh initiated. Reloading page to: ${paginationState.page}")
        viewModelScope.launch {
            if (!paginationState.endReached && !paginationState.isLoading) {
                //paginationState = paginationState.copy(page = paginationState.page)
                paginator.loadNextItems()
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
                    savedCount = if (wallpaper.savedCount + countIncrement <= 0) 0 else wallpaper.savedCount + countIncrement
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

