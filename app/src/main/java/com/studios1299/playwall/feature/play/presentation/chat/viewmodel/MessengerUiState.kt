package com.studios1299.playwall.feature.play.presentation.chat.viewmodel

import android.net.Uri
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.User

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
    val isOnline: Boolean = true,
    val goBack: Boolean = false,
)

data class PaginationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val page: Int = 0
)