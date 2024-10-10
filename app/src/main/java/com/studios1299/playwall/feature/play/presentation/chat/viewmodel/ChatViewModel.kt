package com.studios1299.playwall.feature.play.presentation.chat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.data.WallpaperEventManager
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.response.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.logSmartResult
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.data.DefaultPaginator
import com.studios1299.playwall.feature.play.data.model.Reaction
import com.studios1299.playwall.feature.play.presentation.play.FriendshipStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: CoreRepository,
    private val friendId: String,
) : ViewModel() {
    companion object {
        private const val LOG_TAG = "ChatViewModel"
        private const val PAGE_SIZE = 10
    }

    private val _uiState = MutableStateFlow(MessengerUiState())
    val uiState: StateFlow<MessengerUiState> = _uiState.asStateFlow()
    var paginationState by mutableStateOf(PaginationState())

    private val paginator = DefaultPaginator(
        initialKey = paginationState.page,
        onLoadUpdated = { isLoading ->
            paginationState = paginationState.copy(isLoading = isLoading)
        },
        onRequest = { nextPage ->
            Log.e("Paginator", "Requesting next page: $nextPage")
            chatRepository.getWallpaperHistory(
                userId = friendId,
                page = nextPage,
                pageSize = PAGE_SIZE
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
        onSuccess = { messagesResponse, newKey ->
            Log.e("Paginator", "Success - Loaded ${messagesResponse.size} messages for page $newKey")

            val messages = messagesResponse.map {
                Message(
                    id = it.id,
                    imageUrl = it.fileName,
                    caption = it.comment ?: "",
                    timestamp = it.timeSent,
                    reaction = it.reaction,
                    senderId = it.requesterId,
                    status = it.status,
                    recipientId = it.recipientId
                )
            }

            Log.e("Paginator", "Processed ${messages.size} new messages.")

            paginationState = paginationState.copy(
                page = newKey,
                endReached = messages.isEmpty() || messages.size < PAGE_SIZE
            )

            _uiState.update { currentState ->
                Log.e("Paginator", "Current message count: ${currentState.messages.size}")
                val currentMessages = currentState.messages
                val newMessages = messages.filterNot { newMessage ->
                    currentMessages.any { it.id == newMessage.id }
                }
                Log.e("Paginator", "Added ${newMessages.size} new unique messages.")
                Log.e("Paginator", "Final message count: ${currentState.messages.size}")

                currentState.copy(messages = currentMessages + newMessages)
            }
        }
    )

    private suspend fun handleMessageUpdate(wallpaperHistoryResponse: WallpaperHistoryResponse) {
        Log.e(LOG_TAG, "Handling wallpaper update with id: ${wallpaperHistoryResponse.id}")

        _uiState.update { currentState ->
            val currentMessages = currentState.messages.toMutableList()
            val existingMessageIndex = currentMessages.indexOfFirst { it.id == wallpaperHistoryResponse.id }

            if (existingMessageIndex != -1) {
                Log.e(LOG_TAG, "Updating existing message with id: ${wallpaperHistoryResponse.id}")
                // Update the message if it already exists
                val updatedMessage = currentMessages[existingMessageIndex].copy(
                    caption = wallpaperHistoryResponse.comment ?: "",
                    reaction = wallpaperHistoryResponse.reaction,
                    timestamp = wallpaperHistoryResponse.timeSent
                )
                currentMessages[existingMessageIndex] = updatedMessage
            } else {
                Log.e(LOG_TAG, "Adding new message with id: ${wallpaperHistoryResponse.id}")
                // Add the new message
                val newMessage = Message(
                    id = wallpaperHistoryResponse.id,
                    imageUrl = S3Handler.pathToDownloadableLink(wallpaperHistoryResponse.fileName)!!,
                    caption = wallpaperHistoryResponse.comment ?: "",
                    timestamp = wallpaperHistoryResponse.timeSent,
                    reaction = wallpaperHistoryResponse.reaction,
                    senderId = wallpaperHistoryResponse.requesterId,
                    status =  wallpaperHistoryResponse.status,
                    recipientId = wallpaperHistoryResponse.recipientId
                )
                currentMessages.add(newMessage)
            }

            // Sort messages by timestamp to ensure correct order
            val sortedMessages = currentMessages.sortedByDescending { it.timestamp }

            Log.e(LOG_TAG, "Sorted messages: ${sortedMessages.size} messages in total")

            // Update the state with the sorted message list
            currentState.copy(messages = sortedMessages)
        }

        Log.e(LOG_TAG, "Final state after update: ${_uiState.value.messages}")
    }




    fun loadMessages() {
        Log.d(LOG_TAG, "laoding shit")
        viewModelScope.launch {
            if (!paginationState.endReached && !paginationState.isLoading) {
                paginator.loadNextItems()
            }
        }
    }

    init {
        Log.d(LOG_TAG, "ViewModel initialized")
        loadRecipientData()
        loadMessages()
        setCurrentUser()

        viewModelScope.launch {
            WallpaperEventManager.wallpaperUpdates.collect { wallpaperHistoryResponse ->
                Log.e(LOG_TAG, "Wallpaper event received: $wallpaperHistoryResponse")
                handleMessageUpdate(wallpaperHistoryResponse)
            }
        }
    }

    private val _isConnected = MutableStateFlow(false)
    fun setConnectivityStatus(status: Boolean) {
        _isConnected.value = status
        if (status) {
            reloadMessagesWithImages()
        }
    }

    private fun reloadMessagesWithImages() {
        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.map { message ->
                message.copy()
            }
            currentState.copy(messages = updatedMessages)
        }
    }

    private fun loadRecipientData() {
        viewModelScope.launch {
            when (val result = chatRepository.getUserDataById(friendId)) {
                is SmartResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            recipient = User(
                            id = result.data.id,
                            name = result.data.name,
                            profilePictureUrl = result.data.avatarId,
                            email = result.data.email,
                                since = result.data.since?:"",
                                status = result.data.status?:FriendshipStatus.accepted,
                                requesterId = result.data.requesterId,
                                friendshipId = result.data.friendshipId
                        ))
                    }
                }
                is SmartResult.Error -> {
                    Log.e(LOG_TAG, "Error fetching recipient data: ${result.error}")
                }
            }
        }
    }



    fun setSelectedMessage(message: Message?) {
        _uiState.update { currentState ->
            currentState.copy(selectedMessage = message)
        }
        Log.d(LOG_TAG, "Selected message: $message")
    }

    fun setPickedImage(uri: Uri?) {
        _uiState.update { currentState ->
            currentState.copy(pickedImageUri = uri, pickedImageCaption = "")
        }
    }

    private fun setCurrentUser() {
        viewModelScope.launch {
            when (val result = chatRepository.getUserData()) {
                is SmartResult.Error -> {
                    Log.e(LOG_TAG, "Error: setCurrentUser()")
                }
                is SmartResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(currentUser =
                        User(
                            id = result.data.id,
                            name = result.data.name,
                            email = result.data.email,
                            profilePictureUrl = result.data.avatarId,
                            since = result.data.since?:"",
                            status = result.data.status?:FriendshipStatus.accepted,
                            requesterId = result.data.requesterId,
                            friendshipId = result.data.friendshipId
                        ))
                    }
                }
            }
        }
    }

    fun sendWallpaper(context: Context, uri: Uri?, comment: String?, reaction: String?) {
        viewModelScope.launch {
            var s3Filename: String? = null
            if (uri != null) {
                val wallpaperFile = uriToFile(context, uri)
                if (wallpaperFile == null || !wallpaperFile.exists()) {
                    Log.e("sendWallpaper", "Failed to convert Uri to File or file does not exist.")
                    return@launch
                }
                val filename = chatRepository.uploadFile(wallpaperFile, S3Handler.Folder.WALLPAPERS)
                if (filename is SmartResult.Success) {
                    s3Filename = filename.data
                } else {
                    Log.e("sendWallpaper", "Failed to upload file to S3")
                    return@launch
                }
            }

            if (s3Filename == null) {
                Log.e("sendWallpaper", "Failed: filename is S3 is null")
                return@launch
            }

            // Prepare the ChangeWallpaperRequest
            val changeWallpaperRequest = ChangeWallpaperRequest(
                fileName = s3Filename,
                recipientId = friendId,
                comment = comment,
                reaction = reaction,
                type = "private"
            )

            val response = chatRepository.changeWallpaper(changeWallpaperRequest)

            if (response is SmartResult.Success) {
                Log.e("sendWallpaper", "Wallpaper sent successfully.")
            } else {
                Log.e("sendWallpaper", "Error sending wallpaper: ${logSmartResult(response)}")
            }
        }
    }

    fun reportWallpaper(wallpaperId: Int) {
        viewModelScope.launch {
            chatRepository.reportWallpaper(wallpaperId)
        }
    }

    fun blockFriend(friendshipId: Int, userId: Int) {
        viewModelScope.launch {
            chatRepository.blockUser(friendshipId, userId)
            loadRecipientData()
        }
    }

    fun unblockFriend(friendshipId: Int, userId: Int) {
        viewModelScope.launch {
            chatRepository.unblockUser(friendshipId, userId)
            loadRecipientData()
        }
    }

    fun addOrUpdateReaction(messageId: Int, reaction: Reaction?) {
        Log.e("addOrUpdateReaction", "Reacting...")
        viewModelScope.launch {
            val result = if (reaction != null && reaction.emoji.isNotEmpty()) {
                chatRepository.react(messageId, reaction.name)
            } else {
                chatRepository.react(messageId, null)
            }

            if (result is SmartResult.Success) {
                Log.e("addOrUpdateReaction", "Reacting successful")
                _uiState.update { currentState ->
                    val messages = currentState.messages.map { message ->
                        if (message.id == messageId) {
                            message.copy(reaction = reaction)
                        } else {
                            message
                        }
                    }
                    currentState.copy(messages = messages)
                }
            } else {
                Log.e("addOrUpdateReaction", "Error reacting: $result")
            }
        }
    }

    fun addOrUpdateComment(messageId: Int, comment: String?) {
        viewModelScope.launch {
            val result = if (!comment.isNullOrEmpty()) {
                chatRepository.comment(messageId, comment)
            } else {
                chatRepository.comment(messageId, null)
            }

            if (result is SmartResult.Success) {
                _uiState.update { currentState ->
                    val messages = currentState.messages.map { message ->
                        if (message.id == messageId) {
                            message.copy(caption = comment)
                        } else {
                            message
                        }
                    }
                    currentState.copy(messages = messages)
                }
            } else {
                Log.e("addOrUpdateComment", "Error commenting: $result")
            }
        }
    }

    fun markMessagesAsRead(friendshipId: Int, lastMessageId: Int) {
        Log.e("ChatViewModel", "Marking messages as read for friendshipId: $friendshipId, lastMessageId: $lastMessageId")
        viewModelScope.launch {
            val result = chatRepository.markMessagesAsRead(friendshipId, lastMessageId)
            if (result is SmartResult.Success) {
                Log.e("ChatViewModel", "Messages marked as read successfully for friendshipId: $friendshipId")
            } else if (result is SmartResult.Error) {
                Log.e("ChatViewModel", "Failed to mark messages as read: ${result.error}")
            } else {
                Log.e("ChatViewModel", "Unexpected result when marking messages as read: $result")
            }
        }
    }



    fun getUserNameById(userId: String): String {
        return ""
       // return chatRepository.getUserNameById(userId)
    }

//    fun updateMessageCaption(message: Message, newCaption: String) {
//        val updatedMessage = message.copy(caption = newCaption)
//        chatRepository.updateMessage(updatedMessage)
//
//        Log.d(LOG_TAG, "updateMessageCaption with newCaption: ${newCaption}")
//
//        _uiState.update { currentState ->
//            val updatedMessages = currentState.messages.map {
//                if (it.id == message.id) updatedMessage else it
//            }
//            currentState.copy(messages = updatedMessages)
//        }
//    }
}
