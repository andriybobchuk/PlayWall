package com.studios1299.playwall.feature.play.presentation.play

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.core.presentation.asUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
class PlayViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    var state by mutableStateOf(PlayState())
        private set

    private val eventChannel = Channel<PlayEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        loadFriendsAndRequests()
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
            is PlayAction.OnSelectedFromGallery -> send(action.uri)
            is PlayAction.OnSelectedFromSaved -> {}
            PlayAction.LoadPhotos -> loadPhotos()
            is PlayAction.OnFriendMute -> blockFriend(action.friendshipId, action.userId)
            is PlayAction.OnFriendRemove -> removeFriend(action.friendshipId)
            is PlayAction.OnFriendUnMute -> unblockFriend(action.friendshipId, action.friendshipId)
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
    }

    private fun navigateToChat(friendId: Int) {
        viewModelScope.launch {
            eventChannel.send(PlayEvent.NavigateToChat(friendId))
        }
    }

    private fun send(uri: Uri) {
        viewModelScope.launch {
            eventChannel.send(PlayEvent.WallpaperSent)
        }
    }

    private fun send(wallpaper: String) {
        viewModelScope.launch {
            eventChannel.send(PlayEvent.WallpaperSent)
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
           // val photos = repository.getExploreItems()
            //state = state.copy(exploreWallpapers = photos, isLoading = false)
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
                        when (inviteFriend.error) {
                            DataError.Network.NOT_FOUND -> "User not found, is the email correct?"
                            else -> "An unknown error occurred. Please try again."
                        }
                    }
                    else -> "An unexpected error occurred. Please try again."
                }
                eventChannel.send(PlayEvent.ShowError(UiText.DynamicString(errorMessage)))
            }
            state = state.copy(
                isLoading = false
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun loadFriendsAndRequests() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            when (val friendsResult = repository.getFriends()) {
                is SmartResult.Success -> {
                    state = state.copy(
                        friends = friendsResult.data,
                        isLoading = false
                    )
                }
                is SmartResult.Error -> {
                    eventChannel.send(PlayEvent.ShowError(friendsResult.error.asUiText()))
                }
            }

            when (val friendRequestsResult = repository.getFriendRequests()) {
                is SmartResult.Success -> {
                    state = state.copy(
                        friendRequests = friendRequestsResult.data,
                        isLoading = false
                    )
                }
                is SmartResult.Error -> {
                    eventChannel.send(PlayEvent.ShowError(friendRequestsResult.error.asUiText()))
                }
            }
        }
    }

    private fun acceptFriendRequest(requestId: Int) {
        viewModelScope.launch {
            val result = repository.acceptFriendRequest(AcceptRequest(requestId))
            if (result is SmartResult.Success) {
                eventChannel.send(PlayEvent.FriendRequestAccepted)
                loadFriendsAndRequests()
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
                loadFriendsAndRequests()
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
                // Update the list of friends and set the status to "blocked"
                val updatedFriends = state.friends.map { friend ->
                    if (friend.friendshipId == friendshipId) {
                        friend.copy(status = FriendshipStatus.blocked) // Assuming muted as blocked, adjust if needed
                    } else {
                        friend
                    }
                }
                state = state.copy(friends = updatedFriends, isLoading = false)
                loadFriendsAndRequests()
            } else {
                // Handle failure case, e.g., show error message
                state = state.copy(isLoading = false)
            }
        }
    }

    fun unblockFriend(friendshipId: Int, userId: Int) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.unblockUser(friendshipId, userId)

            if (result is SmartResult.Success) {
                // Update the list of friends and set the status to "accepted"
                val updatedFriends = state.friends.map { friend ->
                    if (friend.friendshipId == friendshipId) {
                        friend.copy(status = FriendshipStatus.accepted) // Assuming muted as blocked, adjust if needed
                    } else {
                        friend
                    }
                }
                state = state.copy(friends = updatedFriends, isLoading = false)
            } else {
                // Handle failure case, e.g., show error message
                state = state.copy(isLoading = false)
            }
        }
    }


    fun removeFriend(friendshipId: Int) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val result = repository.removeUser(friendshipId)

            if (result is SmartResult.Success) {
                // Remove the friend from the friends list
                val updatedFriends = state.friends.filter { friend -> friend.friendshipId != friendshipId }
                state = state.copy(friends = updatedFriends, isLoading = false)
            } else {
                // Handle failure case
                state = state.copy(isLoading = false)
            }
        }
    }

}
