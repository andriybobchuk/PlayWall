package com.andriybobchuk.messenger.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andriybobchuk.messenger.data.FakeChatRepository
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.MessageStatus
import com.andriybobchuk.messenger.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    companion object {
        private const val LOG_TAG = "ChatViewModel"
        private const val PAGE_SIZE = 7
    }

    private val repository = FakeChatRepository()

    private var currentPage = 0
    private var isLoading = false
    private var hasMoreMessages = true

    private val _uiState = MutableStateFlow(MessengerUiState())
    val uiState: StateFlow<MessengerUiState> = _uiState.asStateFlow()


    fun setFullscreenImage(url: String?, caption: String?) {
        _uiState.update { currentState ->
            currentState.copy(fullscreenImageUrl = url, fullscreenCaption = caption)
        }
    }

    fun setPickedImage(uri: Uri?) {
        _uiState.update { currentState ->
            currentState.copy(pickedImageUri = uri, pickedImageCaption = "")
        }
    }

    fun updatePickedImageCaption(caption: String) {
        _uiState.update { currentState ->
            currentState.copy(pickedImageCaption = caption)
        }
    }

    fun setCurrentUser(user: User) {
        _uiState.update { currentState ->
            currentState.copy(currentUser = user)
        }
    }

    fun setRecipient(user: User) {
        _uiState.update { currentState ->
            currentState.copy(recipient = user)
        }
    }

    fun setMessages(messages: List<Message>) {
        _uiState.update { currentState ->
            currentState.copy(messages = messages)
        }
    }

    init {
        Log.d(LOG_TAG, "ViewModel initialized")
        loadInitialMessages()
        setCurrentUser(repository.getCurrentUser())
        setRecipient(repository.getRecipient())
    }

    private fun loadInitialMessages() {
        currentPage = 0
        loadMessages(currentPage)
    }

    fun loadMoreMessages() {
        currentPage++
        loadMessages(currentPage)
    }

    private fun loadMessages(page: Int) {
        viewModelScope.launch {
            if (isLoading || !hasMoreMessages) return@launch

            isLoading = true
            Log.d(LOG_TAG, "Loading messages for page: $page")

            val retrievedMessages = repository.retrieveMessages(page, PAGE_SIZE)
            hasMoreMessages = retrievedMessages.size >= PAGE_SIZE

            _uiState.update { currentState ->
                currentState.copy(messages = currentState.messages + retrievedMessages)
            }

            isLoading = false
            Log.d(LOG_TAG, "Messages loaded for page $page: ${retrievedMessages.size}")
        }
    }


    fun sendImage(imageUri: Uri?, caption: String) {
        viewModelScope.launch {
            Log.d(LOG_TAG, "Sending image with URI: $imageUri and caption: $caption")

            val messageId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()

            imageUri?.let { uri ->
                val message = Message(
                    id = messageId,
                    imageUrl = uri.toString(),
                    caption = caption,
                    timestamp = timestamp,
                    status = MessageStatus.SENT,
                    reactions = listOf(),
                    senderId = _uiState.value.currentUser!!.id,
                    recipientId = _uiState.value.recipient!!.id
                )
                repository.addMessage(message)
                loadMessages(currentPage) // Reload messages to include the new one
            } ?: Log.d(LOG_TAG, "No image URI provided")
        }
    }

    fun deleteMessage(messageId: String) {
        Log.d(LOG_TAG, "Deleting message with ID: $messageId")
        repository.deleteMessage(messageId)
        Log.d(LOG_TAG, "Message deleted")
        loadMessages(currentPage) // Reload messages to reflect the deletion
    }
}
