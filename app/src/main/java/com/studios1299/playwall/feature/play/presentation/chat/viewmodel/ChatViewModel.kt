package com.studios1299.playwall.feature.play.presentation.chat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.logSmartResult
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.data.DefaultPaginator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val chatRepository: CoreRepository,
    private val friendId: String,
) : ViewModel() {
    companion object {
        private const val LOG_TAG = "ChatViewModel"
        private const val PAGE_SIZE = 100
    }

    private val _uiState = MutableStateFlow(MessengerUiState())
    val uiState: StateFlow<MessengerUiState> = _uiState.asStateFlow()
    var paginationState by mutableStateOf(PaginationState())

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

    private val paginator = DefaultPaginator(
        initialKey = paginationState.page,
        onLoadUpdated = {
            paginationState = paginationState.copy(isLoading = it)
        },
        onRequest = { nextPage ->

           // chatRepository.retrieveMessages(nextPage, PAGE_SIZE)
            Log.e(LOG_TAG, "On request ")
            chatRepository.getWallpaperHistory(
                userId = friendId,
                page = nextPage,
                pageSize = PAGE_SIZE
            )
        },
        getNextKey = {
            paginationState.page + 1
        },
        onError = {
            paginationState = paginationState.copy(error = it?.localizedMessage)
        },
        onSuccess = { messagesResponse, newKey ->
            val messages = messagesResponse.map {
                Message(
                    id = it.id,
                    imageUrl = it.fileName,
                    caption = it.comment?:"",
                    timestamp = it.dateCreated,
                    reactions = listOf(it.reaction?:""),
                    senderId = it.requesterId,
                    status = MessageStatus.SENT,
                    recipientId = it.recipientId
                )
            }
            paginationState = paginationState.copy(
                page = newKey,
                endReached = messages.isEmpty() || messages.size < PAGE_SIZE
            )
            _uiState.update { currentState ->
                // Filter out any duplicates
                val currentMessages = currentState.messages
                val newMessages = messages.filterNot { newMessage ->
                    currentMessages.any { it.id == newMessage.id }
                }
                currentState.copy(messages = currentMessages + newMessages)
            }
        }
    )

    init {
        Log.d(LOG_TAG, "ViewModel initialized")
        loadRecipientData()
        loadMessages()
        setCurrentUser(chatRepository.getCurrentUser())
    }

    private fun loadRecipientData() {
        viewModelScope.launch {
            when (val result = chatRepository.getRecipientData(friendId)) {
                is SmartResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            recipient = User(
                            id = "",
                            name = result.data.name,
                            profilePictureUrl = result.data.avatarId,
                            email = result.data.email,
                        ))
                    }
                }
                is SmartResult.Error -> {
                    Log.e(LOG_TAG, "Error fetching recipient data: ${result.error}")
                }
            }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            if (!paginationState.endReached) {
                paginator.loadNextItems()
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

    private fun setCurrentUser(user: User) {
        _uiState.update { currentState ->
            currentState.copy(currentUser = user)
        }
    }

//    fun sendImage(imageUri: Uri?, caption: String) {
//        viewModelScope.launch {
//            Log.d(LOG_TAG, "Sending image with URI: $imageUri and caption: $caption")
//
//            val messageId = 60
//            Log.e(LOG_TAG, "new ID: $messageId")
//            val timestamp = System.currentTimeMillis()
//
//            imageUri?.let { uri ->
//                val message = Message(
//                    id = messageId,
//                    imageUrl = uri.toString(),
//                    caption = caption,
//                    timestamp = timestamp.toString(),
//                    status = MessageStatus.SENT,
//                    reactions = listOf(),
//                    senderId = _uiState.value.currentUser!!.id,
//                    recipientId = _uiState.value.recipient!!.id
//                )
//                chatRepository.addMessage(message)
//                Log.d(LOG_TAG, "Message added: $message")
//                // Add message to current UI state without reloading all messages and
//                // NOT to trigger recomposition!!
//                _uiState.update { currentState ->
//                    currentState.copy(messages = listOf(message) + currentState.messages)
//                }
//            } ?: Log.e(LOG_TAG, "No image URI provided")
//        }
//    }

    fun sendWallpaper(context: Context, uri: Uri?, comment: String?, reaction: String?) {
        viewModelScope.launch {

            Log.e("DEBUGG", "Friend/recipient id: " + friendId)



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
                reaction = reaction
            )

            val response = chatRepository.changeWallpaper(changeWallpaperRequest)

            if (response is SmartResult.Success) {
                Log.e("sendWallpaper", "Wallpaper sent successfully.")
            } else {
                Log.e("sendWallpaper", "Error sending wallpaper: ${logSmartResult(response)}")
            }
        }
    }


//    fun deleteMessage(messageId: String) {
//        Log.d(LOG_TAG, "Deleting message with ID: $messageId")
//        chatRepository.deleteMessage(messageId)
//        // Remove message DIRECTLY from UI state NOT to trigger recomposition!!
//        _uiState.update { currentState ->
//            val updatedMessages = currentState.messages.filterNot { it.id == messageId }
//            currentState.copy(messages = updatedMessages)
//        }
//        Log.d(LOG_TAG, "Message deleted")
//    }

//    fun getUserReaction(messageId: String, userId: String): Reaction? {
//        return _uiState.value.messages.find { it.id == messageId }
//            ?.reactions?.find { it.userName == userId }
//    }
//
//    fun addOrUpdateReaction(messageId: String, reaction: Reaction) {
//        _uiState.update { currentState ->
//            val messages = currentState.messages.map { message ->
//                if (message.id == messageId) {
//                    val newReactions = message.reactions.toMutableList().apply {
//                        // Remove existing reaction from the user
//                        removeAll { it.userName == reaction.userName }
//                        // Add the new reaction if it's not a removal action
//                        if (reaction.emoji.isNotEmpty()) {
//                            add(reaction)
//                        }
//                    }
//                    message.copy(reactions = newReactions)
//                } else {
//                    message
//                }
//            }
//            currentState.copy(messages = messages)
//        }
//    }

//    fun removeReaction(messageId: String, userId: String) {
//        _uiState.update { currentState ->
//            val messages = currentState.messages.map { message ->
//                if (message.id == messageId) {
//                    val newReactions = message.reactions.filterNot { it.userName == userId }
//                    message.copy(reactions = newReactions)
//                } else {
//                    message
//                }
//            }
//            currentState.copy(messages = messages)
//        }
//    }

    fun getUserNameById(userId: String): String {
        return chatRepository.getUserNameById(userId)
    }

    fun updateMessageCaption(message: Message, newCaption: String) {
        val updatedMessage = message.copy(caption = newCaption)
        chatRepository.updateMessage(updatedMessage)

        Log.d(LOG_TAG, "updateMessageCaption with newCaption: ${newCaption}")

        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.map {
                if (it.id == message.id) updatedMessage else it
            }
            currentState.copy(messages = updatedMessages)
        }
    }

//    fun getLastMessageId(): String {
//        val result = chatRepository.getLastMessageId()
//        result.onSuccess { lastMessageId ->
//            Log.d(LOG_TAG, "Last message ID: $lastMessageId")
//            return lastMessageId
//        }.onFailure { exception ->
//            Log.e(LOG_TAG, "Failed to get last message ID: ${exception.localizedMessage}")
//            return ""
//        }
//        return ""
//    }
}
