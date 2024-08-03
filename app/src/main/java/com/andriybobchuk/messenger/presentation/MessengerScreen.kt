package com.andriybobchuk.messenger.presentation

import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import MessageItem
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.andriybobchuk.messenger.model.Message
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.andriybobchuk.messenger.presentation.components.rememberRequestPermissionAndPickImage
import com.andriybobchuk.messenger.model.User
import com.andriybobchuk.messenger.presentation.overlays.FullscreenPopup
import com.andriybobchuk.messenger.presentation.overlays.ImagePickerScreen
import com.andriybobchuk.messenger.presentation.overlays.image_detail.FullscreenImageViewer
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel.Companion
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.ui.theme.LightGrey
import kotlinx.coroutines.launch
import java.util.Calendar


private const val LOG_TAG = "MessengerScreen"

/**
 * Main screen for the messenger feature, displaying the header, messages list,
 * and send image button. Also, if any overlays are enabled like fullscreen view of a specific
 * picture or image picker, it shows it.
 * Heavily relies on viewmodel for state management.
 *
 * @see showOverlays
 */
@Composable
fun MessengerScreen(
    viewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    //var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    val replyingToMessage = uiState.replyingToMessage
    val currentUserId = uiState.currentUser!!.id

    val requestPermissionAndPickImage = rememberRequestPermissionAndPickImage(
        context = LocalContext.current,
        onImagePicked = { uri ->
            viewModel.setPickedImage(uri)
        }
    )
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isAtBottom by remember {
        derivedStateOf {
            val lastIndex = 0
            val lastVisibleItem = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()
            lastVisibleItem?.index == lastIndex
        }
    }

    val selectedMessage = uiState.selectedMessage
    val pickedImageUri = uiState.pickedImageUri
    if (selectedMessage != null) {
        FullscreenImageViewer(
            currentUserId = currentUserId,
            message = selectedMessage,
            viewModel = viewModel,
            onDismiss = { viewModel.setSelectedMessage(null) },
            onDelete = {
                viewModel.deleteMessage(selectedMessage.id)
                viewModel.setSelectedMessage(null)
            }
        )
    } else if (pickedImageUri != null) {
        ImagePickerScreen(
            imageUri = uiState.pickedImageUri,
            caption = uiState.pickedImageCaption,
            onSendClick = { uri, caption ->
                viewModel.sendImage(uri, caption)
                viewModel.setPickedImage(null)
            },
            onDismiss = {
                viewModel.setPickedImage(null)
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        MessengerScreenHeader(recipient = uiState.recipient!!, onBackClick = onBackClick)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            MessagesList(
                viewModel = viewModel,
                uiState = uiState,
                onSwipeComplete = { message ->
                    viewModel.setReplyingToMessage(message)
                },
                scrollState = scrollState
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                SendImageButton(
                    onClick = { requestPermissionAndPickImage() },
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
                if (!isAtBottom) {
                    ScrollToBottomButton(
                        onClick = {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        }
                    )
                }
            }
            if (replyingToMessage != null) {
               // triggerHapticFeedback(LocalContext.current)
                ReplyField(
                    message = replyingToMessage,
                    onCancel = { viewModel.setReplyingToMessage(null) },
                    onComment = { newCaption ->
                        viewModel.updateMessageCaption(replyingToMessage, newCaption)
                        viewModel.setReplyingToMessage(null)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}


/**
 * Displays a list of messages grouped by date separated by DateHeader tags.
 * Provides functionality to handle image click events and auto-scrolls to the latest message.
 *
 * @see DateHeader
 */
@Composable
fun MessagesList(
    viewModel: ChatViewModel,
    uiState: MessengerUiState,
    onSwipeComplete: (Message) -> Unit,
    scrollState: LazyListState
) {
    val messages = uiState.messages
    if (messages.isEmpty()) return

    LazyColumn(
        state = scrollState,
        reverseLayout = true,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }

        items(messages.size) { i ->
            val message = messages[i]
            Log.e(LOG_TAG, "sortedMessages.size : " + messages.size)
            if (i >= messages.size - 1 && !viewModel.paginationState.endReached && !viewModel.paginationState.isLoading) {
                viewModel.loadMessages()
            }

            val isLastMessage = message.id == viewModel.getLastMessageId()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                MessageItem(
                    viewModel = viewModel,
                    message = message,
                    uiState = uiState,
                    isLastMessage = isLastMessage,
                    onSwipeComplete = { onSwipeComplete(message) }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Determine if a date header should be shown before this message
            val showDateHeader = if (i == messages.size - 1) {
                true
            } else {
                !isSameDay(message.timestamp, messages[i + 1].timestamp)
            }

            if (showDateHeader) {
                DateHeader(date = timestampAsDate(message.timestamp))
            }
        }

        item {
            if (viewModel.paginationState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

    }
}

fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
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

@Composable
fun ScrollToBottomButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .size(40.dp)
            .background(LightGrey, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Scroll to Bottom",
            tint = Black
        )
    }
}

@Composable
fun ReplyField(
    message: Message,
    onCancel: () -> Unit,
    onComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(message.caption ?: "") }
    val roundedShape = RoundedCornerShape(48.dp)

    Column(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(LightGrey, shape = roundedShape)
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp), // To create space for buttons
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, shape = CircleShape),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(0.dp) // No extra padding inside the button
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Your comment...") },
                modifier = Modifier
                    .weight(1f) // Allows the TextField to take remaining space
                    .background(LightGrey, shape = roundedShape),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = LightGrey,
                    focusedContainerColor = LightGrey,
                    focusedIndicatorColor = LightGrey,
                    unfocusedIndicatorColor = LightGrey
                ),
                singleLine = true,
                textStyle = TextStyle(color = Black, fontSize = 16.sp),
                shape = roundedShape
            )
            Button(
                onClick = { onComment(text) },
                colors = ButtonDefaults.buttonColors(containerColor = Black)
            ) {
                Text("Comment", color = Color.White)
            }
        }
    }
}

/**
 * Displays the header of the messenger screen with recipient's information
 * and a back button.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MessengerScreenHeader(
    recipient: User,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Set the height of the top bar
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GlideImage(
                        model = recipient.profilePictureUrl,
                        contentDescription = "Recipient Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray, shape = CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = recipient.name,
                            color = Color.Black,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Last online " + timestampAsDate(recipient.lastOnline),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BuildCounterDisplay()
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
    }
}

@Composable
fun BuildCounterDisplay() {
    val context = LocalContext.current
    val buildCounter = remember { getBuildCounter(context) }

    Text(text = "Build #$buildCounter", fontSize = 14.sp, modifier = Modifier.padding(16.dp))
}

/**
 * Button to initiate the image picking process.
 */
@Composable
fun SendImageButton(onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        )
    ) {
        Text(
            text = "Pick Image",
            color = Color.White,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send, // Your message icon resource
            contentDescription = "Send Image",
            tint = Color.White
        )
    }
}