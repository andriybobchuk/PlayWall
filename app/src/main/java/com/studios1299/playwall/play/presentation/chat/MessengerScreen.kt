package com.studios1299.playwall.play.presentation.chat

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import com.studios1299.playwall.play.presentation.chat.viewmodel.ChatViewModel
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.Banners
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_PURPLE
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_WHITE
import com.studios1299.playwall.monetization.presentation.screens.EVIL_EMOJI
import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import com.studios1299.playwall.play.data.model.User
import com.studios1299.playwall.play.presentation.chat.util.ConnectivityStatus
import com.studios1299.playwall.play.presentation.chat.overlays.ImageViewer
import com.studios1299.playwall.play.presentation.chat.viewmodel.MessengerUiState
import com.studios1299.playwall.play.presentation.chat.util.isSameDay
import com.studios1299.playwall.play.presentation.chat.util.timestampAsDate
import com.studios1299.playwall.play.presentation.play.FriendshipStatus
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

private const val LOG_TAG = "MessengerScreen"

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
    modifier: Modifier = Modifier,
    onOpenWrzutomat: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.goBack) {
        if (uiState.goBack) {
            onBackClick()
        }
    }
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

    LaunchedEffect(Unit) {
        viewModel.errorMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    FullscreenOverlays(uiState, viewModel, onOpenWrzutomat)

    Scaffold {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {
            MessengerScreenHeader(
                recipient = uiState.recipient ?: User(
                    -1,
                    "",
                    "",
                    since = "",
                    status = FriendshipStatus.accepted,
                    requesterId = -1,
                    friendshipId = -1,
                    screenRatio = 2f
                ),
                viewModel = viewModel,
                onBackClick = onBackClick,
                onRefreshChat = { viewModel.resetChat() }
            )
            if (!uiState.isOnline) {
                Banners.OfflineStatus()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                MessagesList(
                    recipient = uiState.recipient ?: User(
                        -1,
                        "",
                        "",
                        since = "",
                        status = FriendshipStatus.accepted,
                        requesterId = -1,
                        friendshipId = -1,
                        screenRatio = 2f
                    ),
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
                        recipient = uiState.recipient ?: User(
                            -1,
                            "",
                            "",
                            since = "",
                            status = FriendshipStatus.accepted,
                            requesterId = -1,
                            friendshipId = -1,
                            screenRatio = 2f
                        ),
                        uiState = uiState,
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                    if (!isAtBottom && !uiState.messages.isEmpty()) {
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
    viewModel: ChatViewModel,
    onOpenWrzutomat: () -> Unit
) {
    val selectedMessage = uiState.selectedMessage
    val pickedImageUri = uiState.pickedImageUri
    val context = LocalContext.current

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultUri = UCrop.getOutput(result.data ?: return@rememberLauncherForActivityResult)
        if (resultUri != null) {
            viewModel.sendWallpaper(
                context = context,
                uri = resultUri,
                comment = null,
                reaction = null,
                onOpenWrzutomat = onOpenWrzutomat
            )
            viewModel.setPickedImage(null)
        }
    }

    fun launchUCrop(sourceUri: Uri, screenRatio: Float?) {
        val destinationUri =
            Uri.fromFile(File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(screenRatio ?: 1f, 1f)
            .withMaxResultSize(4096, 4096)
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(100)
                setFreeStyleCropEnabled(true)
                setHideBottomControls(false)
                setToolbarColor(ZEDGE_PURPLE.toArgb())
                setStatusBarColor(ZEDGE_PURPLE.toArgb())
                setToolbarWidgetColor(ZEDGE_WHITE.toArgb())
                setToolbarTitle("Adjust wallpaper")
                setDimmedLayerColor(ZEDGE_PURPLE.toArgb())
                setActiveControlsWidgetColor(ZEDGE_PURPLE.toArgb())
            })

        cropLauncher.launch(uCrop.getIntent(context))
    }

    if (selectedMessage != null) {
        ImageViewer(
            uiState = uiState,
            message = selectedMessage,
            onDismiss = { viewModel.setSelectedMessage(null) },
        )
    } else if (pickedImageUri != null) {
        launchUCrop(
            sourceUri = pickedImageUri,
            screenRatio = 1 / (uiState.recipient?.screenRatio ?: 2f)
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
    if (!uiState.loading && messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.primary_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(),

                ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                        .padding(bottom = 100.dp)
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(200.dp),
                        painter = painterResource(id = R.drawable.ic_pw),
                        contentDescription = "Logo"
                    )
                    Text(
                        text = "No wallpapers yet?",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Start by setting your friend the first wallpaper! $EVIL_EMOJI",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.primary_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            LazyColumn(
                state = scrollState,
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxSize()
                   // .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
                items(messages, key = { it.id }) { message ->
                    if (messages.indexOf(message) >= messages.size - 1 && !viewModel.paginationState.endReached && !viewModel.paginationState.isLoading) {
                        viewModel.loadMessages()
                    }
                    val isLastMessage = message.id == messages[0].id

                    Log.v(
                        LOG_TAG,
                        "Message sender=${uiState.currentUser?.id}, recipient.id=${message.recipientId}, message.id=${message.id}, meagestatus=${message.status}"
                    )

                    val isMine = uiState.currentUser?.id != message.recipientId

                    if (!isMine
                        && message.id != -1
                        && message.status == MessageStatus.unread
                    ) {
                        // Mark the last message as read if it's the recipient's message
                        Log.v(LOG_TAG, "Marking message as read because it should be marked as read")
                        viewModel.markMessagesAsRead(uiState.recipient?.friendshipId ?: -1, message.id)
                    }

                    Row(
                        // modifier = Modifier.fillMaxWidth(),
                        //  horizontalArrangement = Arrangement.End
                    ) {
                        Column {
                            if (isLastMessage && !isMine && message.reaction == null) {
                                EmojiHint(message.id) { reaction ->
                                    viewModel.addOrUpdateReaction(
                                        message.id,
                                        reaction
                                    )
                                }
                            }
                            MessageItem(
                                recipient = uiState.recipient ?: User(
                                    -1,
                                    "",
                                    "",
                                    since = "",
                                    status = FriendshipStatus.accepted,
                                    requesterId = -1,
                                    friendshipId = -1,
                                    screenRatio = 2f
                                ),
                                viewModel = viewModel,
                                message = message,
                                uiState = uiState,
                                isLastMessage = isLastMessage
                            )
                        }
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
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSecondaryContainer)
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
@Composable
fun MessengerScreenHeader(
    recipient: User,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onRefreshChat: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

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
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .padding(start = 48.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                //horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Images.Circle(model = recipient.profilePictureUrl)
                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = recipient.name?:recipient.email,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Became friends ${
                                timestampAsDate(
                                    recipient.since,
                                    LocalContext.current
                                )
                            }",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val context = LocalContext.current
            // Three-dot menu button
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Row {
                    IconButton(
                        onClick = { isMenuExpanded = true },
                        enabled = uiState.isOnline
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
//                    DropdownMenuItem(
//                        text = { Text(text = if (recipient.status == FriendshipStatus.accepted) "Mute friend" else "Unmute friend") },
//                        onClick = {
//                            if (recipient.status == FriendshipStatus.accepted) {
//                               // viewModel.blockFriend(recipient.friendshipId, recipient.id)
//                                viewModel.blockFriend(uiState.currentUser?.friendshipId?:-1, recipient.id)
//                            } else {
//                                viewModel.unblockFriend(recipient.friendshipId, recipient.id)
//                            }
//                            isMenuExpanded = false
//                        }
//                    )
                    DropdownMenuItem(
                        text = { Text(text = "Refresh chat") },
                        onClick = onRefreshChat
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Report") },
                        onClick = {
                            isMenuExpanded = false
                            Toast.makeText(context, "User has been reported, thanks for the feedback", Toast.LENGTH_LONG).show()
                            viewModel.report(null, recipient.id)
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
fun SendImageButton(onClick: () -> Unit, recipient: User, uiState: MessengerUiState, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        enabled = recipient.status == FriendshipStatus.accepted && uiState.isOnline
    ) {
        Text(
            text = stringResource(R.string.pick_image),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(R.string.send_image),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}