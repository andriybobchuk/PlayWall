import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andriybobchuk.messenger.util.Constants.HORIZONTAL_SCREEN_PERCENTAGE
import com.andriybobchuk.messenger.util.Constants.MESSAGE_CORNER_RADIUS
import com.andriybobchuk.messenger.util.formatStatus
import com.andriybobchuk.messenger.util.timestampAsTime
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.util.calculateImageDimensions
import com.andriybobchuk.messenger.presentation.components.ReactSheet
import com.andriybobchuk.messenger.presentation.components.FetchImageAspectRatio
import com.andriybobchuk.messenger.presentation.components.MessageReactionIndicator
import com.andriybobchuk.messenger.presentation.components.Reactions
import com.andriybobchuk.messenger.presentation.components.getMaxMessageDimensions
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

private const val LOG_TAG = "MessageItem"

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
    isLastMessage: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val reactSheet = rememberModalBottomSheetState()
    val isReactSheetOpen = remember { mutableStateOf(false) }
    val reactionsSheet = rememberModalBottomSheetState()
    val isReactionsSheetOpen = remember { mutableStateOf(false) }
    val onReact = { isReactSheetOpen.value = true }
    val onCheckReactions = { isReactionsSheetOpen.value = true }

    val currentUserId = uiState.currentUser!!.id
    if (message.senderId == currentUserId) {
        MessageContent(
            viewModel = viewModel,
            message = message,
            currentUserId = currentUserId,
            isLastMessage = isLastMessage,
            horizontalArrangement = Arrangement.End,
            onReact = onReact,
            onCheckReactions = onCheckReactions
        )
    } else {
        MessageContent(
            modifier = swipeModifier(onReact),
            viewModel = viewModel,
            message = message,
            currentUserId = currentUserId,
            isLastMessage = isLastMessage,
            horizontalArrangement = Arrangement.End,
            onReact = onReact,
            onCheckReactions = onCheckReactions
        )
    }
    ReactSheet(
        viewModel = viewModel,
        message = message,
        currentUserId = currentUserId,
        sheetState = reactSheet,
        isSheetOpen = isReactSheetOpen,
        coroutineScope = coroutineScope
    )
    Reactions(
        viewModel = viewModel,
        reactions = message.reactions,
        sheetState = reactionsSheet,
        isSheetOpen = isReactionsSheetOpen,
        coroutineScope = coroutineScope
    )
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
 * @see Reactions
 */
@Composable
fun MessageContent(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    isLastMessage: Boolean,
    horizontalArrangement: Arrangement.Horizontal,
    onReact: () -> Unit,
    onCheckReactions: () -> Unit
) {
    val isCurrentUser = message.senderId == currentUserId
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
            ) {
                MessageBubble(
                    viewModel = viewModel,
                    message = message,
                    currentUserId = currentUserId,
                    onReact = onReact,
                    onCheckReactions = onCheckReactions,
                    horizontalArrangement = horizontalArrangement
                )
            }
            if (!isCurrentUser) {
                ReplyButton { onReact() }
                Spacer(modifier = Modifier.weight(1 - HORIZONTAL_SCREEN_PERCENTAGE + 0.05f)) // Adjust space between bubble and button
            }
        }
        if (isLastMessage && isCurrentUser) {
            Text(
                text = formatStatus(message.status),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
private fun swipeModifier(
    onSwipeComplete: () -> Unit
): Modifier {
    val offsetX = remember { mutableStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { 20.dp.toPx() }
    val swipeSpeedFactor = 0.2f

    return Modifier
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onHorizontalDrag = { change, dragAmount ->
                    if (dragAmount > 0) { // Only allow right swipe
                        offsetX.value += dragAmount * swipeSpeedFactor
                    }
                    if (offsetX.value > swipeThreshold) {
                        onSwipeComplete()
                        Log.e(LOG_TAG, "onSwipeComplete - Swipe Threshold Passed")
                        offsetX.value = 0f
                    }
                    change.consume()
                },
                onDragEnd = {
                    if (offsetX.value > swipeThreshold) {
                        onSwipeComplete()
                        Log.e(LOG_TAG, "onSwipeComplete - Drag End")
                    }
                    offsetX.value = 0f // Reset offset to snap back the bubble
                },
                onDragCancel = {
                    offsetX.value = 0f // Reset offset to snap back the bubble
                }
            )
        }
        .offset(x = offsetX.value.dp)
}



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
 * @see MessageReactionIndicator
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    onReact: () -> Unit,
    onCheckReactions: () -> Unit,
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
                        onLongClick = { onReact() }
                    )
            ) {
                ImageBox(
                    viewModel = viewModel,
                    message = message,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    onReactionClick = { onReact() }
                )
                CaptionAndTimestamp(
                    message = message,
                    imageWidth = imageWidth,
                    modifier = Modifier
                        .align(Alignment.End)
                )
            }
            MessageReactionIndicator(
                reactions = message.reactions,
                onReactionClick = { onCheckReactions() },
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
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Send,
            contentDescription = "Reply",
            tint = MaterialTheme.colorScheme.primary,
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
        color = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
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
                .background(MaterialTheme.colorScheme.outline),
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
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timestampAsTime(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}