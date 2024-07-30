package com.andriybobchuk.messenger.presentation

import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import MessageItem
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
import com.andriybobchuk.messenger.presentation.components.DateHeader
import com.andriybobchuk.messenger.presentation.components.rememberRequestPermissionAndPickImage
import com.andriybobchuk.messenger.presentation.components.showOverlays
import com.andriybobchuk.messenger.model.User
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.ui.theme.LightGrey
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SortedMap

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
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
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
            val lastIndex = scrollState.layoutInfo.totalItemsCount - 1
            val lastVisibleItem = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == lastIndex
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (!showOverlays(uiState = uiState, viewModel = viewModel)) {
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
                    onMessageSwipe = {},
                    onSwipeComplete = { message ->
                        replyingToMessage = message
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
                                    scrollState.animateScrollToItem(scrollState.layoutInfo.totalItemsCount + 1)
                                }
                            }
                        )
                    }
                }
                if (replyingToMessage != null) {
                    ReplyField(
                        message = replyingToMessage!!,
                        onCancel = { replyingToMessage = null },
                        onComment = { newCaption ->
                            viewModel.updateMessageCaption(replyingToMessage!!.id, newCaption)
                            replyingToMessage = null
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
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
    onMessageSwipe: (Message) -> Unit,
    onSwipeComplete: (Message) -> Unit,
    scrollState: LazyListState
) {
    val messages = uiState.messages
    if (messages.isEmpty()) return

    val sortedMessages = messages.sortedBy { it.timestamp }

    LaunchedEffect(messages.size) {
        scrollState.animateScrollToItem(sortedMessages.size + 1) // Use scrollState here
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        var previousDate: String? = null
        sortedMessages.forEach { message ->
            val messageDate = timestampAsDate(message.timestamp)
            if (messageDate != previousDate) {
                item {
                    DateHeader(date = messageDate)
                }
                previousDate = messageDate
            }
            item {
                val isLastMessage = message.id == sortedMessages.lastOrNull()?.id
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    MessageItem(
                        viewModel = viewModel,
                        message = message,
                        uiState = uiState,
                        isLastMessage = isLastMessage,
                        onMessageSwipe = onMessageSwipe,
                        onSwipeComplete = { onSwipeComplete(message) }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
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
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Last online " + timestampAsDate(recipient.lastOnline),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
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