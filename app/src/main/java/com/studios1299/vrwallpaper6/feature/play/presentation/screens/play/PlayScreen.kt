package com.studios1299.vrwallpaper6.feature.play.presentation.screens.play

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.vrwallpaper6.R
import com.studios1299.vrwallpaper6.core.presentation.ObserveAsEvents
import com.studios1299.vrwallpaper6.core.presentation.components.ToolbarScaffold
import com.studios1299.vrwallpaper6.core.presentation.components.Toolbars

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
                    onReject = { onAction(PlayAction.OnRejectFriendRequest(request.id)) },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp)
                )
            }

            // Add a spacer between the two sections
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Friends Section
            items(state.friends) { friend ->
                FriendItem(
                    friend = friend,
                    onClick = { onAction(PlayAction.OnFriendClick(friend.id)) },
                    modifier = Modifier.padding(vertical = 4.dp)
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (friend.lastMessage != null) {
                Text(
                    text = friend.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (friend.unreadMessages > 0) {
            BadgedBox(
                badge = {},
                modifier = Modifier,
                content = {
                Text(
                    text = friend.unreadMessages.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.background(MaterialTheme.colorScheme.error, CircleShape)
                )
            })
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
            Text(text = friendRequest.name, style = MaterialTheme.typography.titleSmall)
        }
        Row {
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
