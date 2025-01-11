package com.studios1299.playwall.play.presentation.play

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.studios1299.playwall.R
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.networking.response.friendships.LinkRequestData
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Banners
import com.studios1299.playwall.core.presentation.components.Buttons
import com.studios1299.playwall.core.presentation.components.ExpandableFab
import com.studios1299.playwall.core.presentation.components.ExpendableFabItem
import com.studios1299.playwall.monetization.presentation.components.DiamondsDisplay
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.components.KeyboardAware
import com.studios1299.playwall.core.presentation.components.ShimmerLoadingForFriendsList
import com.studios1299.playwall.core.presentation.components.TextFields
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.explore.presentation.explore.ExploreWallpaper
import com.studios1299.playwall.play.data.model.MessageStatus
import com.studios1299.playwall.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import com.studios1299.playwall.play.presentation.chat.util.requestNotificationPermissionWithDexter
import com.studios1299.playwall.play.presentation.chat.util.timestampAsDateTime
import com.studios1299.playwall.monetization.presentation.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun PlayScreenRoot(
    viewModel: PlayViewModel,
    onNavigateToChat: (Int) -> Unit,
    onNavigateToDiamonds: () -> Unit,
    onNavigateToInviteScreen: (String) -> Unit,
    requesterId: Int?,
    requestCode: Int?,
    onAcceptLinkInvite: () -> Unit,
    onOpenWrzutomat: () -> Unit,
    bottomNavbar: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val state = viewModel.state

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is PlayEvent.ShowError -> {
                if (state.isOnline) {
                    Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
                } else {
                    // I dont want anything in this case as i have the banner to show internet status anyways
                    //Toast.makeText(context, "No internet", Toast.LENGTH_LONG).show()
                }
            }
            is PlayEvent.NavigateToChat -> {
                onNavigateToChat(event.friendId)
            }
            PlayEvent.NavigateToDiamonds -> onNavigateToDiamonds()
            PlayEvent.FriendRequestAccepted -> {
                Toast.makeText(context, R.string.request_accepted, Toast.LENGTH_SHORT).show()
            }
            PlayEvent.FriendRequestRejected  -> {
                Toast.makeText(context, R.string.request_declined, Toast.LENGTH_SHORT).show()
            }
            PlayEvent.WallpaperSent -> {
                Toast.makeText(context, R.string.action_successful, Toast.LENGTH_SHORT).show()
            }
            PlayEvent.PlayScreenShouldBeRestarted -> onAcceptLinkInvite()
            is PlayEvent.InviteLinkReady -> shareText(context, event.inviteLink)
            is PlayEvent.QrInviteReady -> onNavigateToInviteScreen(event.inviteLink)
            PlayEvent.FriendInvited -> {
                Toast.makeText(context,"Friend invited successfully!", Toast.LENGTH_LONG).show()
//                if (state.friends.isEmpty() && state.friendRequests.isEmpty()) {
//                    onOpenWrzutomat()
//                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(1200)
            refreshing = false
        }
    }
    LaunchedEffect(state.friends) {
        if(state.friends.isNotEmpty()) {
            Log.e("PlayScreen", "Recomposed with userAvatar: ${state.friends[0].avatarId}")
        }
    }
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = refreshing),
        onRefresh = {
            refreshing = true
            viewModel.loadFriendsAndRequests(forceUpdate = true)},
    ) {
        PlayScreen(
            state = state,
            onAction = { action ->
                viewModel.onAction(action)
            },
            requesterId = requesterId,
            requestCode = requestCode,
            bottomNavbar = bottomNavbar
        )
    }
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    state: PlayState,
    onAction: (PlayAction) -> Unit,
    requesterId: Int?,
    requestCode: Int?,
    bottomNavbar: @Composable () -> Unit
) {
    val isInviteSheetOpen = remember { mutableStateOf(false) }
    val isSavedWallpaperSheetOpen = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val inviteSheetState = rememberModalBottomSheetState()
    val savedWallpaperSheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    var showUnfriendDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }

    var selectedFriendshipId by remember { mutableStateOf<Int>(-1) }
    var selectedFriendId by remember { mutableStateOf<Int>(-1) }

    //val showInviteDialog = remember { mutableStateOf(state.linkInvite.nick=="") }
    val showInviteDialog = remember { mutableStateOf(false) }
    val inviteLinkShouldBeParsed = remember { mutableStateOf(true) }

    requestNotificationPermissionWithDexter(LocalContext.current)

    if (requesterId != -1 && inviteLinkShouldBeParsed.value) {
        Log.e(
            "PlayScreen",
            "PlayAction.OnReceiveInviteLink was initiated, requesterId = $requesterId"
        )
        onAction(PlayAction.OnReceiveInviteLink(requesterId ?: -1, requestCode ?: -1))
        inviteLinkShouldBeParsed.value = false
    }

    KeyboardAware {
        InviteSheet(
            state = state,
            sheetState = inviteSheetState,
            isSheetOpen = isInviteSheetOpen,
            coroutineScope = coroutineScope,
            onAction = onAction
        )
    }

    LaunchedEffect(state.linkInvite) {
        Log.e("PlayScreen", "link invite data: ${state.linkInvite}")
        if (state.linkInvite.email != "") {
            showInviteDialog.value = true
        }
    }

    SavedWallpaperSheet(
        isSheetOpen = isSavedWallpaperSheetOpen,
        sheetState = savedWallpaperSheetState,
        state = state,
        onCloseSheet = { selectedWallpaper ->
            onAction(PlayAction.OnSelectedFromSaved(selectedWallpaper, state.selectedFriends))
            isSavedWallpaperSheetOpen.value = false
            onAction(PlayAction.OnExitSelectMode)
        },
        onAction = onAction
    )

    if (showUnfriendDialog) {
        AlertDialog(
            onDismissRequest = { showUnfriendDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onAction(PlayAction.OnFriendRemove(selectedFriendshipId))
                    showUnfriendDialog = false
                }) {
                    Text(stringResource(R.string.remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnfriendDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.remove_friend)) },
            text = { Text(stringResource(R.string.remove_friend_alert)) }
        )
    }

    if (showMuteDialog) {
        AlertDialog(
            onDismissRequest = { showMuteDialog = false },
            title = { Text(text = "Mute Friend?") },
            text = {
                Text(stringResource(R.string.remove_friend_alert))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        //  selectedFriendId?.let { friendId ->
                        Log.e(
                            "screen",
                            "Blocking user with friendship ID: $selectedFriendshipId by user $selectedFriendId"
                        )
                        onAction(
                            PlayAction.OnFriendMute(
                                selectedFriendshipId,
                                selectedFriendId
                            )
                        )
                        // }
                        showMuteDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMuteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInviteDialog.value) {
        FriendRequestDialog(
            linkInviteData = state.linkInvite,
            onAccept = {
                onAction(PlayAction.OnCreateFriendshipWithLink(requesterId!!, requestCode!!))
                onAction(PlayAction.ClearInviteState)
                showInviteDialog.value = false
            },
            onReject = {
                onAction(PlayAction.ClearInviteState)
                showInviteDialog.value = false
            }
        )
    }

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        coroutineScope.launch {
            val filename =
                MyApp.appModule.coreRepository.uploadWallpaper(uriToFile(context, uri)!!, S3Handler.Folder.WALLPAPERS.path) ?: ""
            onAction(PlayAction.OnSelectedFromGallery(filename))
            onAction(PlayAction.OnExitSelectMode)
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isPremium = AppState.isPremium.collectAsState().value

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (state.isSelectMode) {
                Toolbars.Primary(
                    title = "Send wallpaper",
                    actions = listOf(
                        Toolbars.ToolBarAction(
                            icon = Icons.Outlined.Cancel,
                            contentDescription = "Cancel",
                            onClick = { onAction(PlayAction.OnExitSelectMode) },
                        )
                    ),
                    scrollBehavior = scrollBehavior
                )
            } else {
                Toolbars.Primary(
                    title = "",
//                    actions = listOf(
//                        Toolbars.ToolBarAction(
//                            icon = Icons.Default.CheckCircleOutline,
//                            contentDescription = "Select friends",
//                            onClick = { onAction(PlayAction.OnEnterSelectMode) },
//                            enabled = state.isOnline && state.friends.isNotEmpty()
//                        ),
//                        Toolbars.ToolBarAction(
//                            icon = Icons.Default.PersonAddAlt,
//                            contentDescription = "Invite friend",
//                            onClick = { isInviteSheetOpen.value = true },
//                            enabled = state.isOnline
//                        )
//                    ),
                    scrollBehavior = scrollBehavior,
                    customContent = {
                        DiamondsDisplay(
                            diamondsCount = AppState.devilCount.collectAsState().value,
                            isPremium = isPremium,
                            onClick = {
                                if (!isPremium) {
                                    onAction(PlayAction.OnNavigateToDiamonds)
                                }
                            }
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            ExpandableFab(
                icon = Icons.Rounded.Add,
                text = "Add friend",
                items = listOf(
//                    ExpendableFabItem(
//                        icon = Icons.Rounded.ContentCopy,
//                        text = "Link",
//                        onClick = {
//                            onAction(PlayAction.RequestInviteLink)
//                        }
//                    ),
                    ExpendableFabItem(
                        icon = Icons.Rounded.QrCode,
                        text = "QR-code",
                        onClick = { onAction(PlayAction.RequestQrInvite) }
                    ),
                    ExpendableFabItem(
                        icon = Icons.Rounded.Person,
                        text = "Username",
                        onClick = { isInviteSheetOpen.value = true }
                    )
                )
            )
        },
        bottomBar = {
            if (state.isSelectMode) {
                BottomAppBar(
                    contentPadding = PaddingValues(8.dp),
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Buttons.Outlined(
                                text = stringResource(R.string.saved_in_playwall),
                                isLoading = false,
                                enabled = state.selectedFriends.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
                                onClick = {
                                    onAction(PlayAction.LoadPhotos)
                                    isSavedWallpaperSheetOpen.value = true
                                }
                            )
                            Buttons.Primary(
                                text = stringResource(R.string.device_gallery),
                                isLoading = false,
                                enabled = state.selectedFriends.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                onClick = { requestImagePicker() }
                            )
                        }
                    }
                )
            } else {
                bottomNavbar()
            }
        }
    ) { combinedPadding ->

        if (!state.isLoading && state.friends.isEmpty() && state.friendRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.primary_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(combinedPadding),

                    ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        Text(
                            text = "No friends yet?",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start by adding your first friend!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                isInviteSheetOpen.value = true
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Add Friend")
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.minimal_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(combinedPadding),
                //.background(MaterialTheme.colorScheme.background)
            ) {
                if (!state.isOnline) {
                    item {
                        Banners.OfflineStatus()
                    }
                }
                if (state.isLoading) {
                    item {
                        ShimmerLoadingForFriendsList(Modifier)
                    }
                } else {
                    if (!state.isSelectMode) {
                        items(state.friendRequests) { request ->
                            if (request.requesterId != request.id) {
                                FriendRequestItem(
                                    friendRequest = request,
                                    onAccept = { onAction(PlayAction.OnAcceptFriendRequest(request.id)) },
                                    onReject = { onAction(PlayAction.OnRejectFriendRequest(request.id)) }
                                )
                            }
                        }
                    }
                    items(state.friends) { friend ->
                        if (friend.status != FriendshipStatus.blocked && friend.status != FriendshipStatus.pending) {
                            // Swipe actions (mute and unfriend) will only be available when online
                            val mute = SwipeAction(
                                icon = {
                                    Icon(
                                        modifier = Modifier.padding(end = 16.dp),
                                        imageVector = Icons.Default.VolumeOff,
                                        contentDescription = "Mute",
                                        tint = Color.White
                                    )
                                },
                                background = MaterialTheme.colorScheme.primary,
                                onSwipe = {
                                    Log.e(
                                        "setting",
                                        "Blocking user with friendship ID: ${friend.friendshipId} by user ${friend.id}"
                                    )
                                    selectedFriendshipId = friend.friendshipId
                                    selectedFriendId = friend.id
                                    showMuteDialog = true
                                }
                            )
                            val unfriend = SwipeAction(
                                icon = {
                                    Text(
                                        modifier = Modifier.padding(start = 16.dp),
                                        text = "Unfriend",
                                        color = Color.White
                                    )
                                },
                                background = Color.Red,
                                isUndo = true,
                                onSwipe = {
                                    selectedFriendshipId = friend.friendshipId
                                    selectedFriendId = friend.id
                                    showUnfriendDialog = true
                                }
                            )

                            SwipeableActionsBox(
                                modifier = Modifier.padding(vertical = 5.dp),
                                startActions = if (state.isOnline) listOf(mute) else emptyList(), // Disable swipe if offline
                                endActions = if (state.isOnline) listOf(unfriend) else emptyList(), // Disable swipe if offline
                                swipeThreshold = 120.dp,
                            ) {
                                FriendItem(
                                    friend = friend,
                                    onClick = {
                                        onAction(PlayAction.OnFriendClick(friend.id))
                                    },
                                    isSelectable = state.isSelectMode,
                                    isSelected = state.selectedFriends.contains(friend.id)
                                )
                            }
                        }
                    }

                    items(state.friends) { friend ->
                        if (!state.isSelectMode
                            && friend.status == FriendshipStatus.blocked
                        ) {
                            val unmute = SwipeAction(
                                icon = {
                                    Icon(
                                        modifier = Modifier.padding(end = 16.dp),
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Unmute",
                                        tint = Color.White
                                    )
                                },
                                background = MaterialTheme.colorScheme.primary,
                                onSwipe = {
                                    Log.e(
                                        "setting",
                                        "UN-blocking user with friendship ID: ${friend.friendshipId} by user ${friend.id}"
                                    )
                                    onAction(
                                        PlayAction.OnFriendUnMute(
                                            friend.friendshipId,
                                            friend.id
                                        )
                                    )
                                })

                            val unfriend = SwipeAction(
                                icon = {
                                    Text(
                                        modifier = Modifier.padding(start = 16.dp),
                                        text = "Unfriend",
                                        color = Color.White
                                    )
                                },
                                background = Color.Red,
                                isUndo = true,
                                onSwipe = {
                                    selectedFriendshipId = friend.friendshipId
                                    selectedFriendId = friend.id
                                    showUnfriendDialog = true
                                },
                            )
                            SwipeableActionsBox(
                                modifier = Modifier
                                    .padding(vertical = 5.dp),
                                startActions = listOf(unmute),
                                endActions = listOf(unfriend),
                                swipeThreshold = 120.dp,
                            ) {
                                FriendItem(
                                    friend = friend,
                                    isMuted = true,
                                    onClick = {
                                        //onAction(PlayAction.OnFriendClick(friend.id))
                                    },
                                    isSelectable = false,
                                    isSelected = false
                                )
                            }
                        }
                    }

                    items(state.friends) { friend ->
                        if (!state.isSelectMode
                            && friend.status == FriendshipStatus.pending
                        ) {
                            MyFriendRequestItem(
                                friendRequest = friend,
                                onCancel = { onAction(PlayAction.OnRejectFriendRequest(friend.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    isMuted: Boolean = false,
    onClick: (friendId: Int) -> Unit,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    isSelected: Boolean = false
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        friend.status == FriendshipStatus.blocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        friend.status == FriendshipStatus.pending -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val textColor = if (friend.status == FriendshipStatus.blocked) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    var senderCaption = ""
    if (friend.lastMessageSender != null) {
         if (friend.lastMessageSender == friend.id) senderCaption = "Sent you a wallpaper  • " else senderCaption = "Got your wallpaper • "
    }
    val caption = if (isMuted) {
        "This user is muted"
    } else if (friend.status == FriendshipStatus.pending) {
        "Friend request sent"
    } else {
        "$senderCaption${timestampAsDateTime(friend.lastMessageDate?:"", LocalContext.current)}"
    }
    val status = if (friend.lastMessageSender != friend.id) MessageStatus.read else friend.lastMessageStatus

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable {

                //if(friend.requesterId != friend.id)

                onClick(friend.id)
            }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Images.Circle(
            model = friend.avatarId,
            size = 47.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = friend.nick?:friend.email,
                style = MaterialTheme.typography.titleSmall,
                color = textColor
            )
            //if (friend.lastMessageDate != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light,
                    color = textColor.copy(0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            //}
        }
//        if (status == MessageStatus.unread) {
//            Box(
//                modifier = Modifier
//                    .size(10.dp)
//                    .background(
//                        color = MaterialTheme.colorScheme.primary,
//                        shape = CircleShape
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//            }
//        }
        Spacer(modifier = Modifier.width(6.dp))
        if (isSelectable) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

    }
}

@Composable
fun FriendRequestItem(
    friendRequest: Friend,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Images.Circle(model = friendRequest.avatarId)
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.sent_a_friend_request, friendRequest.nick?:friendRequest.email),
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
                    contentDescription = stringResource(R.string.accept),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onReject) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.reject),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MyFriendRequestItem(
    friendRequest: Friend,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Images.Circle(model = friendRequest.avatarId)
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Requested ${friendRequest.nick?:friendRequest.email}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.reject),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
)
@Composable
fun InviteSheet(
    state: PlayState,
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    onAction: (PlayAction) -> Unit,
) {
    val focusState = remember { mutableStateOf(false) }

    if (isSheetOpen.value) {

        LaunchedEffect(focusState.value) {
            if (focusState.value) {
                sheetState.expand()
            } else {
                sheetState.partialExpand()
            }
        }

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
                    text = stringResource(R.string.invite_a_friend),
                    style = MaterialTheme.typography.titleLarge,

                )
                TextFields.Primary(
                    state = state.friendId,
                    startIcon = Icons.Default.Search,
                    endIcon = null,
                    hint = "john.smith1299",
                    title = stringResource(R.string.enter_username),
                    keyboardType = KeyboardType.Email,
                    onFocusChanged = { isFocused -> focusState.value = isFocused }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Buttons.Primary(
                    text = "Invite",
                    isLoading = state.isLoading,
                    onClick = {
                        onAction(PlayAction.OnInviteFriend(userEmail = state.friendId.text.toString()))
                        state.friendId.clearText()
                        isSheetOpen.value = false
                    }
                )
                if (focusState.value) {
                    Spacer(modifier = Modifier.height(300.dp))
                }
            }
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedWallpaperSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    state: PlayState,
    onCloseSheet: (String) -> Unit,
    onAction: (PlayAction) -> Unit,
) {
    if (isSheetOpen.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen.value = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.select_a_wallpaper),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )

                Box(Modifier.fillMaxWidth()) {
                    if (state.isLoading) {
                        ShimmerLoadingForFriendsList(modifier = Modifier.fillMaxSize())
                    } else if (state.exploreWallpapers.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_photos_available),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .padding()
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                ) {
                    items(state.exploreWallpapers.chunked(3)) { photoRow ->
                        PhotoGridRow(
                            photoRow,
                            state,
                            onAction,
                            onCloseSheet
                        )
                    }
                }
                //ImageGrid(PaddingValues(), state, onWallpaperSelected)

            }
        }
    }
}

@Composable
fun PhotoGridRow(
    exploreWallpapers: List<ExploreWallpaper>,
    state: PlayState,
    onAction: (PlayAction) -> Unit,
    onCloseSheet: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        exploreWallpapers.forEach { photo ->
            Images.Square(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                model = photo.fileName,
                onClick = {
                    Log.e("PhotoGridRow", "Photo clicked with id: ${photo.fileName}")
                    onCloseSheet(photo.fileName)
                }
            )
        }

        repeat(3 - exploreWallpapers.size) {
            Log.e("PhotoGridRow", "Adding empty space to fill remaining grid slots.")
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    Log.e("PhotoGridRow", "Finished rendering PhotoGridRow.")
}

@Composable
fun FriendRequestDialog(
    linkInviteData: LinkRequestData?,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    if (linkInviteData != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Friend Request") },
            text = { Text("${linkInviteData.nick} requests to be your friend. Do you wish to accept?") },
            confirmButton = {
                Button(onClick = onAccept) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = onReject) {
                    Text("Reject")
                }
            }
        )
    }
}

fun shareText(context: Context, text: String, subject: String = "Invite Link") {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        type = "text/plain"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val shareIntent = Intent.createChooser(intent, null)
    context.startActivity(shareIntent)
}



