package com.andriybobchuk.messenger.presentation.viewmodel

import android.net.Uri
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.User

data class MessengerUiState(
    val currentUser: User? = null,
    val recipient: User? = null,
    val messages: List<Message> = emptyList(),
    val fullscreenImageUrl: String? = null,
    val fullscreenCaption: String? = null,
    val pickedImageUri: Uri? = null,
    val pickedImageCaption: String = "",
    val currentMessageId: String = "",
    val selectedMessage: Message? = null,
    val replyingToMessage: Message? = null
)

data class PaginationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val page: Int = 0
)