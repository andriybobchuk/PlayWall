package com.andriybobchuk.messenger.presentation

import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import MessageItem
import android.util.Log
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.andriybobchuk.messenger.model.Message
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.andriybobchuk.messenger.presentation.components.rememberRequestPermissionAndPickImage
import com.andriybobchuk.messenger.model.User
import com.andriybobchuk.messenger.presentation.components.BuildCounterDisplay
import com.andriybobchuk.messenger.presentation.components.ConnectivityStatus
import com.andriybobchuk.messenger.presentation.overlays.ImagePicker
import com.andriybobchuk.messenger.presentation.overlays.ImageViewer
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.util.isSameDay
import com.andriybobchuk.messenger.util.timestampAsDate
import kotlinx.coroutines.launch

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
        ImageViewer(
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
        ImagePicker(
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

    val isConnected = ConnectivityStatus()
    LaunchedEffect(isConnected) {
        viewModel.setConnectivityStatus(isConnected)
    }

    Column(modifier = modifier.fillMaxSize()) {
        MessengerScreenHeader(recipient = uiState.recipient!!, onBackClick = onBackClick)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            MessagesList(
                viewModel = viewModel,
                uiState = uiState,
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
                Log.e(LOG_TAG, "MessengerScreen replyingToMessage = : ${replyingToMessage.caption}")
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
//@Composable
//fun MessagesList(
//    viewModel: ChatViewModel,
//    uiState: MessengerUiState,
//    onSwipeComplete: (Message) -> Unit,
//    scrollState: LazyListState
//) {
//    val messages = uiState.messages
//    if (messages.isEmpty()) return
//
//    LazyColumn(
//        state = scrollState,
//        reverseLayout = true,
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        item {
//            Spacer(modifier = Modifier.height(64.dp))
//        }
//
//        items(messages.size) { i ->
//            val message = messages[i]
//            Log.e(LOG_TAG, "sortedMessages.size : " + messages.size)
//            if (i >= messages.size - 1 && !viewModel.paginationState.endReached && !viewModel.paginationState.isLoading) {
//                viewModel.loadMessages()
//            }
//
//            val isLastMessage = message.id == viewModel.getLastMessageId()
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                MessageItem(
//                    viewModel = viewModel,
//                    message = message,
//                    uiState = uiState,
//                    isLastMessage = isLastMessage,
//                    onSwipeComplete = { onSwipeComplete(message) }
//                )
//            }
//            Spacer(modifier = Modifier.height(6.dp))
//
//            // Determine if a date header should be shown before this message
//            val showDateHeader = if (i == messages.size - 1) {
//                true
//            } else {
//                !isSameDay(message.timestamp, messages[i + 1].timestamp)
//            }
//
//            if (showDateHeader) {
//                DateHeader(date = timestampAsDate(message.timestamp))
//            }
//        }
//
//        item {
//            if (viewModel.paginationState.isLoading) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//        }
//    }
//}


fun onComment(message: Message, viewModel: ChatViewModel) {
    viewModel.setReplyingToMessage(null) // To close previous panels
    val updatedMessage = viewModel.getUpdatedMessageById(message.id)!!
    viewModel.setReplyingToMessage(updatedMessage)
}
@Composable
fun MessagesList(
    viewModel: ChatViewModel,
    uiState: MessengerUiState,
    scrollState: LazyListState
) {
    val messages = uiState.messages
    if (messages.isEmpty()) return

    LazyColumn(
        state = scrollState,
        reverseLayout = true,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }

        items(messages, key = { it.id }) { message ->
            Log.e(LOG_TAG, "sortedMessages.size : " + messages.size)
            if (messages.indexOf(message) >= messages.size - 1 && !viewModel.paginationState.endReached && !viewModel.paginationState.isLoading) {
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
                    isLastMessage = isLastMessage
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Determine if a date header should be shown before this message
            val showDateHeader = if (messages.indexOf(message) == messages.size - 1) {
                true
            } else {
                !isSameDay(message.timestamp, messages[messages.indexOf(message) + 1].timestamp)
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
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary)
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
            .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Scroll to Bottom",
            tint = MaterialTheme.colorScheme.primary
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

    Log.e(LOG_TAG, "text: " + text)

    Column(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, shape = roundedShape)
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
                    .background(MaterialTheme.colorScheme.background, shape = CircleShape),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(0.dp) // No extra padding inside the button
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Your comment...") },
                modifier = Modifier
                    .weight(1f) // Allows the TextField to take remaining space
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = roundedShape),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                shape = roundedShape
            )
            Button(
                onClick = { onComment(text) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Comment", color = MaterialTheme.colorScheme.background)
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
            .background(MaterialTheme.colorScheme.background)
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
                    tint = MaterialTheme.colorScheme.primary
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
                            .background(MaterialTheme.colorScheme.outline, shape = CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = recipient.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Last online " + timestampAsDate(recipient.lastOnline),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BuildCounterDisplay()
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primaryContainer)
    }
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
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "Pick Image",
            color = MaterialTheme.colorScheme.background,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send, // Your message icon resource
            contentDescription = "Send Image",
            tint = MaterialTheme.colorScheme.background
        )
    }
}