import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
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
    isLastMessage: Boolean,
    onMessageSwipe: (Message) -> Unit,
    onSwipeComplete: () -> Unit
) {
    val currentUserId = uiState.currentUser!!.id
    val isCurrentUser = message.senderId == currentUserId
    val dismissState = swipeToDismissBoxState(isCurrentUser, onMessageSwipe, message, onSwipeComplete)


    if (isCurrentUser) {
        MessageContent(
            viewModel = viewModel,
            message = message,
            currentUserId = currentUserId,
            isLastMessage = isLastMessage,
            horizontalArrangement = Arrangement.End,
            onSwipeComplete = onSwipeComplete
        )
    } else {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DismissBackground(dismissState)
            },
            content = {
                MessageContent(
                    viewModel = viewModel,
                    message = message,
                    currentUserId = currentUserId,
                    isLastMessage = isLastMessage,
                    horizontalArrangement = Arrangement.Start,
                    onSwipeComplete = onSwipeComplete
                )
            },
            enableDismissFromEndToStart = false,
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
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    isLastMessage: Boolean,
    horizontalArrangement: Arrangement.Horizontal,
    onSwipeComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val isSheetOpen = remember { mutableStateOf(false) }
    val showEmojiPanel = remember { mutableStateOf(false) }
    val isCurrentUser = message.senderId == currentUserId

    Column {
        EmojiPanel(showEmojiPanel, viewModel, message, currentUserId, horizontalArrangement)

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
                    showEmojiPanel = showEmojiPanel,
                    coroutineScope = coroutineScope,
                    isSheetOpen = isSheetOpen,
                    horizontalArrangement = horizontalArrangement
                )
            }

            if (!isCurrentUser) {
                ReplyButton { onSwipeComplete() }
                Spacer(modifier = Modifier.weight(1 - HORIZONTAL_SCREEN_PERCENTAGE+0.05f)) // Adjust space between bubble and button
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

    ReactionBottomSheet(
        viewModel = viewModel,
        reactions = message.reactions,
        sheetState = sheetState,
        isSheetOpen = isSheetOpen,
        coroutineScope = coroutineScope
    )
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
@Composable
private fun MessageBubble(
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    showEmojiPanel: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    isSheetOpen: MutableState<Boolean>,
    horizontalArrangement: Arrangement.Horizontal
) {
    val (maxWidth, maxHeight) = getMaxMessageDimensions()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var dimensionsLoaded by remember { mutableStateOf(false) }
    val isCurrentUser = message.senderId == currentUserId
    val (imageWidth, imageHeight) = if (dimensionsLoaded) {
        calculateImageDimensions(aspectRatio, maxWidth, maxHeight)
    } else {
        Pair(200.dp, 200.dp)
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
            Column(bubbleModifier(isCurrentUser)) {
                ImageBox(
                    viewModel = viewModel,
                    message = message,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    showEmojiPanel = showEmojiPanel
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




@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun swipeToDismissBoxState(
    isCurrentUser: Boolean,
    onMessageSwipe: (Message) -> Unit,
    message: Message,
    onSwipeComplete: () -> Unit
): SwipeToDismissBoxState {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> {
                    if (!isCurrentUser) {
                        onMessageSwipe(message)
                    }
                    true
                }

                SwipeToDismissBoxValue.Settled -> {
                    onSwipeComplete() // Notify when swipe is complete
                    false
                }
            }
        },
        positionalThreshold = { it * 100000f }
    )
    return dismissState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comment...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                    )
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}







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
    showEmojiPanel: MutableState<Boolean>
) {
    //Log.e(LOG_TAG, viewModel.uiState.value.selectedMessage.toString())

    Box(
        modifier = Modifier
            .width(imageWidth)
            .height(imageHeight)
            .combinedClickable(
                onClick = { viewModel.setSelectedMessage(message) },
                onLongClick = {
                    showEmojiPanel.value = !showEmojiPanel.value
                }
            )
    ) {
//        GlideImage(
//            model = message.imageUrl,
//            contentDescription = "Message Image",
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight()
//                .background(Color.Gray),
//            contentScale = ContentScale.Crop,
//
//        )
        GlideImage(
            model = message.imageUrl,
            contentDescription = "Message Image",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Gray),
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

//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: Target<Drawable>,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onResourceReady(
//                        resource: Drawable,
//                        model: Any,
//                        target: Target<Drawable>?,
//                        dataSource: DataSource,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        TODO("Not yet implemented")
//                    }
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
            style = MaterialTheme.typography.bodyLarge,
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