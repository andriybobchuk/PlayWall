package com.studios1299.playwall.feature.play.presentation.play

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.domain.CoreRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

        state.friendId.textAsFlow()
            .onEach { email ->
                onAction(PlayAction.OnSearchUser(email.toString()))
            }.launchIn(viewModelScope)
    }

    fun onAction(action: PlayAction) {
        when(action) {
            is PlayAction.OnAcceptFriendRequest -> acceptFriendRequest(action.requestId)
            is PlayAction.OnRejectFriendRequest -> rejectFriendRequest(action.requestId)
            PlayAction.Refresh -> loadFriendsAndRequests()
            PlayAction.OnFriendMute -> TODO()
            PlayAction.OnFriendRemove -> TODO()
            PlayAction.OnFriendUnMute -> TODO()
            PlayAction.OnInviteClick -> TODO()
            is PlayAction.OnSearchUser -> searchUser(action.userEmail)
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
            is PlayAction.OnSelectedFromSaved -> send(action.selectedWallpaper)
        }
    }

    private fun enterSelectMode() {
        state = state.copy(isSelectMode = true)
    }

    private fun exitSelectMode() {
        state = state.copy(isSelectMode = false, selectedFriends = emptyList())
    }

    private fun toggleFriendSelection(friendId: String) {
        val currentSelection = state.selectedFriends.toMutableList()
        if (currentSelection.contains(friendId)) {
            currentSelection.remove(friendId)
        } else {
            currentSelection.add(friendId)
        }
        state = state.copy(selectedFriends = currentSelection)
    }

    private fun navigateToChat(friendId: String) {
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

    @OptIn(ExperimentalFoundationApi::class)
    private fun searchUser(userEmail: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val searchResults = repository.searchUsers(userEmail)
            state = state.copy(searchResults = searchResults, isLoading = false)
        }
    }

    private fun getSortedFriends(friends: List<Friend>): List<Friend> {
        return friends.sortedWith(compareBy({ it.muted }, { it.name })) // Mute friends last
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun loadFriendsAndRequests() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val friendsResult = repository.getFriends()
            val friendRequestsResult = repository.getFriendRequests()
            state = state.copy(
                friends = getSortedFriends(friendsResult),
                friendRequests = friendRequestsResult,
                isLoading = false
            )
        }
    }

    private fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.acceptFriendRequest(requestId)
            if (result) {
                eventChannel.send(PlayEvent.FriendRequestAccepted)
                loadFriendsAndRequests()
            } else {
              // eventChannel.send(PlayEvent.ShowError(UiText.StringResource(R.string.error_accepting_request)))
            }
        }
    }

    private fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.rejectFriendRequest(requestId)
            if (result) {
                eventChannel.send(PlayEvent.FriendRequestRejected)
                loadFriendsAndRequests()
            } else {
                //eventChannel.send(PlayEvent.ShowError(UiText.StringResource(R.string.error_rejecting_request)))
            }
        }
    }
}
