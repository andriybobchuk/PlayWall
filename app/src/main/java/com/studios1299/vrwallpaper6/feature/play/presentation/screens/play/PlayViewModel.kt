package com.studios1299.vrwallpaper6.feature.play.presentation.screens.play

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.vrwallpaper6.R
import com.studios1299.vrwallpaper6.core.presentation.UiText
import com.studios1299.vrwallpaper6.feature.play.domain.ChatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PlayViewModel(
    private val repository: ChatRepository
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
            is PlayAction.OnFriendClick -> navigateToChat(action.friendId)
            is PlayAction.OnAcceptFriendRequest -> acceptFriendRequest(action.requestId)
            is PlayAction.OnRejectFriendRequest -> rejectFriendRequest(action.requestId)
            PlayAction.Refresh -> loadFriendsAndRequests()
            PlayAction.OnFriendMute -> TODO()
            PlayAction.OnFriendRemove -> TODO()
            PlayAction.OnFriendUnMute -> TODO()
            PlayAction.OnInviteClick -> TODO()
            PlayAction.OnSelectClick -> TODO()
            PlayAction.OnSelectFriend -> TODO()
            PlayAction.OnSetWallpaperClick -> TODO()
        }
    }

    private fun getSortedFriends(friends: List<Friend>): List<Friend> {
        return friends.sortedWith(compareBy({ it.muted }, { it.name })) // Mute friends last
    }

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

    private fun navigateToChat(friendId: String) {
        viewModelScope.launch {
            eventChannel.send(PlayEvent.NavigateToChat(friendId))
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
