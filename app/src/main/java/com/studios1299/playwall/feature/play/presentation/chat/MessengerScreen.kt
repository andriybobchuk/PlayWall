package com.studios1299.playwall.feature.play.presentation.chat

import com.studios1299.playwall.feature.play.presentation.chat.viewmodel.ChatViewModel
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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.chat.util.BuildCounterDisplay
import com.studios1299.playwall.feature.play.presentation.chat.util.ConnectivityStatus
import com.studios1299.playwall.feature.play.presentation.chat.overlays.ImagePicker
import com.studios1299.playwall.feature.play.presentation.chat.overlays.ImageViewer
import com.studios1299.playwall.feature.play.presentation.chat.viewmodel.MessengerUiState
import com.studios1299.playwall.feature.play.presentation.chat.util.isSameDay
import com.studios1299.playwall.feature.play.presentation.chat.util.timestampAsDate
import com.studios1299.playwall.feature.play.presentation.play.FriendshipStatus
import kotlinx.coroutines.launch

/**
 * Main screen for the messenger feature, displaying the header, messages list,
 * and send image button. Also, if any overlays are enabled like fullscreen view of a specific
 * picture or image picker, it shows it.
 * Heavily relies on viewmodel for state management.
 */
@Composable
fun MessengerScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (uiState.messages.isEmpty()) return@LifecycleEventObserver
                viewModel.markMessagesAsRead(uiState.recipient?.friendshipId ?: -1, uiState.messages[0].id)
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        // Cleanup when the effect leaves the Composition
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    FullscreenOverlays(uiState, viewModel)

    Scaffold {
        Column(modifier = modifier
            .fillMaxSize()
            .padding(it)) {
            MessengerScreenHeader(
                recipient = uiState.recipient?:User(-1, "", "", since = "", status = FriendshipStatus.accepted, requesterId = -1, friendshipId = -1),
                viewModel = viewModel,
                onBackClick = onBackClick,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                MessagesList(
                    recipient = uiState.recipient?:User(-1, "", "", since = "", status = FriendshipStatus.accepted, requesterId = -1, friendshipId = -1),
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
                        recipient = uiState.recipient?:User(-1, "", "", since = "", status = FriendshipStatus.accepted, requesterId = -1, friendshipId = -1),
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
}

@Composable
private fun FullscreenOverlays(
    uiState: MessengerUiState,
    viewModel: ChatViewModel
) {
    val selectedMessage = uiState.selectedMessage
    val pickedImageUri = uiState.pickedImageUri
    val context = LocalContext.current

    if (selectedMessage != null) {
        ImageViewer(
            uiState = uiState,
            message = selectedMessage,
            onDismiss = { viewModel.setSelectedMessage(null) },
        )
    } else if (pickedImageUri != null) {
        ImagePicker(
            imageUri = uiState.pickedImageUri,
            caption = uiState.pickedImageCaption,
            onSendClick = { uri, caption ->
                viewModel.sendWallpaper(
                    context = context,
                    uri = uri,
                    comment = null,
                    reaction = null,
                )
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
    recipient: User,
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
            val isLastMessage = message.id == messages[0].id

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                MessageItem(
                    recipient = uiState.recipient?:User(-1, "", "", since = "", status = FriendshipStatus.accepted, requesterId = -1, friendshipId = -1),
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
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

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
                    Images.Circle(model = recipient.profilePictureUrl)
                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = recipient.email,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Became friends ${timestampAsDate(recipient.since, LocalContext.current)}",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Three-dot menu button
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = { isMenuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = if(recipient.status == FriendshipStatus.accepted) "Mute friend" else "Unmute friend") },
                        onClick = {
                            if (recipient.status == FriendshipStatus.accepted) {
                                viewModel.blockFriend(recipient.friendshipId, recipient.id)
                            } else {
                                viewModel.unblockFriend(recipient.friendshipId, recipient.id)
                            }
                            isMenuExpanded = false
                        }
                    )
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
fun SendImageButton(onClick: () -> Unit, recipient: User, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        enabled = recipient.status == FriendshipStatus.accepted
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