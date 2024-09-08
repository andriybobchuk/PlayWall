package com.studios1299.playwall.create.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class CreateViewModel : ViewModel() {
    private val _state = MutableStateFlow(CreateScreenState())
    val state: StateFlow<CreateScreenState> = _state

    private val _events = Channel<CreateScreenEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateScreenAction) {
        when (action) {
            is CreateScreenAction.OnSelectImage -> {
               // _state.value = _state.value.copy(selectedImageUri = action.uri)
            }
            is CreateScreenAction.OnAddText -> {
                val updatedTexts = _state.value.addedTexts + TextItem(action.text, 12, Offset(100f, 100f))
                _state.value = _state.value.copy(addedTexts = updatedTexts)
            }
            is CreateScreenAction.OnAddSticker -> {
                val updatedStickers = _state.value.addedStickers + StickerItem(action.stickerUri, Offset(100f, 100f))
                _state.value = _state.value.copy(addedStickers = updatedStickers)
            }
            is CreateScreenAction.OnToggleDrawingMode -> {
                _state.value = _state.value.copy(isDrawingModeEnabled = action.enabled)
            }
            is CreateScreenAction.OnSaveImage -> {
                // Implement saving logic
            }
        }
    }
}
