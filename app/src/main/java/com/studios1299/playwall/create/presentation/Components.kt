package com.studios1299.playwall.create.presentation

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aws.sdk.kotlin.services.s3.model.LoggingEnabled
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.explore.presentation.explore.ExploreAction
import com.studios1299.playwall.monetization.presentation.AppState
import com.studios1299.playwall.monetization.presentation.components.DiamondsDisplay
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
                text = stringResource(R.string.no_image),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.select_an_image_to_start_editing),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Button(
                onClick = requestImagePicker,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.select_image))
            }
        }

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Topbar(
    download: () -> Unit,
    send: () -> Unit,
    goToDiamonds: () -> Unit,
    sendEnabled: Boolean,
    setAsMyWallpaper: () -> Unit,
    isImageSelected: Boolean
) {
    val isPremium = AppState.isPremium.collectAsState().value
    Toolbars.WithMenu(
        title = stringResource(R.string.create),
        actions = listOf(
            Toolbars.ToolBarAction(
                icon = Icons.Outlined.Send,
                contentDescription = stringResource(R.string.send_to_friend),
                onClick = send,
                enabled = isImageSelected && sendEnabled
            ),
            Toolbars.ToolBarAction(
                icon = Icons.Default.Download,
                contentDescription = stringResource(R.string.download_image),
                onClick = { download() },
                enabled = isImageSelected
            )
            ,
            Toolbars.ToolBarAction(
                icon = Icons.Outlined.Wallpaper,
                contentDescription = stringResource(R.string.set_as_my_wallpaper),
                onClick = setAsMyWallpaper,
                enabled = isImageSelected
            )
        ),
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        customContent = {
            DiamondsDisplay(
                diamondsCount = AppState.devilCount.collectAsState().value,
                isPremium = isPremium,
                onClick = {
                    if (!isPremium) goToDiamonds()
                }
            )
        }
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

                Toast.makeText(context,
                    context.getString(R.string.image_saved_successfully), Toast.LENGTH_SHORT).show()
                onSuccess(imageUri)
            }

            override fun onFailure(exception: Exception) {
                Toast.makeText(context,
                    context.getString(R.string.failed_to_save_image, exception.message), Toast.LENGTH_SHORT).show()
                onFailure()
            }
        })
    } else {
        Toast.makeText(context,
            context.getString(R.string.failed_to_create_new_mediastore_record), Toast.LENGTH_SHORT).show()
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
                Log.e("CreateScreen", "saveImageForOlderVersions(): Image saved successfully")
                onSuccess(uri)
            }
        }

        override fun onFailure(exception: Exception) {
            Log.e("CreateScreen", "saveImageForOlderVersions(): Failed to save image: ${exception.message}")
            onFailure()
        }
    })
}
