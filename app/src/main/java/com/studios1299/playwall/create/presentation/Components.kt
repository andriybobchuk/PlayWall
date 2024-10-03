package com.studios1299.playwall.create.presentation

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
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
                icon = Icons.Default.Download,
                contentDescription = "Download image",
                onClick = { download() },
                enabled = isImageSelected
            ),
            Toolbars.ToolBarAction(
                icon = Icons.Outlined.Send,
                contentDescription = "Set as friend's wallpaper",
                onClick = send,
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