package com.studios1299.playwall.create.presentation


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.play.presentation.play.Friend
import com.studios1299.playwall.play.presentation.play.WallpaperNotificationForPlay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages = _errorMessages.asSharedFlow()
    fun sendErrorMessage(message: String) {
        viewModelScope.launch {
            _errorMessages.emit(message)
        }
    }

    private val _state = MutableStateFlow(CreateScreenState())
    val state: StateFlow<CreateScreenState> = _state.asStateFlow()

    private val _events = Channel<CreateScreenEvent>()
    val events = _events.receiveAsFlow()

    init {


        // TODO
        viewModelScope.launch {
            repository.debugClearCheckInShit()
        }











        loadFriends()
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                _state.value = _state.value.copy(isOnline = online)
            }
        }
    }

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
            is CreateScreenAction.SendToFriends -> {
                sendWallpaperToFriends(action.selectedFriends, action.filename, action.context)
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

    fun loadFriends() {
        viewModelScope.launch {
            val result = repository.getFriends(true)
            if (result is SmartResult.Success) {
                _state.update { currentState ->
                    currentState.copy(friends = result.data!!)
                }
            }
        }
    }

    fun sendWallpaperToFriends(friends: List<Friend>, uri: Uri, context: Context) {
        viewModelScope.launch {
            val pathTobeSent = S3Handler.uploadToS3(uriToFile(context, uri)!!, S3Handler.Folder.WALLPAPERS)?:""

            friends.forEach { friend ->
                val result = repository.changeWallpaper(
                    ChangeWallpaperRequest(
                        fileName = pathTobeSent,
                        recipientId = friend.id.toString(),
                        comment = null,
                        reaction = null,
                        type = "friend_wallpaper"
                    )
                )
                if (result is SmartResult.Success) {
                    Log.e("sendWallpaperToFriends", "Wallpapers sent to ${friends.size} friends!!")
                    sendErrorMessage("Wallpaper sent!")
                } else if (result is SmartResult.Error) {
                    sendErrorMessage(result.errorBody?:"Wallpaper could not be sent")
                }
            }
            WallpaperNotificationForPlay.setNewWallpaperReceived(true)
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