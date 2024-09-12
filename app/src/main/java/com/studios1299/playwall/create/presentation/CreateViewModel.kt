package com.studios1299.playwall.create.presentation


import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class CreateViewModel : ViewModel() {

    private val _state = MutableStateFlow(CreateScreenState())
    val state: StateFlow<CreateScreenState> = _state.asStateFlow()

    private val _events = Channel<CreateScreenEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateScreenAction) {
        when (action) {
            is CreateScreenAction.SelectImage -> selectImage(action.uri)
            is CreateScreenAction.ConfirmReplaceImage -> confirmReplaceImage()
            is CreateScreenAction.CancelReplaceImage -> cancelReplaceImage()
            is CreateScreenAction.ToggleAddTextSheet -> toggleAddTextSheet()
            is CreateScreenAction.ToggleStickerSheet -> toggleStickerSheet()
            is CreateScreenAction.ToggleDrawModeSheet -> toggleDrawModeSheet()
            is CreateScreenAction.SetDrawSettings -> setDrawSettings(action.color, action.brushSize)
            is CreateScreenAction.SetPendingImage -> {
                _state.value = _state.value.copy(
                    pendingImageUri = action.uri
                )
            }
            CreateScreenAction.ToggleReplacePhotoDialog -> {
                _state.value = _state.value.copy(
                    showReplacePhotoDialog = !_state.value.showReplacePhotoDialog
                )
            }
        }
    }

    private fun selectImage(uri: Uri) {
        if (_state.value.isImageSelected) {
            _state.update {
                it.copy(pendingImageUri = uri, showReplacePhotoDialog = true)
            }
        } else {
            _state.update {
                it.copy(selectedImageUri = uri)
            }
        }
    }

    private fun confirmReplaceImage() {
        _state.update { currentState ->
            currentState.pendingImageUri?.let { uri ->
                currentState.copy(
                    selectedImageUri = uri,
                    pendingImageUri = null,
                    showReplacePhotoDialog = false
                )
            } ?: currentState.copy(showReplacePhotoDialog = false)
        }
    }

    private fun cancelReplaceImage() {
        _state.update { it.copy(pendingImageUri = null, showReplacePhotoDialog = false) }
    }

    private fun toggleAddTextSheet() {
        _state.update { it.copy(showAddTextSheet = !it.showAddTextSheet) }
    }

    private fun toggleStickerSheet() {
        _state.update { it.copy(showStickerSheet = !it.showStickerSheet) }
    }

    private fun toggleDrawModeSheet() {
        _state.update { it.copy(showDrawModeSheet = !it.showDrawModeSheet) }
    }

    private fun setDrawSettings(color: Color?, brushSize: Float?) {
        _state.update { it.copy(selectedColor = color, brushSize = brushSize) }
    }
}