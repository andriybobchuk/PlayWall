import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andriybobchuk.messenger.Constants.HORIZONTAL_SCREEN_PERCENTAGE
import com.andriybobchuk.messenger.Constants.MESSAGE_CORNER_RADIUS
import com.andriybobchuk.messenger.presentation.components.EmojiPanel
import com.andriybobchuk.messenger.presentation.formatStatus
import com.andriybobchuk.messenger.presentation.timestampAsTime
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.Reaction
import com.andriybobchuk.messenger.presentation.calculateImageDimensions
import com.andriybobchuk.messenger.presentation.components.EmojiBottomSheet
import com.andriybobchuk.messenger.presentation.components.FetchImageAspectRatio
import com.andriybobchuk.messenger.presentation.components.MessageReactionBox
import com.andriybobchuk.messenger.presentation.components.ReactionBottomSheet
import com.andriybobchuk.messenger.presentation.components.getMaxMessageDimensions
import com.andriybobchuk.messenger.presentation.overlays.image_detail.FullscreenImageViewer
import com.andriybobchuk.messenger.presentation.timestampAsDate
import com.andriybobchuk.messenger.presentation.triggerHapticFeedback
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel.Companion
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.ui.theme.LightBlue
import com.andriybobchuk.messenger.ui.theme.LightGrey
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import com.bumptech.glide.integration.compose.GlideImage
import kotlin.math.roundToInt

private const val LOG_TAG = "MessageItem"
const val ANIMATION_DURATION = 500
const val MIN_DRAG_AMOUNT = 6

