package com.studios1299.playwall.feature.play.presentation.play

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Buttons
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.components.TextFields
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.feature.play.presentation.chat.util.rememberRequestPermissionAndPickImage
import com.studios1299.playwall.feature.play.presentation.chat.util.requestNotificationPermissionWithDexter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

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
            PlayEvent.WallpaperSent -> {
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

    var showUnfriendDialog by remember { mutableStateOf(false) }
    var showMuteDialog by remember { mutableStateOf(false) }

    requestNotificationPermissionWithDexter(LocalContext.current)

    InviteSheet(
        state = state,
        sheetState = inviteSheetState,
        isSheetOpen = isInviteSheetOpen,
        coroutineScope = coroutineScope,
        onAction = onAction
    )

    SavedWallpaperSheet(
        isSheetOpen = isSavedWallpaperSheetOpen,
        sheetState = savedWallpaperSheetState,
        state = state,
        onWallpaperSelected = { selectedWallpaper ->
            onAction(PlayAction.OnSelectedFromSaved(selectedWallpaper))
            isSavedWallpaperSheetOpen.value = false
        }
    )

    if (showUnfriendDialog) {
        AlertDialog(
            onDismissRequest = { showUnfriendDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onAction(PlayAction.OnFriendRemove(""))
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
                Text(text = "You are about to mute this friend. They will go to the bottom of the list and neither of you will be able to set wallpapers for each other. Are you sure?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                      //  selectedFriendId?.let { friendId ->
                            onAction(PlayAction.OnFriendMute("friendId"))
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
        onAction(PlayAction.OnSelectedFromGallery(uri))
        onAction(PlayAction.OnExitSelectMode)
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
                            onClick = { onAction(PlayAction.OnExitSelectMode) }
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
                            contentDescription = "Select",
                            onClick = { onAction(PlayAction.OnEnterSelectMode) }
                        ),
                        Toolbars.ToolBarAction(
                            icon = Icons.Default.PersonAddAlt,
                            contentDescription = "Invite friend",
                            onClick = { isInviteSheetOpen.value = true }
                        )
                    ),
                    scrollBehavior = scrollBehavior
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
                                    onAction(PlayAction.OnExitSelectMode)
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(combinedPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                if (!friend.muted) {
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
                        onSwipe = { showMuteDialog = true }
                    )

                    val unfriend = SwipeAction(
                        icon = {
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = "Unfriend",
                                color = Color.White
                            ) },
                        background = Color.Red,
                        isUndo = true,
                        onSwipe = { showUnfriendDialog = true },
                    )

                    SwipeableActionsBox(
                        modifier = Modifier
                            .padding(vertical = 5.dp),
                        startActions = listOf(mute),
                        endActions = listOf(unfriend),
                        swipeThreshold = 120.dp,
                    ) {
                        FriendItem(
                            friend = friend,
                            onClick = { onAction(PlayAction.OnFriendClick(friend.id)) },
                            isSelectable = state.isSelectMode,
                            isSelected = state.selectedFriends.contains(friend.id)
                        )
                    }
                }
            }
            items(state.friends) { friend ->
                if(!state.isSelectMode && friend.muted) {
                    FriendItem(
                        friend = friend,
                        onClick = { onAction(PlayAction.OnFriendClick(friend.id)) },
                        isSelectable = false,
                        isSelected = false
                    )
                }
            }

        }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    onClick: (friendId: String) -> Unit,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    isSelected: Boolean = false
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        friend.muted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.background
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
            .clickable { onClick(friend.id) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectable) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Images.Circle(
            model = friend.avatarId,
            size = 40.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = friend.email,
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
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.unreadMessages.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
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
                    text = stringResource(R.string.invite_a_friend),
                    style = MaterialTheme.typography.titleLarge,

                )
                TextFields.Primary(
                    state = state.friendId,
                    startIcon = Icons.Default.Search,
                    endIcon = null,
                    hint = "jane.doe@gmail.com",
                    title = stringResource(R.string.enter_email),
                    keyboardType = KeyboardType.Email
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
            }
//            LazyColumn {
//                items(state.searchResults) { user ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        GlideImage(
//                            model = user.profilePictureUrl,
//                            contentDescription = user.name,
//                            modifier = Modifier
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .background(MaterialTheme.colorScheme.outline)
//                        )
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Column {
//                            Text(text = user.name, style = MaterialTheme.typography.bodyMedium)
//                            Text(text = user.email, style = MaterialTheme.typography.bodySmall)
//                        }
//                        Spacer(modifier = Modifier.weight(1f))
//                        Button(onClick = {
//                        }) {
//                            Text(text = stringResource(R.string.invite))
//                        }
//                    }
//                }
//            }
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
    onWallpaperSelected: (Int) -> Unit
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
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (state.exploreWallpapers.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_photos_available),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                ImageGrid(PaddingValues(), state, onWallpaperSelected)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun ImageGrid(
    innerPadding: PaddingValues,
    state: PlayState,
    onWallpaperSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        if (!state.isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.exploreWallpapers.size) { index ->
                    val photo = state.exploreWallpapers[index]
                    GlideImage(
                        model = "",
                        contentDescription = "wallpaper",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                if (state.exploreWallpapers.isNotEmpty()) {
                                    //downloadAndUploadImage(photo.url)
                                    onWallpaperSelected(photo.id)
                                }
                            }
                            .padding(1.dp)
                            .background(MaterialTheme.colorScheme.outline),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
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
