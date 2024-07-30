package com.andriybobchuk.messenger.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.andriybobchuk.messenger.Constants.EMOJI_LIST
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.Reaction
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import com.andriybobchuk.messenger.presentation.viewmodel.MessengerUiState
import com.andriybobchuk.messenger.ui.theme.LightGrey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Displays a panel of emojis for the user to select as reactions to a message.
 * Highlights the currently selected emoji, if any.
 *
 * @param selectedEmoji The currently selected emoji.
 * @param onEmojiClick A callback function that is invoked when an emoji is clicked,
 *                     passing the clicked emoji as a parameter.
 */
@Composable
fun EmojiPanel(
    showEmojiPanel: MutableState<Boolean>,
    viewModel: ChatViewModel,
    message: Message,
    currentUserId: String,
    horizontalArrangement: Arrangement.Horizontal
) {
    val selectedEmoji = viewModel.getUserReaction(message.id, currentUserId)?.emoji
    Row(
        modifier = Modifier
            .padding(top = 8.dp, end = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        if (showEmojiPanel.value) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(LightGrey)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val emojis = EMOJI_LIST
                emojis.forEach { emoji ->
                    val isSelected = emoji == selectedEmoji
                    Text(
                        text = emoji,
                        fontSize = 18.sp,
                        color = if (isSelected) Color.Black else Color.Unspecified,
                        modifier = Modifier
                            .background(
                                if (isSelected) Color.DarkGray.copy(alpha = 0.2f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                if (selectedEmoji == emoji) {
                                    viewModel.removeReaction(message.id, currentUserId)
                                } else {
                                    val reaction = Reaction(userName = currentUserId, emoji = emoji)
                                    viewModel.addOrUpdateReaction(message.id, reaction)
                                }
                                showEmojiPanel.value = false
                            }
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

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
                .background(LightGrey)
                .border(1.dp, Color.White, RoundedCornerShape(18.dp))
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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