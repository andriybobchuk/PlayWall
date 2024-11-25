package com.studios1299.playwall.play.presentation.chat

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.studios1299.playwall.R
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.downloadImageToDevice
import com.studios1299.playwall.core.presentation.components.Buttons
import com.studios1299.playwall.play.data.model.Message
import com.studios1299.playwall.play.data.model.Reaction
import com.studios1299.playwall.play.data.model.User
import com.studios1299.playwall.play.presentation.chat.viewmodel.ChatViewModel
import com.studios1299.playwall.play.presentation.play.FriendshipStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Displays the emoji reactions for a message in a small box that can be clicked to open
 * a larger reaction panel. Handles click events to manage reactions.
 */
@Composable
fun MessageReactionIndicator(
    reaction: Reaction?,
    onReactionClick: () -> Unit,
    isCurrentUser: Boolean,
    modifier: Modifier
) {
    if (reaction != null) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(18.dp))
                .clickable(onClick = onReactionClick)
                .padding(4.dp)
        ) {
            Row {
                Text(
                    text = reaction.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactSheet(
    recipient: User,
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: Int,
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
) {
    val focusState = remember { mutableStateOf(false) }
    LaunchedEffect(focusState.value) {
        if (focusState.value) {
            sheetState.expand()
        } else {
            sheetState.partialExpand()
        }
    }

    if (isSheetOpen.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = {
                coroutineScope.launch {
                    isSheetOpen.value = false
                }
            }
        ) {
            val selectedReaction = message.reaction
            val isCurrentUser = message.senderId == currentUserId

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (recipient.status == FriendshipStatus.accepted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Reaction.entries.forEach { reaction ->
                            val isSelected = reaction == selectedReaction
                            Text(
                                text = reaction.toString(),
                                fontSize = 24.sp,
                                color = if (isSelected) Color.Black else Color.Unspecified,
                                modifier = Modifier
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.secondary.copy(
                                            alpha = 0.3f
                                        ) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (selectedReaction == reaction) {
                                            viewModel.addOrUpdateReaction(message.id, null)
                                        } else {
                                            viewModel.addOrUpdateReaction(message.id, reaction)
                                        }
                                        isSheetOpen.value = false
                                    }
                                    .padding(6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recipient.status == FriendshipStatus.accepted) {
                    ReplyField(
                        message = message,
                        viewModel = viewModel,
                        isSheetOpen = isSheetOpen,
                        onFocusChanged = { isFocused -> focusState.value = isFocused  }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                val context = LocalContext.current
                Buttons.Secondary(text = "Download wallpaper", isLoading = false, onClick = {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        Dexter.withContext(MyApp.appModule.context)
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(object : PermissionListener {
                                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse?) {
                                    downloadImageToDevice(MyApp.appModule.context, message.imageUrl) { success ->
                                        Toast.makeText(
                                            context,
                                            if (success) "Saved successfully" else "Saving failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse?) {
                                    Log.e("DownloadDebug", "Permission Denied: $permissionDeniedResponse")
                                }
                                override fun onPermissionRationaleShouldBeShown(permissionRequest: PermissionRequest?, token: PermissionToken?) {
                                    token?.continuePermissionRequest()
                                }
                            }).check()
                    } else {
                        downloadImageToDevice(MyApp.appModule.context, message.imageUrl) { success ->
                            Toast.makeText(
                                context,
                                if (success) "Saved successfully" else "Saving failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    onClick = {
                        viewModel.reportWallpaper(message.id)
                    Toast.makeText(context, "Wallpaper has been reported", Toast.LENGTH_SHORT).show()
                        isSheetOpen.value = false
                }) {
                    Text(text = "Report this wallpaper", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (focusState.value) {
                    Spacer(modifier = Modifier.height(300.dp))
                }
            }
        }
    }
}

@Composable
fun ReplyField(
    message: Message,
    viewModel: ChatViewModel,
    isSheetOpen: MutableState<Boolean>,
    onFocusChanged: (Boolean) -> Unit,
) {
    var text by remember { mutableStateOf(message.caption) }
    val roundedShape = RoundedCornerShape(14.dp)
    val maxCharacters = 200
    val context = LocalContext.current

    var isFocused by remember {
        mutableStateOf(false)
    }

    // Track whether the character limit has been exceeded
    var showLimitExceededToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, shape = roundedShape)
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = text?:"",
                onValueChange = {
                    if (it.length <= maxCharacters) {
                        text = it
                        showLimitExceededToast = false
                    } else {
                        showLimitExceededToast = true
                    }
                },
                label = { Text(stringResource(R.string.your_comment)) },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = roundedShape)
                    .onFocusChanged {
                        isFocused = it.isFocused
                        onFocusChanged(it.isFocused)
                    },
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
                onClick = {
                    val trimmedText = text?.trimEnd() // Trim trailing spaces before sending
                    viewModel.addOrUpdateComment(message.id, trimmedText)
                    isSheetOpen.value = false
                },
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.comment), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    // Show a toast if the character limit is exceeded
    if (showLimitExceededToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context,
                "Character limit is $maxCharacters",
                Toast.LENGTH_SHORT
            ).show()
            showLimitExceededToast = false
        }
    }
}

@Composable
fun EmojiHint(
    messageId: Int,
    onAddReaction: (Reaction) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Reaction.entries.forEach { reaction ->
                Text(
                    text = reaction.toString(),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            onAddReaction(reaction)
                        }
                        .padding(horizontal = 10.dp)
                )
            }
        }
    }
}
