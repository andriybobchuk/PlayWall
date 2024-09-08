package com.studios1299.playwall.create.presentation

import android.net.Uri
import androidx.compose.ui.graphics.Color

sealed interface CreateScreenAction {
    data class OnSelectImage(val uri: Uri) : CreateScreenAction
    data class OnAddText(val text: String, val color: Color) : CreateScreenAction
    data class OnAddSticker(val stickerUri: Uri) : CreateScreenAction
    data class OnToggleDrawingMode(val enabled: Boolean) : CreateScreenAction
    object OnSaveImage : CreateScreenAction
}
