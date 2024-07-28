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
    val pickedImageCaption: String = ""
)