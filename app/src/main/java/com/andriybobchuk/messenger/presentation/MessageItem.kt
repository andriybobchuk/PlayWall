import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
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
import com.andriybobchuk.messenger.presentation.components.gestureModifier
import com.andriybobchuk.messenger.presentation.components.getMaxMessageDimensions
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.ui.theme.LightBlue
import com.andriybobchuk.messenger.ui.theme.LightGrey
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch

private const val LOG_TAG = "MessageItem"

/**
 * Main composable to hold a skeleton of the message bubble with appropriate styling,
 * gestures, and reactions.
 * Handles user interactions like clicking and long-clicking for reactions and viewing images
 * in fullscreen.
 * Uses gestures to toggle emoji panel and open a reaction bottom sheet.
 *
 * @see EmojiPanel -> This appears on top of the message when you log press it.
 * @see MessageContent -> This is the actual message bubble.
 * @see MessageReactionBox -> This is an emoji attached to the message bubble if anyone reacted.
 * @see ReactionBottomSheet -> This rolls up when you click on MessageReactionBox and shows you
 * people who reacted
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(
    viewModel: ChatViewModel,
    uiState: MessengerUiState,
    message: Message,
    isLastMessage: Boolean,
    onMessageSwipe: (Message) -> Unit,
    onSwipeComplete: () -> Unit // Added callback for swipe completion
) {
    val currentUserId = uiState.currentUser!!.id
    val isCurrentUser = message.senderId == currentUserId
    val (maxWidth, maxHeight) = getMaxMessageDimensions()
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var dimensionsLoaded by remember { mutableStateOf(false) }
    val showEmojiPanel = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    FetchImageAspectRatio(message.imageUrl) { ratio ->
        aspectRatio = ratio
        dimensionsLoaded = true
        Log.d(LOG_TAG, "Fetched aspect ratio for image: $ratio")
    }

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
        positionalThreshold = { it * .25f }
    )

    // Use conditional content based on whether the message is sent by the current user
    if (isCurrentUser) {
        // Message sent by current user, not swipeable
        Column {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, end = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val selectedEmoji = viewModel.getUserReaction(message.id, currentUserId)?.emoji
                if (showEmojiPanel.value) {
                    EmojiPanel(
                        selectedEmoji = selectedEmoji,
                        onEmojiClick = { emoji ->
                            if (selectedEmoji == emoji) {
                                viewModel.removeReaction(message.id, currentUserId)
                            } else {
                                val reaction = Reaction(userName = currentUserId, emoji = emoji)
                                viewModel.addOrUpdateReaction(message.id, reaction)
                            }
                            showEmojiPanel.value = false
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = maxWidth)
                ) {
                    MessageContent(
                        message = message,
                        aspectRatio = aspectRatio,
                        dimensionsLoaded = dimensionsLoaded,
                        maxWidth = maxWidth,
                        maxHeight = maxHeight,
                        isCurrentUser = isCurrentUser,
                        gestureModifier = Modifier // No-op or actual gesture modifier here
                    )
                    MessageReactionBox(
                        reactions = message.reactions,
                        onReactionClick = {
                            coroutineScope.launch {
                                isSheetOpen = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-8).dp, y = 8.dp)
                    )
                }
            }
            if (isLastMessage) {
                Text(
                    text = formatStatus(message.status),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp)
                )
            }
        }
    } else {
        // Message not sent by current user, swipeable
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DismissBackground(dismissState)
            },
            content = {
                Column {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val selectedEmoji = viewModel.getUserReaction(message.id, currentUserId)?.emoji
                        if (showEmojiPanel.value) {
                            EmojiPanel(
                                selectedEmoji = selectedEmoji,
                                onEmojiClick = { emoji ->
                                    if (selectedEmoji == emoji) {
                                        viewModel.removeReaction(message.id, currentUserId)
                                    } else {
                                        val reaction = Reaction(userName = currentUserId, emoji = emoji)
                                        viewModel.addOrUpdateReaction(message.id, reaction)
                                    }
                                    showEmojiPanel.value = false
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = maxWidth)
                        ) {
                            MessageContent(
                                message = message,
                                aspectRatio = aspectRatio,
                                dimensionsLoaded = dimensionsLoaded,
                                maxWidth = maxWidth,
                                maxHeight = maxHeight,
                                isCurrentUser = isCurrentUser,
                                gestureModifier = Modifier // No-op or actual gesture modifier here
                            )
                            MessageReactionBox(
                                reactions = message.reactions,
                                onReactionClick = {
                                    coroutineScope.launch {
                                        isSheetOpen = true
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-8).dp, y = 8.dp)
                            )
                        }
                    }
                    if (isLastMessage) {
                        Text(
                            text = formatStatus(message.status),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 16.dp)
                        )
                    }
                }
                if (isSheetOpen) {
                    ReactionBottomSheet(
                        viewModel = viewModel,
                        reactions = message.reactions,
                        sheetState = sheetState,
                        onDismiss = {
                            coroutineScope.launch {
                                isSheetOpen = false
                            }
                        }
                    )
                }
            }
        )
    }
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
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Reply",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp),
                    tint = Color.Black
                )
            }
        }
        SwipeToDismissBoxValue.EndToStart -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Reply",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(16.dp),
                    tint = Color.Black
                )
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}




/**
 * Displays the content of a chat message, including the image, caption, and timestamp.
 * NOT including the reactions stuff like top reaction panel or reaction box.
 * Handles the dynamic calculation of image dimensions based on the aspect ratio so that long images
 * are displayed as long images and wide images preserve their wide format.
 *
 * @see ImageBox
 * @see CaptionAndTimestamp
 */
@Composable
fun MessageContent(
    message: Message,
    aspectRatio: Float,
    dimensionsLoaded: Boolean,
    maxWidth: Dp,
    maxHeight: Dp,
    isCurrentUser: Boolean,
    gestureModifier: Modifier
) {
    val (imageWidth, imageHeight) = if (dimensionsLoaded) {
        calculateImageDimensions(aspectRatio, maxWidth, maxHeight)
    } else {
        Pair(200.dp, 200.dp)
    }

    Column(
        modifier = Modifier
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
    ) {
        ImageBox(
            message = message,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            gestureModifier = gestureModifier
        )

        CaptionAndTimestamp(
            message = message,
            imageWidth = imageWidth,
            modifier = Modifier
                .align(Alignment.End)
        )
    }
}

/**
 * Displays an image within a chat message bubble, including the image's dimensions and gesture handling.
 * Shows a timestamp on the image if there is no caption.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageBox(
    message: Message,
    imageWidth: Dp,
    imageHeight: Dp,
    gestureModifier: Modifier
) {
    Box(
        modifier = gestureModifier
            .width(imageWidth)
            .height(imageHeight)
    ) {
        GlideImage(
            model = message.imageUrl,
            contentDescription = "Message Image",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Gray),
            contentScale = ContentScale.Crop
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