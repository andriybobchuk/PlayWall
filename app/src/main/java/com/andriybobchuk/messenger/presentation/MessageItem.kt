import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andriybobchuk.messenger.Constants.MESSAGE_CORNER_RADIUS
import com.andriybobchuk.messenger.presentation.components.EmojiPanel
import com.andriybobchuk.messenger.presentation.overlays.ReactionBottomSheet
import com.andriybobchuk.messenger.presentation.formatStatus
import com.andriybobchuk.messenger.presentation.timestampAsTime
import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.Reaction
import com.andriybobchuk.messenger.presentation.components.FetchImageAspectRatio
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import com.andriybobchuk.messenger.ui.theme.LightBlue
import com.andriybobchuk.messenger.ui.theme.LightGrey
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch

@SuppressLint("ServiceCast")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MessageItem(viewModel: ChatViewModel, message: Message, currentUserId: String, isLastMessage: Boolean, onImageClick: (String) -> Unit) {
    val isCurrentUser = message.senderId == currentUserId

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val maxWidth = screenWidth * 0.65f
    val maxHeight = screenHeight * 0.35f

    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var dimensionsLoaded by remember { mutableStateOf(false) }

    val showEmojiPanel = remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Fetch aspect ratio
    FetchImageAspectRatio(message.imageUrl) { ratio ->
        aspectRatio = ratio
        dimensionsLoaded = true
    }

    var imageWidth: Dp = 200.dp
    var imageHeight: Dp

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val gestureModifier = Modifier.combinedClickable(
        onClick = { onImageClick(message.imageUrl) },
        onLongClick = {
            showEmojiPanel.value = !showEmojiPanel.value
            // Trigger haptic feedback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(50)
            }
        },
        onLongClickLabel = "React!"
    )

    Column {
        Row(
            modifier = Modifier
                .padding(top = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
        ) {
            // Show emoji panel if it's toggled on
            val selectedEmoji = viewModel.getUserReaction(message.id, currentUserId)?.emoji
            if (showEmojiPanel.value) {
                EmojiPanel(
                    selectedEmoji = selectedEmoji,
                    onEmojiClick = { emoji ->
                        if (selectedEmoji == emoji) {
                            viewModel.removeReaction(message.id, currentUserId)
                        } else {
                            val reaction = Reaction(userName = currentUserId, emoji = emoji)
                            viewModel.addOrUpdateReaction(message.id, reaction)
                        }
                        showEmojiPanel.value = false
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = maxWidth)
            ) {
                Column(
                    modifier = gestureModifier
                        .clip(RoundedCornerShape(
                            if (isCurrentUser) MESSAGE_CORNER_RADIUS.dp else 4.dp,
                            MESSAGE_CORNER_RADIUS.dp,
                            if (isCurrentUser) 4.dp else MESSAGE_CORNER_RADIUS.dp,
                            MESSAGE_CORNER_RADIUS.dp,
                        ))
                        .background(
                            color = if (isCurrentUser) LightGrey else LightBlue,
                        )
                        .wrapContentWidth(align = Alignment.End)
                ) {
                    if (dimensionsLoaded) {
                        if (aspectRatio > 1) {
                            // Wide image
                            imageWidth = maxWidth
                            imageHeight = (maxWidth / aspectRatio).coerceAtMost(maxHeight)
                        } else {
                            // Narrow image
                            imageHeight = maxHeight
                            imageWidth = (maxHeight * aspectRatio).coerceAtMost(maxWidth)
                        }

                        Box(
                            modifier = Modifier
                                .width(imageWidth)
                                .height(imageHeight)
                                //.clickable { onImageClick(message.imageUrl) }
                        ) {
                            GlideImage(
                                model = message.imageUrl,
                                contentDescription = "Message Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )

                            if (message.caption.isEmpty()) {
                                Text(
                                    text = timestampAsTime(message.timestamp),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.25f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        // Placeholder while dimensions are loading
                        Box(
                            modifier = Modifier
                                .width(maxWidth)
                                .height(200.dp)
                        )
                    }

                    if (message.caption.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.caption,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(4.dp)
                                .widthIn(max = imageWidth - 4.dp)
                                .wrapContentWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = timestampAsTime(message.timestamp),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Emoji reaction box
                if (message.reactions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(LightGrey)
                            .border(1.dp, Color.White, RoundedCornerShape(18.dp))
                            .clickable {
                                coroutineScope.launch {
                                    isSheetOpen = true
                                }
                            }
                            .padding(4.dp)
                    ) {
                        Row {
                            message.reactions.forEach { reaction ->
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
        }
        // Show status below the last message
        if (isLastMessage && isCurrentUser) {
            Text(
                text = formatStatus(message.status),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
            )
        }
    }

    if (isSheetOpen) {
        ReactionBottomSheet(
            viewModel = viewModel,
            reactions = message.reactions,
            sheetState = sheetState,
            onDismiss = {
                coroutineScope.launch {
                    isSheetOpen = false
                }
            }
        )
    }
}


