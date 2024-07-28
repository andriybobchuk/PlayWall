package com.andriybobchuk.messenger.presentation

import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import MessageItem
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.sp
import com.andriybobchuk.messenger.presentation.components.DateHeader
import com.andriybobchuk.messenger.presentation.components.rememberRequestPermissionAndPickImage
import com.andriybobchuk.messenger.presentation.components.showOverlays
import com.andriybobchuk.messenger.model.User

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

    val requestPermissionAndPickImage = rememberRequestPermissionAndPickImage(
        context = LocalContext.current,
        onImagePicked = { uri ->
            viewModel.setPickedImage(uri)
        }
    )

    Column(modifier = modifier.fillMaxSize()) {
        if (!showOverlays(
                uiState = uiState,
                viewModel = viewModel
            )
        ) {
            MessengerScreenHeader(
                recipient = uiState.recipient!!,
                onBackClick = onBackClick
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.1f))
            ) {
                MessagesList(
                    messages = uiState.messages,
                    currentUserId = uiState.currentUser!!.id,
                    onImageClick = { imageUrl, caption ->
                        viewModel.setFullscreenImage(imageUrl, caption)
                    })
                SendImageButton(
                    onClick = {
                        requestPermissionAndPickImage()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
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
    messages: List<Message>,
    currentUserId: String,
    onImageClick: (String, String) -> Unit
) {
    val groupedMessages = groupMessagesByDate(messages)
    val lastMessageId = messages.lastOrNull()?.id

    val listState = rememberLazyListState()
    LaunchedEffect(messages) {
        listState.animateScrollToItem(messages.size - 1)
        Log.d(LOG_TAG, "Messages updated, scrolling to bottom")
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        groupedMessages.forEach { (date, messagesOnDate) ->
            item {
                DateHeader(date = date)
            }
            items(messagesOnDate) { message ->
                val isLastMessage = message.id == lastMessageId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    MessageItem(
                        message = message,
                        currentUserId = currentUserId,
                        isLastMessage = isLastMessage,
                        onImageClick = { imageUrl ->
                            onImageClick(imageUrl, message.caption)
                        })
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Spacer(modifier = Modifier.padding(vertical = 36.dp))
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
            .padding(horizontal = 18.dp, vertical = 22.dp),
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