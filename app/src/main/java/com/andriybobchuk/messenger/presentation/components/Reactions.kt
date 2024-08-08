package com.andriybobchuk.messenger.presentation.components

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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andriybobchuk.messenger.util.Constants.EMOJI_LIST
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.Reaction
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import com.andriybobchuk.messenger.ui.theme.NAVY200
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Displays the emoji reactions for a message in a small box that can be clicked to open
 * a larger reaction panel. Handles click events to manage reactions.
 */
@Composable
fun MessageReactionBox(
    reactions: List<Reaction>,
    onReactionClick: () -> Unit,
    modifier: Modifier
) {
    if (reactions.isNotEmpty()) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
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
 * @param onDismiss A callback function invoked when the bottom sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionBottomSheet(
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
                    text = "Reactions",
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
fun EmojiBottomSheet(
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
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Emoji",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
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
                                    if (isSelected) NAVY200 else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (selectedEmoji == emoji) {
                                        viewModel.removeReaction(message.id, currentUserId)
                                    } else {
                                        val reaction = Reaction(userName = currentUserId, emoji = emoji)
                                        viewModel.addOrUpdateReaction(message.id, reaction)
                                    }
                                    isSheetOpen.value = false
                                }
                                .padding(6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isCurrentUser) {
                            viewModel.deleteMessage(message.id)
                        } else {
                            viewModel.setReplyingToMessage(message)
                        }
                        isSheetOpen.value = false
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary, disabledContentColor = MaterialTheme.colorScheme.tertiary, disabledContainerColor = Color.Black),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (isCurrentUser) Icons.Default.Delete else Icons.Default.Send,
                        contentDescription = if (isCurrentUser) "Delete this message" else "Reply to message"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCurrentUser) "Delete this message" else "Reply to message",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

