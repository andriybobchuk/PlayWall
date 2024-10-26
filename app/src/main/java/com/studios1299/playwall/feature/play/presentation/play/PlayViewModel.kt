package com.studios1299.playwall.feature.play.presentation.play

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.data.FriendEvent
import com.studios1299.playwall.core.data.WallpaperEventManager
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.core.presentation.asUiText
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
class PlayViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    companion object {
        private const val LOG_TAG = "PlayViewModel"
    }

    var state by mutableStateOf(PlayState())
        private set

    private val eventChannel = Channel<PlayEvent>()
    val events = eventChannel.receiveAsFlow()

    private var isInitialLoad = true
    init {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                state = state.copy(isOnline = online)
                if (online && !isInitialLoad) {
                    loadFriendsAndRequests(forceUpdate = true)
                }
                isInitialLoad = false
            }
        }
        viewModelScope.launch {
            WallpaperEventManager.friendUpdates.collect { friendEvent ->
                handleFriendEvent(friendEvent)
            }
        }
        loadFriendsAndRequests(forceUpdate = false)
    }

    private fun handleFriendEvent(friendEvent: FriendEvent) {
        Log.e(LOG_TAG, "FriendEvent received: $friendEvent")
        loadFriendsAndRequests(forceUpdate = true)
    }


    fun onAction(action: PlayAction) {
        when(action) {
            is PlayAction.OnAcceptFriendRequest -> acceptFriendRequest(action.requestId)
            is PlayAction.OnRejectFriendRequest -> declineFriendRequest(action.requestId)
            PlayAction.Refresh -> loadFriendsAndRequests()
            is PlayAction.OnInviteFriend -> inviteFriend(action.userEmail)
            is PlayAction.OnSelectFriend -> toggleFriendSelection(action.friendId)
            is PlayAction.OnEnterSelectMode -> enterSelectMode()
            is PlayAction.OnExitSelectMode -> exitSelectMode()
            is PlayAction.OnFriendClick -> {
                if (state.isSelectMode) {
                    toggleFriendSelection(action.friendId)
                } else {
                    navigateToChat(action.friendId)
                }
            }
            is PlayAction.OnSelectedFromGallery -> sendWallpaperToFriends(action.filename)
            is PlayAction.OnSelectedFromSaved -> {
                sendWallpaperToFriends(action.selectedWallpaper)
            }
            PlayAction.LoadPhotos -> loadSavedWallpapers()
            is PlayAction.OnFriendMute -> blockFriend(action.friendshipId, action.userId)
            is PlayAction.OnFriendRemove -> removeFriend(action.friendshipId)
            is PlayAction.OnFriendUnMute -> unblockFriend(action.friendshipId, action.friendshipId)
            is PlayAction.UpdateSelectedFriends -> state = state.copy(selectedFriends = action.updatedSelectedFriends)
        }
    }

    private fun enterSelectMode() {
        state = state.copy(isSelectMode = true)
    }

    private fun exitSelectMode() {
        state = state.copy(isSelectMode = false, selectedFriends = emptyList())
    }

    private fun toggleFriendSelection(friendId: Int) {
        val currentSelection = state.selectedFriends.toMutableList()
        if (currentSelection.contains(friendId)) {
            currentSelection.remove(friendId)
        } else {
            currentSelection.add(friendId)
        }
        state = state.copy(selectedFriends = currentSelection)
        Log.e("PlayViewModel", "Selected friends: " + state.selectedFriends.size)
    }

    private fun navigateToChat(friendId: Int) {
        viewModelScope.launch {
            eventChannel.send(PlayEvent.NavigateToChat(friendId))
        }
    }

    private fun inviteFriend(email: String) {
        viewModelScope.launch {

            state = state.copy(isLoading = true)
            val inviteFriend = repository.inviteFriend(email)
            if (inviteFriend is SmartResult.Success) {
                // say success
                eventChannel.send(PlayEvent.ShowError(UiText.DynamicString("Friend invited successfully!")))
            } else {
                // say error
                val errorMessage = when (inviteFriend) {
                    is SmartResult.Error -> {
                        inviteFriend.errorBody
                    }
                    else -> "An unexpected error occurred. Please try again."
                }
                eventChannel.send(PlayEvent.ShowError(UiText.DynamicString(errorMessage?:"Error")))
            }
            state = state.copy(
                isLoading = false
            )
        }
    }


