package com.studios1299.playwall.create.presentation

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.studios1299.playwall.core.data.ChangeWallpaperWorker
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun CreateScreenRoot(
    viewModel: CreateViewModel,
    bottomNavbar: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CreateScreenEvent.ImageSaved -> {
                Toast.makeText(context, "Image saved successfully", Toast.LENGTH_LONG).show()
            }
            is CreateScreenEvent.ShowError -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    CreateScreen(
        state = state,
        onAction = { action -> viewModel.onAction(action) },
        bottomNavbar = bottomNavbar
    )
}

@Composable
fun CreateScreen(
    state: CreateScreenState,
    onAction: (CreateScreenAction) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoEditorView: PhotoEditorView? by remember { mutableStateOf(null) }
    var photoEditor: PhotoEditor? by remember { mutableStateOf(null) }
    var showAddTextSheet by remember { mutableStateOf(false) }
    var previousTextColor: Color? = null
    var showStickerSheet by remember { mutableStateOf(false) }
    var showDrawModeSheet by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var brushSize by remember { mutableStateOf<Float?>(null) }
    val isImageSelected = selectedImageUri != Uri.EMPTY
    var showReplacePhotoDialog by remember { mutableStateOf(false) }

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        if (isGif(uri, context)) {
            Toast.makeText(context, "GIFs are not supported.", Toast.LENGTH_SHORT).show()
        } else {
            if (isImageSelected) {
                pendingImageUri = uri
                showReplacePhotoDialog = true
            } else {
                resetPhotoEditor(photoEditor)
                selectedImageUri = uri
            }
        }
    }

    val requestSave = {
        CoroutineScope(Dispatchers.Main).launch {
            if (photoEditor != null && photoEditorView != null) {
                saveImageToGallery(
                    context = context,
                    photoEditor = photoEditor,
                    onSuccess = { savedUri ->
                        selectedImageUri = savedUri
                    }
                )
            } else {
                Toast.makeText(context, "Photo editor is not initialized", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != Uri.EMPTY) {
            try {
                photoEditorView?.source?.setImageURI(selectedImageUri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("CreateScreen", "Error loading image", e)
            }
        }
    }

    val workManager = WorkManager.getInstance(context)
    val inputData = Data.Builder()
        .putString("file_name", "your_image_name") // You can use this for reference but won't need it in this version
        .putBoolean("set_home_screen", true) // or false based on user preference
        .putBoolean("set_lock_screen", true) // or false based on user preference
        .build()

    val changeWallpaperRequest = OneTimeWorkRequestBuilder<ChangeWallpaperWorker>()
        .setInputData(inputData)
        .build()

    Scaffold(
        topBar = {
            Topbar(
                requestSave = { requestSave() },
                send = { workManager.enqueue(changeWallpaperRequest) },
                isImageSelected = isImageSelected
            )
        },
        bottomBar = {
            Column {
                DrawingToolbar(
                    onChooseImage = requestImagePicker,
                    onAddText = { showAddTextSheet = true },
                    onAddSticker = { showStickerSheet = true },
                    onDraw = { showDrawModeSheet = true },
                    onUndo = { photoEditor?.undo() },
                    onRedo = { photoEditor?.redo() },
                    enabled = isImageSelected
                )
                bottomNavbar()
            }
        }
    ) { paddingValues ->
        if (!isImageSelected) {
            NoImagePlaceholder(paddingValues, requestImagePicker)
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                AndroidView(
                    factory = { context ->
                        PhotoEditorView(context).apply {
                            photoEditorView = this
                            selectedImageUri.let { uri ->
                                source.setImageURI(uri)
                            }
                            photoEditor = PhotoEditor.Builder(context, this)
                                .setPinchTextScalable(true)
                                .build()
                            photoEditor!!.setBrushDrawingMode(true)
                            source.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (showAddTextSheet) {
            AddTextBottomSheet(
                onDismiss = { showAddTextSheet = false },
                onTextAdded = { inputText, textColor ->
                    photoEditor?.addText(inputText, TextStyleBuilder().apply {
                        withTextColor(textColor)
                        withTextSize(24f)
                    })
                    previousTextColor = Color(textColor)
                },
                initialColor = previousTextColor
            )
        }

        if (showStickerSheet) {
            StickerBottomSheet(
                onDismiss = { showStickerSheet = false },
                onStickerSelected = { stickerResourceId ->
                    val bitmap = BitmapFactory.decodeResource(context.resources, stickerResourceId)
                    photoEditor?.addImage(bitmap)
                }
            )
        }

        if (showDrawModeSheet) {
            DrawModeBottomSheet(
                initialColor = selectedColor,
                initialBrushSize = brushSize,
                onDismiss = { showDrawModeSheet = false },
                onDrawSettingsSelected = { color, size ->
                    selectedColor = color
                    brushSize = size
                    showDrawModeSheet = false
                    photoEditor?.setShape(ShapeBuilder()
                        .withShapeSize(size)
                        .withShapeColor(android.graphics.Color.parseColor("#" + Integer.toHexString(color.hashCode())))
                    )
                    photoEditor!!.setBrushDrawingMode(true)
                }
            )
        }

        if (showReplacePhotoDialog) {
            AlertDialog(
                onDismissRequest = { showReplacePhotoDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        resetPhotoEditor(photoEditor)
                        selectedImageUri = pendingImageUri ?: Uri.EMPTY
                        pendingImageUri = null
                        showReplacePhotoDialog = false
                    }) {
                        Text("Replace")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pendingImageUri = null
                        showReplacePhotoDialog = false
                    }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Replace Image?") },
                text = { Text("You will lose your progress if you haven't saved. Do you want to replace the image?") }
            )
        }
    }
}

//@Composable
//fun CreateScreen(
//    state: CreateScreenState,
//    onAction: (CreateScreenAction) -> Unit,
//    bottomNavbar: @Composable () -> Unit
//) {
//    val context = LocalContext.current
//    var photoEditorView: PhotoEditorView? by remember { mutableStateOf(null) }
//    var photoEditor: PhotoEditor? by remember { mutableStateOf(null) }
//    var previousTextColor: Color? = null
//
//    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
//        if (isGif(uri, context)) {
//            Toast.makeText(context, "GIFs are not supported.", Toast.LENGTH_SHORT).show()
//        } else {
//            if (state.isImageSelected) {
//                onAction(CreateScreenAction.SetPendingImage(uri))
//                onAction(CreateScreenAction.ToggleReplacePhotoDialog)
//            } else {
//                resetPhotoEditor(photoEditor)
//                onAction(CreateScreenAction.SelectImage(uri))
//            }
//        }
//    }
//
////    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
////        if (isGif(uri, context)) {
////            Toast.makeText(context, "GIFs are not supported.", Toast.LENGTH_SHORT).show()
////        } else {
////            if (isImageSelected) {
////                pendingImageUri = uri
////                showReplacePhotoDialog = true
////            } else {
////                resetPhotoEditor(photoEditor)
////                selectedImageUri = uri
////            }
////        }
////    }
//
//    LaunchedEffect(state.selectedImageUri) {
//        if (state.selectedImageUri != Uri.EMPTY) {
//            try {
//                photoEditorView?.source?.setImageURI(state.selectedImageUri)
//            } catch (e: Exception) {
//                Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
//                Log.e("CreateScreen", "Error loading image", e)
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            Topbar(
//                requestSave = {  },
//                isImageSelected = state.isImageSelected
//            )
//        },
//        bottomBar = {
//            Column {
//                DrawingToolbar(
//                    onChooseImage = requestImagePicker,
//                    onAddText = { onAction(CreateScreenAction.ToggleAddTextSheet) },
//                    onAddSticker = { onAction(CreateScreenAction.ToggleStickerSheet) },
//                    onDraw = { onAction(CreateScreenAction.ToggleDrawModeSheet) },
//                    onUndo = { photoEditor?.undo() },
//                    onRedo = { photoEditor?.redo() },
//                    enabled = state.isImageSelected
//                )
//                bottomNavbar()
//            }
//        }
//    ) { paddingValues ->
//        if (!state.isImageSelected) {
//            NoImagePlaceholder(paddingValues) {
//                requestImagePicker()
//            }
//        } else {
//            Column(
//                modifier = Modifier
//                    .padding(paddingValues)
//                    .fillMaxSize()
//            ) {
//                AndroidView(
////                    factory = { context ->
////                        PhotoEditorView(context).apply {
////                            photoEditorView = this
////                            source.setImageURI(state.selectedImageUri)
////                            photoEditor = PhotoEditor.Builder(context, this)
////                                .setPinchTextScalable(true)
////                                .build()
////                            photoEditor!!.setBrushDrawingMode(true)
////                            source.scaleType = ImageView.ScaleType.CENTER_CROP
////                        }
////                    },
//                    factory = { context ->
//                        PhotoEditorView(context).apply {
//                            photoEditorView = this
//                            state.selectedImageUri.let { uri ->
//                                source.setImageURI(uri)
//                            }
//                            photoEditor = PhotoEditor.Builder(context, this)
//                                .setPinchTextScalable(true)
//                                .build()
//                            photoEditor!!.setBrushDrawingMode(true)
//                            source.scaleType = ImageView.ScaleType.CENTER_CROP
//                        }
//                    },
//                    modifier = Modifier.fillMaxSize()
//                )
//            }
//        }
//
//        if (state.showAddTextSheet) {
//            AddTextBottomSheet(
//                onDismiss = { onAction(CreateScreenAction.ToggleAddTextSheet) },
//                onTextAdded = { inputText, textColor ->
//                    photoEditor?.addText(inputText, TextStyleBuilder().apply {
//                        withTextColor(textColor)
//                        withTextSize(24f)
//                    })
//                    previousTextColor = Color(textColor)
//                },
//                initialColor = previousTextColor
//            )
//        }
//
//        if (state.showStickerSheet) {
//            StickerBottomSheet(
//                onDismiss = { onAction(CreateScreenAction.ToggleStickerSheet) },
//                onStickerSelected = { stickerResourceId ->
//                    val bitmap = BitmapFactory.decodeResource(context.resources, stickerResourceId)
//                    photoEditor?.addImage(bitmap)
//                }
//            )
//        }
//
//        if (state.showDrawModeSheet) {
//            DrawModeBottomSheet(
//                initialColor = state.selectedColor,
//                initialBrushSize = state.brushSize,
//                onDismiss = { onAction(CreateScreenAction.ToggleDrawModeSheet) },
//                onDrawSettingsSelected = { color, size ->
//                    onAction(CreateScreenAction.SetDrawSettings(color, size))
//                    photoEditor?.setShape(ShapeBuilder()
//                        .withShapeSize(size)
//                        .withShapeColor(android.graphics.Color.parseColor("#" + Integer.toHexString(color.hashCode())))
//                    )
//                    photoEditor!!.setBrushDrawingMode(true)
//                }
//            )
//        }
//
//        if (state.showReplacePhotoDialog) {
//            AlertDialog(
//                onDismissRequest = { onAction(CreateScreenAction.CancelReplaceImage) },
//                confirmButton = {
//                    TextButton(onClick = {
//                        onAction(CreateScreenAction.ConfirmReplaceImage)
//                    }) {
//                        Text("Replace")
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = {
//                        onAction(CreateScreenAction.CancelReplaceImage)
//                    }) {
//                        Text("Cancel")
//                    }
//                },
//                title = { Text("Replace Image?") },
//                text = { Text("You will lose your progress if you haven't saved. Do you want to replace the image?") }
//            )
//        }
//    }
//}






