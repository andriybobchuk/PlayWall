package com.andriybobchuk.messenger.presentation.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.andriybobchuk.messenger.Constants.BACKGROUND_ALPHA
import com.andriybobchuk.messenger.Constants.BACKGROUND_COLOR
import com.andriybobchuk.messenger.Constants.DRAG_ALPHA_FACTOR
import com.andriybobchuk.messenger.Constants.DRAG_DISMISS_THRESHOLD
import com.andriybobchuk.messenger.Constants.TOP_BAR_BACKGROUND_COLOR
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * This composable provides an interactive fullscreen image view, allowing the user to drag the image
 * vertically to dismiss it. It includes a top app bar that shows the sender and timestamp of the image,
 * as well as an optional caption at the bottom. The top bar and caption visibility toggle based on user
 * interaction with the image.
 *
 * The image can be dragged to adjust its position and fade out the background, making it possible to
 * dismiss the viewer by dragging beyond a threshold.
 */
@Composable
fun FullscreenImageViewer(
    imageUrl: String,
    caption: String,
    sender: String,
    dateTime: String,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val topBarVisible = remember { mutableStateOf(true) }
    val alpha = remember { Animatable(BACKGROUND_ALPHA) }
    val dragAmount = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR.copy(alpha = alpha.value))
            .handleVerticalDrag(
                offsetY = offsetY,
                alpha = alpha,
                topBarVisible = topBarVisible,
                coroutineScope = coroutineScope,
                dragAmount = dragAmount,
                onDismiss = onDismiss
            )
    ) {
        FullscreenImageContent(
            imageUrl = imageUrl,
            offsetY = offsetY,
            onTap = { topBarVisible.value = !topBarVisible.value }
        )

        TopAppBarContent(
            visible = topBarVisible.value,
            sender = sender,
            dateTime = dateTime,
            onDismiss = onDismiss,
            onDeleteClick = onDeleteClick
        )

        CaptionContent(
            visible = topBarVisible.value,
            caption = caption,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        )
    }
}

/**
 * This modifier enables dragging functionality for the image. It adjusts the image's vertical position and
 * the alpha of the background based on drag gestures. When the drag exceeds a specified threshold, it triggers
 * the `onDismiss` callback to close the image viewer. The background alpha fades based on the drag amount.
 *
 * @param offsetY The vertical offset of the image, managed by an Animatable.
 * @param alpha The alpha value of the background, managed by an Animatable.
 * @param dragAmount Mutable state tracking the total amount of drag.
 * @return The Modifier configured to handle vertical drag gestures.
 */
private fun Modifier.handleVerticalDrag(
    offsetY: Animatable<Float, *>,
    alpha: Animatable<Float, *>,
    topBarVisible: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    dragAmount: MutableState<Float>,
    onDismiss: () -> Unit
): Modifier {
    return this.pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragStart = { topBarVisible.value = false },
            onDragEnd = {
                coroutineScope.launch {
                    offsetY.animateTo(0f)
                    alpha.animateTo(BACKGROUND_ALPHA)
                }
                topBarVisible.value = true
            }
        ) { _, dragAmountDelta ->
            dragAmount.value += dragAmountDelta
            coroutineScope.launch {
                offsetY.snapTo(offsetY.value + dragAmountDelta)
                val clampedAlpha =
                    (1 - abs(offsetY.value) / (DRAG_DISMISS_THRESHOLD * DRAG_ALPHA_FACTOR)).coerceIn(
                        0f,
                        BACKGROUND_ALPHA
                    )
                alpha.snapTo(clampedAlpha)
                if (abs(dragAmount.value) > DRAG_DISMISS_THRESHOLD) {
                    onDismiss()
                }
            }
        }
    }
}

/**
 * This composable renders the image in fullscreen mode, allowing it to be dragged and zoomed. The `offsetY` parameter
 * is used to apply vertical offsets based on user drag actions, while the `onTap` lambda toggles the visibility
 * of the top app bar when the image is tapped.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun FullscreenImageContent(
    imageUrl: String,
    offsetY: Animatable<Float, *>,
    onTap: () -> Unit
) {
    GlideImage(
        model = imageUrl,
        contentDescription = "Fullscreen Image",
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) },
        contentScale = ContentScale.Fit
    )
}

/**
 * Composable for displaying the top app bar with sender information and action buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarContent(
    visible: Boolean,
    sender: String,
    dateTime: String,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.fillMaxWidth()
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(text = sender, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.bodyMedium.copy(Color.Gray)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = TOP_BAR_BACKGROUND_COLOR,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

/**
 * Composable for displaying the caption at the bottom of the fullscreen image.
 */
@Composable
private fun CaptionContent(
    visible: Boolean,
    caption: String,
    modifier: Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(TOP_BAR_BACKGROUND_COLOR)
                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp, top = 16.dp)
        ) {
            Text(
                text = caption,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}