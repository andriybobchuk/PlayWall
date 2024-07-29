package com.andriybobchuk.messenger.presentation.components

import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.andriybobchuk.messenger.Constants.HORIZONTAL_SCREEN_PERCENTAGE
import com.andriybobchuk.messenger.Constants.VERTICAL_SCREEN_PERCENTAGE
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.presentation.overlays.FullscreenImageViewer
import com.andriybobchuk.messenger.presentation.overlays.ImagePickerScreen
import com.andriybobchuk.messenger.presentation.timestampAsDate
import com.andriybobchuk.messenger.presentation.triggerHapticFeedback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener


private const val LOG_TAG = "ComposeUtil"

/**
 * This composable function handles requesting permission and launching the image picker.
 * It ensures that the READ_MEDIA_IMAGES permission is granted before opening the image picker.
 * If the permission is already granted, it directly launches the image picker.
 * If the permission is denied, it shows a rationale if necessary and continues the permission request.
 * If the permission is denied without rationale, it displays a Toast message to inform the user.
 *
 * @param context The context used for requesting permission and showing the Toast message.
 * @param onImagePicked Callback function that gets called with the selected image URI once the image is picked.
 * @return A lambda function that requests the necessary permission and launches the image picker.
 */
@Composable
fun rememberRequestPermissionAndPickImage(
    context: Context,
    onImagePicked: (Uri) -> Unit
): () -> Unit {
    val getContentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                onImagePicked(it)
            }
        }

    return remember {
        {
            val permission = Manifest.permission.READ_MEDIA_IMAGES
            val permissionStatus = ContextCompat.checkSelfPermission(context, permission)

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_TAG, "Permission already granted")
                getContentLauncher.launch("image/*")
            } else {
                Dexter.withContext(context)
                    .withPermission(permission)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            Log.d(LOG_TAG, "Permission granted")
                            getContentLauncher.launch("image/*")
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            Log.e(LOG_TAG, "Permission denied")
                            Toast.makeText(
                                context,
                                "Permission denied. Cannot pick images.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: com.karumi.dexter.listener.PermissionRequest,
                            token: PermissionToken
                        ) {
                            Log.d(LOG_TAG, "Permission rationale should be shown")
                            token.continuePermissionRequest()
                        }
                    })
                    .check()
            }
        }
    }
}

/**
 * Fetches the aspect ratio of an image from the provided URL and returns it through a callback.
 *
 * Hello, I need this function to know the original proportions of the image to then properly
 * display it on the messenger screen with the native proportions and not like idiotic squares.
 * I have suspicions that there is a better way to do it but I didnt find it yet.
 *
 * @param imageUrl The URL of the image.
 * @param onAspectRatioReady Callback function that is called with the aspect ratio when it is ready.
 */
@Composable
fun FetchImageAspectRatio(imageUrl: String, onAspectRatioReady: (Float) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(imageUrl) {
        Log.d(LOG_TAG, "Loading image from URL: $imageUrl")
        Glide.with(context)
            .load(imageUrl)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(LOG_TAG, "Image load failed: $e")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    val width = resource.intrinsicWidth
                    val height = resource.intrinsicHeight
                    val aspectRatio = width.toFloat() / height.toFloat()
                    Log.d(LOG_TAG, "Image loaded successfully. Aspect Ratio: $aspectRatio")
                    onAspectRatioReady(aspectRatio)
                    return false
                }
            })
            .submit()
    }
}

@Composable
fun showOverlays(uiState: MessengerUiState, viewModel: ChatViewModel): Boolean {
    if (uiState.fullscreenImageUrl != null) {
        FullscreenImageViewer(
            imageUrl = uiState.fullscreenImageUrl,
            caption = uiState.fullscreenCaption ?: "",
            sender = uiState.currentUser!!.name,
            dateTime = timestampAsDate(viewModel.getMessageById(uiState.currentMessageId)!!.timestamp),
            onDismiss = {
                viewModel.setFullscreenImage(null, null, "")
            },
            onDeleteClick = {
                viewModel.deleteMessage(uiState.currentMessageId)
                viewModel.setFullscreenImage(null, null, "")
            }
        )
        return true
    } else if (uiState.pickedImageUri != null) {
        ImagePickerScreen(
            imageUri = uiState.pickedImageUri,
            caption = uiState.pickedImageCaption,
            onSendClick = { uri, caption ->
                viewModel.sendImage(uri, caption)
                viewModel.setPickedImage(null)
            },
            onDismiss = {
                viewModel.setPickedImage(null)
            },
            onCaptionChange = { newCaption ->
                viewModel.updatePickedImageCaption(newCaption)
            }
        )
        return true
    }
    return false
}

/**
 * Displays the date header tag to separate messages grouped by date.
 */
@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
    }
}

/**
 * Calculates the maximum dimensions that a message can take up on the screen.
 */
@Composable
fun getMaxMessageDimensions(): Pair<Dp, Dp> {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val maxWidth = screenWidth * HORIZONTAL_SCREEN_PERCENTAGE
    val maxHeight = screenHeight * VERTICAL_SCREEN_PERCENTAGE
    return Pair(maxWidth, maxHeight)
}

/**
 * Creates a gesture modifier for a chat message, enabling interactions such as opening the
 * image in fullscreen on click and showing the emoji panel on long click.
 */
@OptIn(ExperimentalFoundationApi::class)
fun gestureModifier(
    viewModel: ChatViewModel,
    messageId: String,
    imageUrl: String,
    caption: String,
    context: Context,
    showEmojiPanel: MutableState<Boolean>,
): Modifier {
    return Modifier.combinedClickable(
        onClick = { viewModel.setFullscreenImage(imageUrl, caption, messageId) },
        onLongClick = {
            showEmojiPanel.value = !showEmojiPanel.value
            triggerHapticFeedback(context)
        },
        onLongClickLabel = "React!"
    )
}