@OptIn(ExperimentalFoundationApi::class)
fun loadFriendsAndRequests(forceUpdate: Boolean = false) {
    viewModelScope.launch {
        state = state.copy(isLoading = true)

        when (val friendsResult = repository.getFriends(forceUpdate)) {
            is SmartResult.Success -> {
                state = state.copy(
                    friends = friendsResult.data!!,
                    isLoading = false
                )
            }
            is SmartResult.Error -> {
                eventChannel.send(PlayEvent.ShowError(UiText.DynamicString(friendsResult.errorBody?:"")))
            }
        }
        when (val friendRequestsResult = repository.getFriendRequests(forceUpdate)) {
            is SmartResult.Success -> {
                state = state.copy(
                    friendRequests = friendRequestsResult.data!!,
                    isLoading = false
                )
            }
            is SmartResult.Error -> {
            }
        }
    }
}

    private fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            val result = repository.acceptFriendRequest(AcceptRequest(requestId))
            if (result is SmartResult.Success) {
                eventChannel.send(PlayEvent.FriendRequestAccepted)
                loadFriendsAndRequests(forceUpdate = true)
            } else {
                eventChannel.send(PlayEvent.ShowError(UiText.DynamicString("Error accepting request")))
            }
        }
    }

    private fun declineFriendRequest(requestId: Int) {
        viewModelScope.launch {
            val result = repository.declineFriendRequest(DeclineRequest(requestId.toInt()))
            if (result is SmartResult.Success) {
                eventChannel.send(PlayEvent.FriendRequestRejected)
                loadFriendsAndRequests(forceUpdate = true)
            } else {
                eventChannel.send(PlayEvent.ShowError(UiText.DynamicString("Error declining request")))
            }
        }
    }

    fun blockFriend(friendshipId: Int, userId: Int) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.blockUser(friendshipId, userId)
            if (result is SmartResult.Success) {
                loadFriendsAndRequests(forceUpdate = true)
            } else {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun unblockFriend(friendshipId: Int, userId: Int) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.unblockUser(friendshipId, userId)
            if (result is SmartResult.Success) {
                loadFriendsAndRequests(forceUpdate = true)
            } else {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun removeFriend(friendshipId: Int) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.removeUser(friendshipId)
            if (result is SmartResult.Success) {
                loadFriendsAndRequests(forceUpdate = true)
            } else {
                state = state.copy(isLoading = false)
            }
        }
    }

    // TODO: Almost Boilerplate from PostDetailViewModel, can be merged
    fun sendWallpaperToFriends(fileName: String) {
        Log.e("sendWallpaperToFriends", "Initiating sendWallpaperToFriends with ${state.selectedFriends.size} friends.")

        viewModelScope.launch {
            Log.e("sendWallpaperToFriends", "Launching coroutine for sending wallpapers.")

            val pathTobeSent = S3Handler.downloadableLinkToPath(presignedUrl = fileName)
            Log.e("sendWallpaperToFriends", "Path to be sent: $pathTobeSent")

            if (pathTobeSent.isNullOrBlank()) {
                Log.e("sendWallpaperToFriends", "Error: Path to be sent is null or blank.")
                return@launch
            }

            state.selectedFriends.forEach { friend ->
                Log.e("sendWallpaperToFriends", "Sending wallpaper to friend with ID: $friend")

                val result = repository.changeWallpaper(
                    ChangeWallpaperRequest(
                        fileName = pathTobeSent,
                        recipientId = friend.toString(),
                        comment = null,
                        reaction = null,
                        type = "friend_wallpaper"
                    )
                )

                if (result is SmartResult.Success) {
                    Log.e("sendWallpaperToFriends", "Successfully sent wallpaper to friend ID: $friend")
                } else {
                    Log.e("sendWallpaperToFriends", "Failed to send wallpaper to friend ID: $friend")
                    Log.e("sendWallpaperToFriends", "Filename: $pathTobeSent")
                    Log.e("sendWallpaperToFriends", "Error: $result")
                }
            }
            eventChannel.send(PlayEvent.WallpaperSent)
            Log.e("sendWallpaperToFriends", "Finished sending wallpapers to all friends.")
            loadFriendsAndRequests(forceUpdate = true)
        }
    }


    // TODO: Almost Boilerplate from PostDetailViewModel, can be merged
    fun loadSavedWallpapers() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.loadSavedWallpapers(0, 18)

            if (result is SmartResult.Success) {
                state = state.copy(exploreWallpapers = result.data!!.map {
                    ExploreWallpaper(
                        id = it.id,
                        fileName = it.fileName,
                        type = it.type,
                        sentCount = it.sentCount,
                        savedCount = it.savedCount,
                        isLiked = Preferences.isWallpaperLiked(it.id),
                        dateCreated = it.dateCreated
                    )
                }, isLoading = false)
            } else {
                Log.e("loadSavedWallpapers", result.toString())
                state = state.copy(isLoading = false)
            }
        }
    }
}
