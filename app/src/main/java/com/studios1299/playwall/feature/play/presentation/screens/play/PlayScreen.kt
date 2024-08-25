package com.studios1299.playwall.feature.play.presentation.screens.play

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.TextFields
import com.studios1299.playwall.core.presentation.components.ToolbarScaffold
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.Reaction
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.screens.chat.ReplyField
import com.studios1299.playwall.feature.play.presentation.screens.chat.viewmodel.ChatViewModel
import com.studios1299.playwall.feature.play.presentation.util.Constants.EMOJI_LIST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun PlayScreenRoot(
    viewModel: PlayViewModel,
    onNavigateToChat: (String) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is PlayEvent.ShowError -> {
                Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
            }
            is PlayEvent.NavigateToChat -> {
                onNavigateToChat(event.friendId)
            }
            PlayEvent.FriendRequestAccepted, PlayEvent.FriendRequestRejected -> {
                Toast.makeText(context, R.string.action_successful, Toast.LENGTH_SHORT).show()
            }
        }
    }

    PlayScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
        },
        bottomNavbar = bottomNavbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    state: PlayState,
    onAction: (PlayAction) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val isInviteSheetOpen = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val inviteSheetState = rememberModalBottomSheetState()
    InviteSheet(
        state = state,
        sheetState = inviteSheetState,
        isSheetOpen = isInviteSheetOpen,
        coroutineScope = coroutineScope
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Toolbars.Primary(
                title = "Play",
                actions = listOf(
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.CheckCircleOutline,
                        contentDescription = "Select",
                        onClick = { /* handle click */ }
                    ),
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.PersonAddAlt,
                        contentDescription = "Invite friend",
                        onClick = { isInviteSheetOpen.value = true }
                    )
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            bottomNavbar()
        }
    ) { combinedPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(combinedPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Friend Requests Section
            items(state.friendRequests) { request ->
                FriendRequestItem(
                    friendRequest = request,
                    onAccept = { onAction(PlayAction.OnAcceptFriendRequest(request.id)) },
                    onReject = { onAction(PlayAction.OnRejectFriendRequest(request.id)) }
                )
            }

            // Friends Section
            items(state.friends) { friend ->
                FriendItem(
                    friend = friend,
                    onClick = { onAction(PlayAction.OnFriendClick(friend.id)) }
                )
            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FriendItem(
    friend: Friend,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (friend.muted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.background
    }

    val textColor = if (friend.muted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = friend.avatar,
            contentDescription = friend.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.titleSmall,
                color = textColor
            )
            if (friend.lastMessage != null) {
                Text(
                    text = friend.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (friend.unreadMessages > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = friend.unreadMessages.toString(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                modifier = Modifier,
                {}
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FriendRequestItem(
    friendRequest: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) // Background color
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = friendRequest.avatar,
            contentDescription = friendRequest.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${friendRequest.name} sent a friend request",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAccept) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onReject) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalGlideComposeApi::class
)
@Composable
fun InviteSheet(
    state: PlayState,
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.
                    padding(bottom = 12.dp),
                    text = "Invite a friend",
                    style = MaterialTheme.typography.titleLarge,

                )
                TextFields.Primary(
                    state = state.friendId,
                    startIcon = Icons.Default.Search,
                    endIcon = null,
                    hint = "jane.doe@gmail.com",
                    title = "Enter email",
                    keyboardType = KeyboardType.Email
                )
            }
            LazyColumn {
                items(state.searchResults) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlideImage(
                            model = user.profilePictureUrl,
                            contentDescription = user.name,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = user.name, style = MaterialTheme.typography.bodyMedium)
                            Text(text = user.email, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            // Handle invite action here
                        }) {
                            Text(text = "Invite")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}





