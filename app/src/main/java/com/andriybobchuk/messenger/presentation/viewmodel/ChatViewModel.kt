package com.andriybobchuk.messenger.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andriybobchuk.messenger.data.FakeChatRepository
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.MessageStatus
import com.andriybobchuk.messenger.model.Reaction
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
        private const val PAGE_SIZE = 15
    }

    private val repository = FakeChatRepository()

    private var currentPage = 0
    private var isLoading = false
    private var hasMoreMessages = true

    private val _uiState = MutableStateFlow(MessengerUiState())
    val uiState: StateFlow<MessengerUiState> = _uiState.asStateFlow()


    fun setFullscreenImage(url: String?, caption: String?, messageId: String) {
        _uiState.update { currentState ->
            currentState.copy(fullscreenImageUrl = url, fullscreenCaption = caption, currentMessageId = messageId)
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
        loadMessages()
        setCurrentUser(repository.getCurrentUser())
        setRecipient(repository.getRecipient())
    }

    private fun loadMessages() {
        Log.d(LOG_TAG, "Loading messages...")
        val retrievedMessages = repository.retrieveMessages()
        _uiState.update { currentState ->
            currentState.copy(messages = retrievedMessages)
        }
        Log.d(LOG_TAG, "Messages loaded: ${retrievedMessages.size}")
    }

    private fun loadInitialMessages() {
        currentPage = 0
        loadMessagesPaginated(currentPage)
    }

    fun loadMoreMessages() {
        currentPage++
        loadMessagesPaginated(currentPage)
    }

    private fun loadMessagesPaginated(page: Int) {
        viewModelScope.launch {
            if (isLoading || !hasMoreMessages) return@launch

            isLoading = true
            Log.d(LOG_TAG, "Loading messages for page: $page")

            val retrievedMessages = repository.retrieveMessagesPaginated(page, PAGE_SIZE)
            hasMoreMessages = retrievedMessages.size >= PAGE_SIZE

            _uiState.update { currentState ->
                currentState.copy(messages = retrievedMessages)
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

                Log.d(LOG_TAG, "Message added: $message")
                loadMessages()
            } ?: Log.d(LOG_TAG, "No image URI provided")
        }
    }

    fun deleteMessage(messageId: String) {
        Log.d(LOG_TAG, "Deleting message with ID: $messageId")
        repository.deleteMessage(messageId)
        Log.d(LOG_TAG, "Message deleted")
        loadMessagesPaginated(currentPage)
    }

    fun getUserReaction(messageId: String, userId: String): Reaction? {
        return _uiState.value.messages.find { it.id == messageId }
            ?.reactions?.find { it.userName == userId }
    }

    fun addOrUpdateReaction(messageId: String, reaction: Reaction) {
        _uiState.update { currentState ->
            val messages = currentState.messages.map { message ->
                if (message.id == messageId) {
                    val newReactions = message.reactions.toMutableList().apply {
                        // Remove existing reaction from the user
                        removeAll { it.userName == reaction.userName }
                        // Add the new reaction if it's not a removal action
                        if (reaction.emoji.isNotEmpty()) {
                            add(reaction)
                        }
                    }
                    message.copy(reactions = newReactions)
                } else {
                    message
                }
            }
            currentState.copy(messages = messages)
        }
    }

    fun removeReaction(messageId: String, userId: String) {
        _uiState.update { currentState ->
            val messages = currentState.messages.map { message ->
                if (message.id == messageId) {
                    val newReactions = message.reactions.filterNot { it.userName == userId }
                    message.copy(reactions = newReactions)
                } else {
                    message
                }
            }
            currentState.copy(messages = messages)
        }
    }

    fun getUserNameById(userId: String): String {
        return repository.getUserNameById(userId)
    }

    fun getMessageById(messageId: String): Message? {
        return repository.getMessageById(messageId)
    }

    fun updateMessageCaption(messageId: String, newCaption: String) {
        viewModelScope.launch {
            // Retrieve the existing message
            val existingMessage = repository.getMessageById(messageId)
            existingMessage?.let { message ->
                // Update the message with the new caption
                val updatedMessage = message.copy(caption = newCaption)
                repository.updateMessage(updatedMessage)
                // Reload messages to update the UI
                loadMessages()
            } ?: Log.d(LOG_TAG, "Message with ID $messageId not found.")
        }
    }
}
