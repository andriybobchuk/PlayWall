package com.studios1299.playwall.feature.play.presentation.screens.play

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.ToolbarScaffold
import com.studios1299.playwall.core.presentation.components.Toolbars

@Composable
fun PlayScreenRoot(
    viewModel: PlayViewModel,
    onNavigateToChat: (String) -> Unit,
    paddingValues: PaddingValues
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
        navBarPadding = paddingValues
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    state: PlayState,
    onAction: (PlayAction) -> Unit,
    navBarPadding: PaddingValues
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    ToolbarScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        toolbar = {
            Toolbars.Primary(
                title = "Play",
                actions = listOf(
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.Check,
                        contentDescription = "Select",
                        onClick = { /* handle click */ }
                    ),
                    Toolbars.ToolBarAction(
                        icon = Icons.Default.PersonAdd,
                        contentDescription = "Invite friend",
                        onClick = { /* handle click */ }
                    )
                ),
                scrollBehavior = scrollBehavior
            )
        },
        navBarPadding = navBarPadding
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





