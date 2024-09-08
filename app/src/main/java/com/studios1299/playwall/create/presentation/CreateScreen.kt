package com.studios1299.playwall.create.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.SaveFileResult
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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
        //bottomNavbar = bottomNavbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: CreateScreenState
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }

    var photoEditorView: PhotoEditorView? by remember { mutableStateOf(null) }
    var photoEditor: PhotoEditor? by remember { mutableStateOf(null) }

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        selectedImageUri = uri
    }
    val requestSave = {
        //photoEditor?.saveAsFile(Environment.DIRECTORY_PICTURES, )
        saveImageToGallery(selectedImageUri, {Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()}, context)
    }
    LaunchedEffect(selectedImageUri) {
        selectedImageUri.let { uri ->
            // Set the new image URI
            photoEditorView?.source?.setImageURI(uri)
        }
    }

    Scaffold(
        topBar = {
            Toolbars.Primary(
                title = "Create",
                actions = listOf(
                    Toolbars.ToolBarAction(
                        icon = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        onClick = { photoEditor?.undo() }
                    ),
                    Toolbars.ToolBarAction(
                        icon = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        onClick = { photoEditor?.redo() }
                    ),
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.Save,
                        contentDescription = "Save Image",
                        onClick = requestSave
                    )
                ),
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            )
        },
        bottomBar = {
            CustomBottomToolbar(
                onChooseImage = requestImagePicker,
                onAddText = {
                   // photoEditor?.addText("hey", Color.BLUE)
                    photoEditor?.addText("hey", TextStyleBuilder().apply {
                        withTextColor(Color.BLUE)
                        withTextSize(24f)
                    })
                },
                onAddSticker = {
                    //photoEditor?.addEmoji("\uD83D\uDE0A")
                    photoEditor?.addImage(BitmapFactory.decodeResource(context.resources, R.drawable.pw))
                },
                onDraw = {
                    photoEditorView?.let { editorView ->
                        photoEditor?.setBrushDrawingMode(true)
                        photoEditor?.setShape(ShapeBuilder()
                            .withShapeSize(10f)
                            .withShapeColor(Color.RED)
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            AndroidView(
                factory = { context ->
                    PhotoEditorView(context).apply {
                        photoEditorView = this
                        photoEditorView?.let {
                            photoEditor = PhotoEditor.Builder(context, this)
                                .setPinchTextScalable(true)
                                .build()
                            photoEditor!!.setBrushDrawingMode(true)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun CustomBottomToolbar(
    onChooseImage: () -> Unit,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onDraw: () -> Unit
) {
    BottomAppBar {
        IconButton(
            onClick = onChooseImage
        ) {
            Icon(Icons.Default.Image, contentDescription = "Choose Image")
        }
        IconButton(onClick = onAddText) {
            Icon(Icons.Default.TextFields, contentDescription = "Add Text")
        }
        IconButton(onClick = onAddSticker) {
            Icon(Icons.Default.StickyNote2, contentDescription = "Add Sticker")
        }
        IconButton(onClick = onDraw) {
            Icon(Icons.Default.Brush, contentDescription = "Draw")
        }
    }
}

@Composable
fun rememberRequestPermissionAndSaveImage(
    imageUri: Uri,
    onImageSaved: (Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current
    //val save = saveImageToGallery(imageUri, onImageSaved)

    return remember {
        {
            val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            val permissionStatus = ContextCompat.checkSelfPermission(context, permission)

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                saveImageToGallery(imageUri, onImageSaved, context)
            } else {
                Dexter.withContext(context)
                    .withPermission(permission)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            Log.d("LOG_TAG", "Permission granted")
                            saveImageToGallery(imageUri, onImageSaved, context)
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            Log.e("LOG_TAG", "Permission denied")
                            Toast.makeText(
                                context,
                                "context.getString(R.string.permission_denied_cannot_save_image)",
                                Toast.LENGTH_SHORT
                            ).show()
                            onImageSaved(false)
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: com.karumi.dexter.listener.PermissionRequest,
                            token: PermissionToken
                        ) {
                            Log.d("LOG_TAG", "Permission rationale should be shown")
                            token.continuePermissionRequest()
                        }
                    })
                    .check()
            }
        }
    }
}

private fun saveImageToGallery(imageUri: Uri, onImageSaved: (Boolean) -> Unit, context: Context) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let { outputUri ->
        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
            val inputStream = context.contentResolver.openInputStream(imageUri)
            inputStream?.use { input ->
                input.copyTo(outputStream)
                onImageSaved(true)
                Log.d("LOG_TAG", "Image saved successfully")
                return
            }
        }
    }
    onImageSaved(false)
}




//    private fun saveImageToGallery(context: Context, photoEditor: PhotoEditor) {
//        val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/edited_image.jpg"
//
//        // Use coroutines to save the image
//        CoroutineScope(Dispatchers.IO).launch {
//            val result = photoEditor.saveAsFile(filePath)
//            (context as Activity).runOnUiThread {
//                if (result is SaveFileResult.Success) {
//                    Toast.makeText(context, "Image saved to gallery!", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(context, "Couldn't save image", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }


//private fun saveImageToGallery(photoEditor: PhotoEditor?, context: Context) {
//    val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/edited_image_${System.currentTimeMillis()}.png"
//
//    photoEditor?.saveAsFile(filePath, object : PhotoEditor.OnSaveListener {
//        override fun onSuccess(@NonNull imagePath: String) {
//            // Add the saved image to gallery
//            MediaScannerConnection.scanFile(context, arrayOf(imagePath), null) { _, _ ->
//                Toast.makeText(context, "Image saved successfully to $imagePath", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        override fun onFailure(@NonNull exception: Exception) {
//            Toast.makeText(context, "Failed to save image: ${exception.message}", Toast.LENGTH_SHORT).show()
//        }
//    })
//}

