package com.studios1299.vrwallpaper6.feature.play.presentation.screens.chat

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studios1299.vrwallpaper6.feature.play.presentation.util.Constants.EMOJI_LIST
import com.studios1299.vrwallpaper6.feature.play.data.model.Message
import com.studios1299.vrwallpaper6.feature.play.data.model.Reaction
import com.studios1299.vrwallpaper6.feature.play.presentation.screens.chat.viewmodel.ChatViewModel
import com.studios1299.vrwallpaper6.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Displays the emoji reactions for a message in a small box that can be clicked to open
 * a larger reaction panel. Handles click events to manage reactions.
 */
@Composable
fun MessageReactionIndicator(
    reactions: List<Reaction>,
    onReactionClick: () -> Unit,
    isCurrentUser: Boolean,
    modifier: Modifier
) {
    if (reactions.isNotEmpty()) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(18.dp))
                .clickable(onClick = onReactionClick)
                .padding(4.dp)
        ) {
            Row {
                reactions.forEach { reaction ->
                    Text(
                        text = reaction.emoji,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Displays a bottom sheet modal containing a list of reactions to a message.
 * Each reaction is shown along with the username of the user who reacted.
 *
 * @param viewModel The ChatViewModel used to retrieve user information.
 * @param reactions A list of Reaction objects to display.
 * @param sheetState The state of the bottom sheet, controlling its visibility.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reactions(
    viewModel: ChatViewModel,
    reactions: List<Reaction>,
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    coroutineScope: CoroutineScope
) {
    if (isSheetOpen.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = {
                coroutineScope.launch {
                isSheetOpen.value = false
            } }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.reactions),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                ) {
                    reactions.forEach { reaction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.getUserNameById(reaction.userName),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = reaction.emoji,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactSheet(
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    coroutineScope: CoroutineScope
) {
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
            val selectedEmoji = viewModel.getUserReaction(message.id, currentUserId)?.emoji
            val isCurrentUser = message.senderId == currentUserId

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EMOJI_LIST.forEach { emoji ->
                        val isSelected = emoji == selectedEmoji
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            color = if (isSelected) Color.Black else Color.Unspecified,
                            modifier = Modifier
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (selectedEmoji == emoji) {
                                        viewModel.removeReaction(message.id, currentUserId)
                                    } else {
                                        val reaction =
                                            Reaction(userName = currentUserId, emoji = emoji)
                                        viewModel.addOrUpdateReaction(message.id, reaction)
                                    }
                                    isSheetOpen.value = false
                                }
                                .padding(6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (isCurrentUser) {
                    Button(
                        onClick = {
                            viewModel.deleteMessage(message.id)
                            isSheetOpen.value = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary, disabledContentColor = MaterialTheme.colorScheme.tertiary, disabledContainerColor = Color.Black),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_this_message)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.delete_this_message),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    ReplyField(
                        message = message,
                        viewModel = viewModel,
                        isSheetOpen = isSheetOpen
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun ReplyField(
    message: Message,
    viewModel: ChatViewModel,
    isSheetOpen: MutableState<Boolean>,
) {
    var text by remember { mutableStateOf(message.caption ?: "") }
    val roundedShape = RoundedCornerShape(14.dp)
    val maxCharacters = 200
    val context = LocalContext.current

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
                value = text,
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
                onClick = {
                    val trimmedText = text.trimEnd() // Trim trailing spaces before sending
                    viewModel.updateMessageCaption(message, trimmedText)
                    isSheetOpen.value = false
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.comment), color = MaterialTheme.colorScheme.background)
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