/**
 * Main MessageItem function.
 * - Represents a single message item in the chat UI.
 * - Decides if this message belongs to us or the other person and applies appropriate parameters.
 * - Handles the display of the message content and applies swipe-to-dismiss(reply) behavior for
 * received messages.
 *
 * Splitting this functionality into different functions helps to maintain a clear separation of concerns,
 * making the code more modular and easier to maintain.
 *
 * @see SwipeToDismissBox - This is my swipe-to-reply functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(
    viewModel: ChatViewModel,
    uiState: MessengerUiState,
    message: Message,
    isLastMessage: Boolean,
    onSwipeComplete: () -> Unit
) {
    val currentUserId = uiState.currentUser!!.id
    if (message.senderId == currentUserId) {
        MessageContent(
            viewModel = viewModel,
            message = message,
            currentUserId = currentUserId,
            isLastMessage = isLastMessage,
            horizontalArrangement = Arrangement.End,
            onSwipeComplete = onSwipeComplete
        )
    } else {
        // Gesture detection for swipe-to-reply
        val offsetX = remember { mutableStateOf(0f) }
        val swipeThreshold = with(LocalDensity.current) { 20.dp.toPx() } // Smaller threshold
        val swipeSpeedFactor = 0.2f // Slower swipe speed
        MessageContent(
            modifier = swipeModifier(offsetX, swipeSpeedFactor, swipeThreshold, onSwipeComplete),
            viewModel = viewModel,
            message = message,
            currentUserId = currentUserId,
            isLastMessage = isLastMessage,
            horizontalArrangement = Arrangement.End,
            onSwipeComplete = onSwipeComplete
        )
    }

}

/**
 * Displays the main content of a chat message, including emoji panel, message bubble,
 * and reactions bottom sheet. This function focuses on structuring the message layout
 * and managing state for interactions like emoji reactions and bottom sheet visibility.
 *
 * This separation allows for focused management of message display logic while keeping the
 * swipe-to-dismiss(reply) and other behaviors modular.
 *
 * @see EmojiPanel
 * @see MessageBubble
 * @see ReactionBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageContent(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    isLastMessage: Boolean,
    horizontalArrangement: Arrangement.Horizontal,
    onSwipeComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val emojiSheetState = rememberModalBottomSheetState()
    val isSheetOpen = remember { mutableStateOf(false) }
    //val showEmojiPanel = remember { mutableStateOf(false) }
    val isEmojiSheetOpen = remember { mutableStateOf(false) }
    val isCurrentUser = message.senderId == currentUserId

    Column(
        modifier = modifier
    ) {
       // EmojiPanel(showEmojiPanel, viewModel, message, currentUserId, horizontalArrangement)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f) // Take up available space
            ) {
                MessageBubble(
                    viewModel = viewModel,
                    message = message,
                    currentUserId = currentUserId,
                    //showEmojiPanel = showEmojiPanel,
                    coroutineScope = coroutineScope,
                    //isSheetOpen = isSheetOpen,
                    //isSheetOpen = isSheetOpen,
                    //isSheetOpen = isSheetOpen,
                    //isSheetOpen = isSheetOpen,
                    //isSheetOpen = isSheetOpen,
                    isSheetOpen = isSheetOpen,
                    isEmojiSheetOpen = isEmojiSheetOpen,
                    horizontalArrangement = horizontalArrangement
                )
            }

            if (!isCurrentUser) {
                ReplyButton { onSwipeComplete() }
                Spacer(modifier = Modifier.weight(1 - HORIZONTAL_SCREEN_PERCENTAGE + 0.05f)) // Adjust space between bubble and button
            }
        }
        if (isLastMessage && isCurrentUser) {
            Text(
                text = formatStatus(message.status),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
            )
        }
    }

    EmojiBottomSheet(
        viewModel = viewModel,
        message = message,
        currentUserId = currentUserId,
        sheetState = emojiSheetState,
        isSheetOpen = isEmojiSheetOpen,
        coroutineScope = coroutineScope
    )

    ReactionBottomSheet(
        viewModel = viewModel,
        reactions = message.reactions,
        sheetState = sheetState,
        isSheetOpen = isSheetOpen,
        coroutineScope = coroutineScope
    )
}

@Composable
private fun swipeModifier(
    offsetX: MutableState<Float>,
    swipeSpeedFactor: Float,
    swipeThreshold: Float,
    onSwipeComplete: () -> Unit
) = Modifier
    .pointerInput(Unit) {
        detectHorizontalDragGestures(
            onHorizontalDrag = { change, dragAmount ->
                if (dragAmount > 0) { // Only allow right swipe
                    offsetX.value += dragAmount * swipeSpeedFactor
                }
                if (offsetX.value > swipeThreshold) {
                    onSwipeComplete()
                    offsetX.value = 0f
                }
                change.consume()
            },
            onDragEnd = {
                if (offsetX.value > swipeThreshold) {
                    onSwipeComplete()
                }
                offsetX.value = 0f // Reset offset to snap back the bubble
            },
            onDragCancel = {
                offsetX.value = 0f // Reset offset to snap back the bubble
            }
        )
    }
    .offset(x = offsetX.value.dp)

/**
 * Represents the visual bubble for a chat message, including the image, caption,
 * timestamp, and reaction options. Handles dynamic image dimension calculations and
 * gesture interactions for the message bubble.
 *
 * This separation helps manage the complexity of the message display and interactions
 * by focusing on the visual presentation and gestures within the bubble.
 *
 * @see ImageBox
 * @see CaptionAndTimestamp
 * @see MessageReactionBox
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    coroutineScope: CoroutineScope,
    isSheetOpen: MutableState<Boolean>,
    isEmojiSheetOpen: MutableState<Boolean>,
    horizontalArrangement: Arrangement.Horizontal,
) {
    val (maxWidth, maxHeight) = getMaxMessageDimensions()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var dimensionsLoaded by remember { mutableStateOf(false) }
    val isCurrentUser = message.senderId == currentUserId
    val (imageWidth, imageHeight) = if (dimensionsLoaded) {
        calculateImageDimensions(aspectRatio, maxWidth, maxHeight)
    } else {
        Pair(maxWidth, 170.dp)
    }
    FetchImageAspectRatio(message.imageUrl) { ratio ->
        aspectRatio = ratio
        dimensionsLoaded = true
        Log.d(LOG_TAG, "Fetched aspect ratio for image: $ratio")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = horizontalArrangement
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
        ) {
            Column(
                bubbleModifier(isCurrentUser)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            coroutineScope.launch {
                                isEmojiSheetOpen.value = true
                            }
                        }
                    )
            ) {
                ImageBox(
                    viewModel = viewModel,
                    message = message,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    onReactionClick = {
                        coroutineScope.launch {
                            isEmojiSheetOpen.value = true
                        }
                    }
                )

                CaptionAndTimestamp(
                    message = message,
                    imageWidth = imageWidth,
                    modifier = Modifier
                        .align(Alignment.End)
                )
            }
            MessageReactionBox(
                reactions = message.reactions,
                onReactionClick = {
                    coroutineScope.launch {
                        isSheetOpen.value = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-8).dp, y = 8.dp)
            )
        }
    }
}

@Composable
fun ReplyButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(LightGrey)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Send,
            contentDescription = "Reply",
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun bubbleModifier(isCurrentUser: Boolean) = Modifier
    .clip(
        RoundedCornerShape(
            if (isCurrentUser) MESSAGE_CORNER_RADIUS.dp else 4.dp,
            MESSAGE_CORNER_RADIUS.dp,
            if (isCurrentUser) 4.dp else MESSAGE_CORNER_RADIUS.dp,
            MESSAGE_CORNER_RADIUS.dp,
        )
    )
    .background(
        color = if (isCurrentUser) LightGrey else LightBlue,
    )
    .wrapContentWidth(align = Alignment.End)




/**
 * Displays an image within a chat message bubble, including the image's dimensions and gesture handling.
 * Shows a timestamp on the image if there is no caption.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageBox(
    viewModel: ChatViewModel,
    message: Message,
    imageWidth: Dp,
    imageHeight: Dp,
    onReactionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(imageWidth)
            .height(imageHeight)
            .combinedClickable(
                onClick = { viewModel.setSelectedMessage(message) },
                onLongClick = onReactionClick
            )
    ) {
        GlideImage(
            model = message.imageUrl,
            contentDescription = "Message Image",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            requestBuilderTransform = { requestBuilder ->
                requestBuilder.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(LOG_TAG, "Image load failed", e)
                        e?.logRootCauses(LOG_TAG)
                        e?.causes?.forEach { cause ->
                            Log.e(LOG_TAG, "Cause: ${cause.message}", cause)
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(LOG_TAG, "Image loaded successfully")
                        return false
                    }
                })
            }
        )
        if (message.caption.isEmpty()) {
            Text(
                text = timestampAsTime(message.timestamp),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(
                        Color.Black.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Displays the caption and timestamp for a chat message if a caption is present.
 * Arranges the caption and timestamp below the image.
 */
@Composable
fun CaptionAndTimestamp(
    message: Message,
    imageWidth: Dp,
    modifier: Modifier
) {
    if (message.caption.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.caption,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(4.dp)
                .widthIn(max = imageWidth - 4.dp)
                .wrapContentWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timestampAsTime(message.timestamp),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}