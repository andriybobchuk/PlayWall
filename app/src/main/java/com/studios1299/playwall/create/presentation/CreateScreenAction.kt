package com.studios1299.playwall.create.presentation

import android.net.Uri
import androidx.compose.ui.graphics.Color

sealed class CreateScreenAction {
    data class SelectImage(val uri: Uri) : CreateScreenAction()
    object ConfirmReplaceImage : CreateScreenAction()
    object CancelReplaceImage : CreateScreenAction()
    object ToggleAddTextSheet : CreateScreenAction()
    object ToggleStickerSheet : CreateScreenAction()
    object ToggleDrawModeSheet : CreateScreenAction()
    data class SetPendingImage(val uri: Uri) : CreateScreenAction()
    object ToggleReplacePhotoDialog : CreateScreenAction()
    data class SetDrawSettings(val color: Color?, val brushSize: Float?) : CreateScreenAction()
}

