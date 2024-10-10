package com.studios1299.playwall.create.presentation

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studios1299.playwall.core.presentation.components.Toolbars
import ja.burhanrashid52.photoeditor.PhotoEditor
import java.io.File
import java.io.OutputStream

@Composable
fun NoImagePlaceholder(
    paddingValues: PaddingValues,
    requestImagePicker: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
        Column(
            modifier = Modifier
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
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Topbar(
    download: () -> Unit,
    send: () -> Unit,
    setAsMyWallpaper: () -> Unit,
    isImageSelected: Boolean
) {
    Toolbars.WithMenu(
        title = "Create",
        actions = listOf(
            Toolbars.ToolBarAction(
                icon = Icons.Outlined.Send,
                contentDescription = "Send to friend",
                onClick = send,
                enabled = isImageSelected
            ),
            Toolbars.ToolBarAction(
                icon = Icons.Default.Download,
                contentDescription = "Download image",
                onClick = { download() },
                enabled = isImageSelected
            )
            ,
            Toolbars.ToolBarAction(
                icon = Icons.Outlined.Wallpaper,
                contentDescription = "Set as my wallpaper",
                onClick = setAsMyWallpaper,
                enabled = isImageSelected
            )
        ),
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    )
}

fun isGif(uri: Uri, context: Context): Boolean {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri)
    return mimeType == "image/gif"
}

fun resetPhotoEditor(photoEditor: PhotoEditor?) {
    photoEditor?.clearAllViews()
}


fun saveImageToGallery(
    context: Context,
    photoEditor: PhotoEditor?,
    onSuccess: (Uri) -> Unit,
    onFailure: () -> Unit = {}
) {
    val fileName = "edited_image_${System.currentTimeMillis()}.png"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 (API 29) and above: Use MediaStore to save image
        saveImageForNewerVersions(context, photoEditor, fileName, onSuccess, onFailure)
    } else {
        // For older Android versions: Use the external storage directory
        saveImageForOlderVersions(context, photoEditor, fileName, onSuccess, onFailure)
    }
}

private fun saveImageForNewerVersions(
    context: Context,
    photoEditor: PhotoEditor?,
    fileName: String,
    onSuccess: (Uri) -> Unit,
    onFailure: () -> Unit
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // Saves in Pictures directory
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val contentResolver = context.contentResolver
    val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (imageUri != null) {
        val outputStream: OutputStream? = contentResolver.openOutputStream(imageUri)

        // Save the image using PhotoEditor
        val tempFile = File.createTempFile("temp_", ".png", context.cacheDir)
        photoEditor?.saveAsFile(tempFile.absolutePath, object : PhotoEditor.OnSaveListener {
            override fun onSuccess(imagePath: String) {
                // Copy the file to the gallery
                tempFile.inputStream().use { input ->
                    outputStream?.use { output ->
                        input.copyTo(output)
                    }
                }
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null, null)

                Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show()
                onSuccess(imageUri)
            }

            override fun onFailure(exception: Exception) {
                Toast.makeText(context, "Failed to save image: ${exception.message}", Toast.LENGTH_SHORT).show()
                onFailure()
            }
        })
    } else {
        Toast.makeText(context, "Failed to create new MediaStore record", Toast.LENGTH_SHORT).show()
        onFailure()
    }
}

private fun saveImageForOlderVersions(
    context: Context,
    photoEditor: PhotoEditor?,
    fileName: String,
    onSuccess: (Uri) -> Unit,
    onFailure: () -> Unit
) {
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val filePath = "${picturesDir.absolutePath}/$fileName"

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

//fun saveImageToGallery(
//    context: Context,
//    photoEditor: PhotoEditor?,
//    onSuccess: (Uri) -> Unit,
//    onFailure: () -> Unit = {}
//) {
//    val filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/edited_image_${System.currentTimeMillis()}.png"
//
//    photoEditor?.saveAsFile(filePath, object : PhotoEditor.OnSaveListener {
//        override fun onSuccess(imagePath: String) {
//            MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { path, uri ->
//                Toast.makeText(context, "Image saved successfully: $path", Toast.LENGTH_SHORT).show()
//                onSuccess(uri)
//            }
//        }
//        override fun onFailure(exception: Exception) {
//            Toast.makeText(context, "Failed to save image: ${exception.message}", Toast.LENGTH_SHORT).show()
//            onFailure()
//        }
//    })
//}