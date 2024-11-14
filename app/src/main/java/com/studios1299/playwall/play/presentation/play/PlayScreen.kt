package com.studios1299.playwall.play.presentation.play

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Cancel
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.data.s3.uriToFile
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Banners
import com.studios1299.playwall.core.presentation.components.Buttons
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
    bottomNavbar: @Composable () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is PlayEvent.ShowError -> {
                if (state.isOnline) {
                    Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "No internet", Toast.LENGTH_LONG).show()
                }
            }
            is PlayEvent.NavigateToChat -> {
                onNavigateToChat(event.friendId)
            }
            PlayEvent.NavigateToDiamonds -> onNavigateToDiamonds()
            PlayEvent.FriendRequestAccepted, PlayEvent.FriendRequestRejected -> {
                Toast.makeText(context, R.string.action_successful, Toast.LENGTH_SHORT).show()
            }
            PlayEvent.WallpaperSent -> {
                Toast.makeText(context, R.string.action_successful, Toast.LENGTH_SHORT).show()
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
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = refreshing),
        onRefresh = {
            refreshing = true
            viewModel.loadFriendsAndRequests(forceUpdate = true)        },
    ) {
        PlayScreen(
            state = state,
            onAction = { action ->
                viewModel.onAction(action)
            },
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

    requestNotificationPermissionWithDexter(LocalContext.current)

    KeyboardAware {
        InviteSheet(
            state = state,
            sheetState = inviteSheetState,
            isSheetOpen = isInviteSheetOpen,
            coroutineScope = coroutineScope,
            onAction = onAction
        )
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
                        Log.e("screen", "Blocking user with friendship ID: $selectedFriendshipId by user $selectedFriendId")
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

    val requestImagePicker = rememberRequestPermissionAndPickImage { uri ->
        coroutineScope.launch {
            val filename = S3Handler.uploadToS3(uriToFile(context, uri)!!, S3Handler.Folder.WALLPAPERS)?:""
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
                    title = "Play",
                    actions = listOf(
                        Toolbars.ToolBarAction(
                            icon = Icons.Default.CheckCircleOutline,
                            contentDescription = "Select friends",
                            onClick = { onAction(PlayAction.OnEnterSelectMode) },
                            enabled = state.isOnline && state.friends.isNotEmpty()
                        ),
                        Toolbars.ToolBarAction(
                            icon = Icons.Default.PersonAddAlt,
                            contentDescription = "Invite friend",
                            onClick = { isInviteSheetOpen.value = true },
                            enabled = state.isOnline
                        )
                    ),
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(combinedPadding)
                    .background(MaterialTheme.colorScheme.background)
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
                            FriendRequestItem(
                                friendRequest = request,
                                onAccept = { onAction(PlayAction.OnAcceptFriendRequest(request.id)) },
                                onReject = { onAction(PlayAction.OnRejectFriendRequest(request.id)) }
                            )
                        }
                    }
                    items(state.friends) { friend ->
                        Log.e("UI", "friend: $friend")
                        if (friend.status != FriendshipStatus.blocked) {
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
                                    Log.e("setting", "Blocking user with friendship ID: ${friend.friendshipId} by user ${friend.id}")
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
                        if(!state.isSelectMode
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
                                    Log.e("setting", "UN-blocking user with friendship ID: ${friend.friendshipId} by user ${friend.id}")
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
                                    onClick = {
                                        //onAction(PlayAction.OnFriendClick(friend.id))
                                    },
                                    isSelectable = false,
                                    isSelected = false
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
    onClick: (friendId: Int) -> Unit,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    isSelected: Boolean = false
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        friend.status == FriendshipStatus.blocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.background
    }

    val textColor = if (friend.status == FriendshipStatus.blocked) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    val senderCaption = if (friend.lastMessageSender == friend.id) "Sent you a wallpaper" else "Got your wallpaper"
    val caption = "$senderCaption • ${timestampAsDateTime(friend.lastMessageDate?:"", LocalContext.current)}"
    val status = if (friend.lastMessageSender != friend.id) MessageStatus.read else friend.lastMessageStatus

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick(friend.id) }
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
                text = friend.email,
                style = MaterialTheme.typography.titleSmall,
                color = textColor
            )
            if (friend.lastMessageDate != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light,
                    color = textColor.copy(0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
                text = stringResource(R.string.sent_a_friend_request, friendRequest.email),
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
                    hint = "jane.doe@gmail.com",
                    title = stringResource(R.string.enter_email),
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

//@Composable
//@OptIn(ExperimentalGlideComposeApi::class)
//fun ImageGrid(
//    innerPadding: PaddingValues,
//    state: PlayState,
//    onWallpaperSelected: (Int) -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(innerPadding)
//    ) {
//        if (!state.isLoading) {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(3),
//                modifier = Modifier.fillMaxSize()
//            ) {
//                items(state.exploreWallpapers.size) { index ->
//                    val photo = state.exploreWallpapers[index]
//                    GlideImage(
//                        model = "",
//                        contentDescription = "wallpaper",
//                        modifier = Modifier
//                            .aspectRatio(1f)
//                            .clickable {
//                                if (state.exploreWallpapers.isNotEmpty()) {
//                                    //downloadAndUploadImage(photo.url)
//                                    onWallpaperSelected(photo.id)
//                                }
//                            }
//                            .padding(1.dp)
//                            .background(MaterialTheme.colorScheme.outline),
//                        contentScale = ContentScale.Crop,
//                    )
//                }
//            }
//        }
//    }
//}

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


//fun downloadAndUploadImage(imageUrl: String) {
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            Log.e("WALL", "Downloading image from URL: $imageUrl")
//            val file = downloadImageFromUrl(imageUrl)
//            Log.e("WALL", "Downloaded image, now uploading...")
//
//            val key = uploadWallpaper(file)
//            Log.e("WALL", "Image uploaded with key: $key")
//
//        } catch (e: Exception) {
//            Log.e("WALL", "Error during download or upload: ${e.message}")
//        }
//    }
//}
//
//fun downloadImageFromUrl(imageUrl: String): File {
//    Log.e("WALL", "Starting image download from: $imageUrl")
//    val url = URL(imageUrl)
//    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
//    connection.doInput = true
//    connection.connect()
//
//    val inputStream: InputStream = connection.inputStream
//    val tempFile = File.createTempFile("downloaded_image", ".jpg") // Save as temp file
//    tempFile.outputStream().use { outputStream ->
//        inputStream.copyTo(outputStream)
//    }
//    inputStream.close()
//
//    Log.e("WALL", "Image successfully downloaded and saved to temp file: ${tempFile.absolutePath}")
//    return tempFile
//}
//
//fun uploadWallpaper(file: File): String {
//    val uuid = UUID.randomUUID().toString() // Use UUID v4 or v7
//    val key = "wallpapers/$uuid v4"
//    Log.e("WALL", "Uploading file with key: $key")
//
//    val putObjectRequest = PutObjectRequest.builder()
//        .bucket("playwall-dev")
//        .key(key)
//        .build()
//
//    S3ClientProvider.s3Client.putObject(putObjectRequest, RequestBody.fromFile(file))
//
//    Log.e("WALL", "File uploaded successfully with key: $key")
//    return key // Return the key for future reference
//}
//
//fun downloadWallpaper(key: String, downloadPath: String) {
//    Log.e("WALL", "Downloading wallpaper with key: $key to path: $downloadPath")
//
//    val getObjectRequest = GetObjectRequest.builder()
//        .bucket("playwall-dev")
//        .key(key)
//        .build()
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        S3ClientProvider.s3Client.getObject(getObjectRequest, Paths.get(downloadPath))
//        Log.e("WALL", "Wallpaper successfully downloaded to: $downloadPath")
//    }
//}
