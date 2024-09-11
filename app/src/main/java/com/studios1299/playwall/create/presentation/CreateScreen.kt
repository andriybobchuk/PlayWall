package com.studios1299.playwall.create.presentation

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.TextFields
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
      //  onAction = { action -> viewModel.onAction(action) },
        bottomNavbar = bottomNavbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: CreateScreenState,
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
    var selectedColor by remember { mutableStateOf<Color?>(null) } // Start with no color selected
    var brushSize by remember { mutableStateOf<Float?>(null) } // Start with default brush size
    val isImageSelected = selectedImageUri != Uri.EMPTY
    var showReplacePhotoDialog by remember { mutableStateOf(false) }

    // Check if the selected image is a GIF
    fun isGif(uri: Uri): Boolean {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return mimeType == "image/gif"
    }

    fun resetPhotoEditor() {
        photoEditor?.clearAllViews()  // Clears all drawings, text, and stickers
    }

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        if (isGif(uri)) {
            Toast.makeText(context, "GIFs are not supported. Please select a static image.", Toast.LENGTH_SHORT).show()
        } else {
            if (isImageSelected) {
                pendingImageUri = uri
                showReplacePhotoDialog = true
            } else {
                resetPhotoEditor()  // Reset when selecting a new image
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
                        Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show()
                        selectedImageUri = savedUri
                    },
                    onFailure = {
                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
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

    if (showReplacePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showReplacePhotoDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    resetPhotoEditor()  // Clear the previous edits
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

    Scaffold(
        topBar = {
            Toolbars.Primary(
                title = "Create",
                actions = listOf(
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.Save,
                        contentDescription = "Save Image",
                        onClick = { requestSave() },
                        enabled = isImageSelected
                    ),
                    Toolbars.ToolBarAction(
                        icon = Icons.Outlined.Send,
                        contentDescription = "Send Image",
                        onClick = {
                            // ask viewmodel to send the image
                        },
                        enabled = isImageSelected
                    )
                ),
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            )
        },
        bottomBar = {
            Column {
                CustomBottomToolbar(
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

                Box(Modifier.fillMaxSize().padding(paddingValues)) {
                    Column(
                        modifier = Modifier
                            //.fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        Text(
                            text = "No Image",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Select an image to start editing!",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Button(
                            onClick = requestImagePicker,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Select Image")
                        }
                    }

                }
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
                initialColor = selectedColor, // Pass the previously selected color
                initialBrushSize = brushSize, // Pass the previously selected brush size
                onDismiss = { showDrawModeSheet = false },
                onDrawSettingsSelected = { color, size ->
                    // Update the state with selected color and brush size
                    selectedColor = color
                    brushSize = size
                    showDrawModeSheet = false // Close the bottom sheet
                    photoEditor?.setShape(ShapeBuilder()
                        .withShapeSize(size)
                        .withShapeColor(android.graphics.Color.parseColor("#" + Integer.toHexString(color.hashCode())))
                    )
                    photoEditor!!.setBrushDrawingMode(true)
                }
            )
        }

    }
}



@Composable
fun CustomBottomToolbar(
    onChooseImage: () -> Unit,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onDraw: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = onChooseImage
            ) {
                Icon(Icons.Outlined.Image, contentDescription = "Choose Image")
            }
            IconButton(
                onClick = onAddText,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.TextFields, contentDescription = "Add Text")
            }
            IconButton(
                onClick = onAddSticker,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.EmojiEmotions, contentDescription = "Add Sticker")
            }
            IconButton(
                onClick = onDraw,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.Brush, contentDescription = "Draw")
            }
            IconButton(
                onClick = onUndo,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.Undo, contentDescription = "Undo")
            }
            IconButton(
                onClick = onRedo,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.Redo, contentDescription = "Redo")
            }
        }
    }
}




private fun saveImageToGallery(
    context: Context,
    photoEditor: PhotoEditor?,
    onSuccess: (Uri) -> Unit,
    onFailure: () -> Unit,
) {
    val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/edited_image_${System.currentTimeMillis()}.png"

    photoEditor?.saveAsFile(filePath, object : PhotoEditor.OnSaveListener {
        override fun onSuccess(imagePath: String) {
            MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { path, uri ->
                Toast.makeText(context, "Image saved successfully: $path", Toast.LENGTH_SHORT).show()
                onSuccess(uri)
            }
        }
        override fun onFailure(exception: Exception) {
            Toast.makeText(context, "Failed to save image: ${exception.message}", Toast.LENGTH_SHORT).show()
            onFailure()
        }
    })
}






