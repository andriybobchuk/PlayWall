package com.andriybobchuk.messenger.feature.chat.presentation

import com.andriybobchuk.messenger.feature.chat.presentation.viewmodel.ChatViewModel
import MessageItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.andriybobchuk.messenger.R
import com.andriybobchuk.messenger.feature.chat.presentation.util.rememberRequestPermissionAndPickImage
import com.andriybobchuk.messenger.feature.chat.model.User
import com.andriybobchuk.messenger.feature.chat.presentation.util.BuildCounterDisplay
import com.andriybobchuk.messenger.feature.chat.presentation.util.ConnectivityStatus
import com.andriybobchuk.messenger.feature.chat.presentation.overlays.ImagePicker
import com.andriybobchuk.messenger.feature.chat.presentation.overlays.ImageViewer
import com.andriybobchuk.messenger.feature.chat.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.feature.chat.presentation.util.isSameDay
import com.andriybobchuk.messenger.feature.chat.presentation.util.timestampAsDate
import kotlinx.coroutines.launch

/**
 * Main screen for the messenger feature, displaying the header, messages list,
 * and send image button. Also, if any overlays are enabled like fullscreen view of a specific
 * picture or image picker, it shows it.
 * Heavily relies on viewmodel for state management.
 */
@Composable
fun MessengerScreen(
    viewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = uiState.currentUser!!.id
    val requestPermissionAndPickImage = rememberRequestPermissionAndPickImage(
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
    val isConnected = ConnectivityStatus()
    LaunchedEffect(isConnected) {
        viewModel.setConnectivityStatus(isConnected)
    }

    FullscreenOverlays(uiState, currentUserId, viewModel)

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
        }
    }
}

@Composable
private fun FullscreenOverlays(
    uiState: MessengerUiState,
    currentUserId: String,
    viewModel: ChatViewModel
) {
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
                DateHeader(date = timestampAsDate(message.timestamp, LocalContext.current))
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
            contentDescription = stringResource(R.string.down),
            tint = MaterialTheme.colorScheme.primary
        )
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
                .height(60.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
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
                        contentDescription = stringResource(R.string.recipient_profile_picture),
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
                            text = stringResource(R.string.last_online) + timestampAsDate(recipient.lastOnline, LocalContext.current),
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
            text = stringResource(R.string.pick_image),
            color = MaterialTheme.colorScheme.background,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(R.string.send_image),
            tint = MaterialTheme.colorScheme.background
        )
    }
}