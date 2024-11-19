package com.studios1299.playwall.play.presentation.chat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.WallpaperEventManager
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.response.wallpapers.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.play.data.model.Message
import com.studios1299.playwall.play.data.model.User
import com.studios1299.playwall.play.data.DefaultPaginator
import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.data.model.Reaction
import com.studios1299.playwall.play.presentation.play.FriendshipStatus
import com.studios1299.playwall.play.presentation.play.PlayRefreshState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatViewModel(
    private val chatRepository: CoreRepository,
    private val friendId: String
) : ViewModel() {
    companion object {
        private const val LOG_TAG = "ChatViewModel"
        private const val PAGE_SIZE = 20
    }

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages = _errorMessages.asSharedFlow()
    fun sendErrorMessage(message: String) {
        viewModelScope.launch {
            _errorMessages.emit(message)
        }
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
            Log.e("Paginator", "paginationState.page: ${paginationState.page}")
            var forceUpdate = false
            if (nextPage != 0) {
                forceUpdate = true
            }
            Log.e("Paginator", "forceUpdate = $forceUpdate")

            chatRepository.getWallpaperHistory(
                userId = friendId,
                page = nextPage,
                pageSize = PAGE_SIZE,
                forceUpdate = forceUpdate
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
            Log.e(
                "Paginator",
                "Success - Loaded ${messagesResponse.size} messages for page $newKey"
            )

            val messages = messagesResponse.map {
                Message(
                    id = it.id,
                    imageUrl = it.fileName,
                    caption = it.comment ?: "",
                    timestamp = it.timeSent,
                    reaction = it.reaction,
                    senderId = it.requesterId,
                    status = it.status ?: MessageStatus.unread,
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

    suspend fun waitForUserAndRecipient(): Pair<User, User> {
        return combine(
            _uiState.map { it.currentUser }.filterNotNull(),
            _uiState.map { it.recipient }.filterNotNull()
        ) { currentUser, recipient ->
            currentUser to recipient
        }.first() // Suspend until both values are non-null
    }
    fun resetChat() {
        Log.e(LOG_TAG, "Resetting chat..")
        viewModelScope.launch {
            // Wait until both currentUser and recipient are available
            val (currentUser, recipient) = try {
                waitForUserAndRecipient()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to retrieve currentUser and recipient: ${e.localizedMessage}")
                sendErrorMessage("User data is unavailable.")
                return@launch
            }

            // Clear current messages in the UI state
            _uiState.update { it.copy(messages = emptyList()) }
            Log.e(LOG_TAG, "Clearing old messages from UI..")

            try {
                Log.e(LOG_TAG, "Clearing local cached messages..")
                MyApp.appModule.chatDao.clearChatCache(currentUser.id, recipient.id)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error clearing chat cache: ${e.localizedMessage}")
                sendErrorMessage("Failed to reset chat.")
            }

            Log.e(LOG_TAG, "Resetting pagination state")
            paginationState = PaginationState(page = 0, isLoading = false, endReached = false)
            paginator.reset()

            // Reload the first page of messages
            try {
                Log.e(LOG_TAG, "Loading messages after reset")
                loadMessages()
                loadRecipientData(forceUpdate = true)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error loading messages: ${e.localizedMessage}")
                sendErrorMessage("Failed to load messages.")
            }
        }
    }

    private suspend fun handleMessageUpdate(wallpaperHistoryResponse: WallpaperHistoryResponse) {
        Log.e(LOG_TAG, "Handling wallpaper update with id: ${wallpaperHistoryResponse.id}")

        _uiState.update { currentState ->
            val currentMessages = currentState.messages.toMutableList()
            val existingMessageIndex =
                currentMessages.indexOfFirst { it.id == wallpaperHistoryResponse.id }

            if (existingMessageIndex != -1) {
                Log.e(LOG_TAG, "Updating existing message with id: ${wallpaperHistoryResponse.id}")
                // Update the message if it already exists
                if (wallpaperHistoryResponse.comment != null) {
                    currentMessages[existingMessageIndex] =
                        currentMessages[existingMessageIndex].copy(
                            caption = wallpaperHistoryResponse.comment,
                        )
                } else if (wallpaperHistoryResponse.reaction != null) {
                    if (wallpaperHistoryResponse.reaction == Reaction.none) {
                        currentMessages[existingMessageIndex] =
                            currentMessages[existingMessageIndex].copy(
                                reaction = null,
                            )
                    } else {
                        currentMessages[existingMessageIndex] =
                            currentMessages[existingMessageIndex].copy(
                                reaction = wallpaperHistoryResponse.reaction,
                            )
                    }
                } else if (wallpaperHistoryResponse.status != null) {
                    Log.e(LOG_TAG, "Status changed to: ${wallpaperHistoryResponse.status}")
                    currentMessages[existingMessageIndex] =
                        currentMessages[existingMessageIndex].copy(
                            status = wallpaperHistoryResponse.status,
                        )
                }
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
                    status = wallpaperHistoryResponse.status ?: MessageStatus.unread,
                    recipientId = wallpaperHistoryResponse.recipientId
                )
                currentMessages.add(newMessage)
                chatRepository.saveMessageToLocal(newMessage)
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
        viewModelScope.launch {
            if (!paginationState.endReached && !paginationState.isLoading) {
                paginator.loadNextItems()
            }
        }
    }

    private var isInitialLoad = true
    init {
        Log.d(LOG_TAG, "ViewModel initialized")
        loadRecipientData()
        setCurrentUser()

        if (WallpaperNotificationForChat.isNewWallpaperReceived()) {
            Log.e(LOG_TAG, "New wallpaper received, resetting chat..")
            resetChat()
            WallpaperNotificationForChat.setNewWallpaperReceived(false) // Reset flag after refreshing
        } else {
            loadMessages()
        }

        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                _uiState.update {
                    it.copy(isOnline = online)
                }
                if (online && !isInitialLoad) {
                    loadRecipientData()
                    loadMessages()
                    setCurrentUser()
                }
                isInitialLoad = false
            }
        }
        viewModelScope.launch {
            WallpaperEventManager.wallpaperUpdates.collect { wallpaperHistoryResponse ->
                Log.e(LOG_TAG, "Wallpaper event received: $wallpaperHistoryResponse")
                handleMessageUpdate(wallpaperHistoryResponse)
                PlayRefreshState.triggerRefresh()
                Log.e("observeRefreshFlag", "Refresh was triggered after receiving a wallpaper")
            }
        }
        viewModelScope.launch {
            WallpaperEventManager.friendUpdates.collect { friendEvent ->
                Log.e(LOG_TAG, "Friendship event received: $friendEvent")
                if (friendEvent.type == "friend_remove") {
                    _uiState.update { it.copy(goBack = true) }
                } else if (friendEvent.type == "friend_block") {
                    _uiState.update { it.copy(goBack = true) }
                } else {
                    loadRecipientData()
                }
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

    private fun loadRecipientData(forceUpdate: Boolean = false) {
        viewModelScope.launch {
            when (val result = chatRepository.getUserDataById(friendId, forceUpdate)) {
                is SmartResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            recipient = result.data?.let {
                                User(
                                    id = it.id,
                                    name = result.data.name,
                                    profilePictureUrl = result.data.avatarId,
                                    email = result.data.email,
                                    since = result.data.since ?: "",
                                    status = result.data.status ?: FriendshipStatus.accepted,
                                    requesterId = result.data.requesterId,
                                    friendshipId = result.data.friendshipId,
                                    screenRatio = result.data.screenRatio
                                )
                            })
                    }
                }

                is SmartResult.Error -> {
                    Log.e(LOG_TAG, "Error fetching recipient data: ${result.errorBody}")
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
        Log.e(LOG_TAG, "setCurrentUser(), start")
        viewModelScope.launch {
            when (val result = chatRepository.getUserData()) {
                is SmartResult.Error -> {
                    Log.e(LOG_TAG, "Error: setCurrentUser()")
                }
                is SmartResult.Success -> {
                    Log.e(LOG_TAG, "setCurrentUser(), reult currentuserid: ${result.data?.id}")
                    _uiState.update { currentState ->
                        currentState.copy(currentUser =
                        result.data?.let {
                            User(
                                id = it.id,
                                name = result.data.name,
                                email = result.data.email,
                                profilePictureUrl = result.data.avatarId,
                                since = result.data.since ?: "",
                                status = result.data.status ?: FriendshipStatus.accepted,
                                requesterId = result.data.requesterId,
                                friendshipId = result.data.friendshipId,
                                screenRatio = result.data.screenRatio
                            )
                        })
                    }
                }
            }
            Log.e(LOG_TAG, "setCurrentUser(), currentuserid: ${uiState.value.currentUser?.id}")
        }
    }

    fun sendWallpaper(context: Context, uri: Uri?, comment: String?, reaction: String?) {
        Log.v(
            "sendWallpaper",
            "Function called with uri: $uri, comment: $comment, reaction: $reaction"
        )
        Log.e("observeRefreshFlag", "Refresh was triggered after sending a wallpaper")
        PlayRefreshState.triggerRefresh()

        viewModelScope.launch {
            var s3Filename: String? = null

            if (uri != null) {
                Log.d("sendWallpaper", "Converting Uri to File...")
                val wallpaperFile = uriToFile(context, uri)
                if (wallpaperFile == null || !wallpaperFile.exists()) {
                    Log.e("sendWallpaper", "Failed to convert Uri to File or file does not exist.")
                    return@launch
                }
                Log.d("sendWallpaper", "Uploading file to S3...")
                val filename = chatRepository.uploadFile(wallpaperFile, S3Handler.Folder.WALLPAPERS)
                if (filename is SmartResult.Success) {
                    s3Filename = filename.data
                    Log.d("sendWallpaper", "File uploaded successfully. S3 Filename: $s3Filename")
                } else {
                    return@launch
                }
            } else {
                Log.e("sendWallpaper", "Uri is null, cannot proceed.")
            }

            if (s3Filename == null) {
                Log.e("sendWallpaper", "Failed: filename in S3 is null")
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

            Log.d("sendWallpaper", "ChangeWallpaperRequest prepared: $changeWallpaperRequest")

            // Optimistically update the UI state with the new message
            val currentTimestamp =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC") // Set timezone to UTC
                }.format(Date())

            val optimisticMessage = Message(
                id = -1, // Temporary ID, replace with actual once received
                imageUrl = S3Handler.pathToDownloadableLink(s3Filename)!!,
                caption = comment,
                timestamp = currentTimestamp,
                status = MessageStatus.unread,
                reaction = null,
                senderId = uiState.value.currentUser!!.id,
                recipientId = friendId.toInt()
            )

            Log.d("sendWallpaper", "Optimistically adding message: $optimisticMessage")

            // Update the UI state immediately
            _uiState.update { currentState ->
                val messages = currentState.messages + optimisticMessage

                val sortedMessages = messages.sortedByDescending {
                    it.timestamp.takeIf { it.isNotEmpty() }?.let { timestamp ->
                        // Parse the timestamp string to a Date object and convert it to Long for sorting
                        SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            Locale.getDefault()
                        ).apply {
                            timeZone = TimeZone.getTimeZone("UTC") // Set timezone to UTC
                        }.parse(timestamp)?.time
                    } ?: Long.MAX_VALUE // If timestamp is empty, place it at the end
                }
                Log.d(
                    "sendWallpaper",
                    "UI state updated with new message. Current message count: ${messages.size}"
                )
                currentState.copy(messages = sortedMessages)
            }

            // Send the wallpaper request
            Log.d("sendWallpaper", "Sending wallpaper request to server...")
            val response = chatRepository.changeWallpaper(changeWallpaperRequest)

            if (response is SmartResult.Success) {
                Log.e("sendWallpaper", "Wallpaper sent successfully. Response: $response")
                // Update the message with the actual data returned from the response
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.map { message ->
                        if (message.id == -1 && response.data != null) { // Find the optimistic message
                            Log.e(
                                "sendWallpaper",
                                "Updating optimistic message with server response data."
                            )
                            message.copy(
                                id = response.data.id, // Update with the actual ID
                                timestamp = response.data.timestamp ?: "",
                                status = response.data.status ?: MessageStatus.unread,
                                reaction = response.data.reaction
                            )
                        } else {
                            message
                        }
                    }
                    Log.e(
                        "sendWallpaper",
                        "UI state updated with final message data. Current message count: ${updatedMessages.size}"
                    )

                    currentState.copy(messages = updatedMessages)
                }
            } else if (response is SmartResult.Error) {
                sendErrorMessage(response.message?:response.errorBody?:"Wallpaper could not be sent")
                // Remove the optimistic message if the request fails
                _uiState.update { currentState ->
                    val filteredMessages =
                        currentState.messages.filter { it.id != -1 } // Remove optimistic message
                    Log.e(
                        "sendWallpaper",
                        "Optimistic message removed. Current message count: ${filteredMessages.size}"
                    )
                    currentState.copy(messages = filteredMessages)
                }

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
        Log.e("addOrUpdateReaction", "Reacting with " + messageId)
        WallpaperNotificationForChat.setNewWallpaperReceived(true)
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
            WallpaperNotificationForChat.setNewWallpaperReceived(true)
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
        Log.e(
            "ChatViewModel",
            "Marking messages as read for friendshipId: $friendshipId, lastMessageId: $lastMessageId"
        )
        viewModelScope.launch {
            val result = chatRepository.markMessagesAsRead(friendshipId, lastMessageId)
            if (result is SmartResult.Success) {
                Log.e(
                    "ChatViewModel",
                    "Messages marked as read successfully for friendshipId: $friendshipId"
                )
            } else if (result is SmartResult.Error) {
                Log.e("ChatViewModel", "Failed to mark messages as read: ${result.errorBody}")
            } else {
                Log.e("ChatViewModel", "Unexpected result when marking messages as read: $result")
            }
        }
    }
}

object WallpaperNotificationForChat {
    private const val PREF_NAME = "wallpaper_notifications"
    private const val NEW_WALLPAPER_RECEIVED_KEY = "new_wallpaper_received"

    fun setNewWallpaperReceived(value: Boolean) {
        val prefs = MyApp.appModule.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(NEW_WALLPAPER_RECEIVED_KEY, value).apply()
    }

    fun isNewWallpaperReceived(): Boolean {
        val prefs = MyApp.appModule.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(NEW_WALLPAPER_RECEIVED_KEY, false)
    }
}