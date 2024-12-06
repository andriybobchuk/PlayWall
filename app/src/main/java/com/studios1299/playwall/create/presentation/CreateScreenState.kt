package com.studios1299.playwall.create.presentation

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.presentation.play.Friend
import com.studios1299.playwall.play.presentation.play.FriendshipStatus

data class CreateScreenState(
    val selectedImageUri: Uri = Uri.EMPTY,
    val imageString: String = "",
    val pendingImageUri: Uri? = null,
    val photoEditorInitialized: Boolean = false,
    val showAddTextSheet: Boolean = false,
    val showStickerSheet: Boolean = false,
    val showDrawModeSheet: Boolean = false,
    val selectedColor: Color? = null,
    val brushSize: Float? = null,
    val isImageSelected: Boolean = selectedImageUri != Uri.EMPTY,
    val showReplacePhotoDialog: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val selectedFriend: Friend = Friend(-2, -2, "", "", "", FriendshipStatus.pending, -3, "", MessageStatus.read, -3, 2.1f),
    val isOnline: Boolean = true
)

