package com.andriybobchuk.messenger.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andriybobchuk.messenger.data.FakeChatRepository
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.MessageStatus
import com.andriybobchuk.messenger.model.Reaction
import com.andriybobchuk.messenger.model.User
import com.andriybobchuk.messenger.presentation.pagination.DefaultPaginator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    companion object {
        private const val LOG_TAG = "ChatViewModel"
        private const val PAGE_SIZE = 5
    }

    private val repository = FakeChatRepository()

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

    fun reloadMessagesWithImages() {
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
            repository.retrieveMessages(nextPage, PAGE_SIZE)
        },
        getNextKey = {
            paginationState.page + 1
        },
        onError = {
            paginationState = paginationState.copy(error = it?.localizedMessage)
        },
        onSuccess = { messages, newKey ->
            paginationState = paginationState.copy(
                page = newKey,
                endReached = messages.isEmpty()
            )
            _uiState.update { currentState ->
                currentState.copy(messages = currentState.messages + messages)
            }
        }
    )

    init {
        Log.d(LOG_TAG, "ViewModel initialized")
        loadMessages()
        setCurrentUser(repository.getCurrentUser())
        setRecipient(repository.getRecipient())
    }

    fun loadMessages() {
        viewModelScope.launch {
            paginator.loadNextItems()
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
                // Add message to current UI state without reloading all messages and
                // NOT to trigger recomposition!!
                _uiState.update { currentState ->
                    currentState.copy(messages = listOf(message) + currentState.messages)
                }
            } ?: Log.e(LOG_TAG, "No image URI provided")
        }
    }

    fun deleteMessage(messageId: String) {
        Log.d(LOG_TAG, "Deleting message with ID: $messageId")
        repository.deleteMessage(messageId)
        // Remove message DIRECTLY from UI state NOT to trigger recomposition!!
        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.filterNot { it.id == messageId }
            currentState.copy(messages = updatedMessages)
        }
        Log.d(LOG_TAG, "Message deleted")
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

    fun updateMessageCaption(message: Message, newCaption: String) {
        val updatedMessage = message.copy(caption = newCaption)
        repository.updateMessage(updatedMessage)

        Log.d(LOG_TAG, "updateMessageCaption with newCaption: ${newCaption}")

        // Update the message in the UI state
        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.map {
                if (it.id == message.id) updatedMessage else it
            }
            currentState.copy(messages = updatedMessages)
        }
    }

    fun getLastMessageId(): String {
        val result = repository.getLastMessageId()
        result.onSuccess { lastMessageId ->
            Log.d(LOG_TAG, "Last message ID: $lastMessageId")
            return lastMessageId
        }.onFailure { exception ->
            Log.e(LOG_TAG, "Failed to get last message ID: ${exception.localizedMessage}")
            return ""
        }
        return ""
    }
}
