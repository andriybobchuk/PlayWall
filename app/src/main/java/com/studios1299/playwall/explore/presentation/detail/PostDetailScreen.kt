package com.studios1299.playwall.explore.presentation.detail

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Banners
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.play.presentation.chat.util.timestampAsDateTime
import com.studios1299.playwall.play.presentation.play.Friend
import kotlinx.coroutines.launch

@Composable
fun PostDetailScreenRoot(
    viewModel: PostDetailViewModel,
    onExit: () -> Unit
) {
    val state = if (viewModel.fromProfile) {
        viewModel.profileState
    } else {
        viewModel.exploreState
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PostDetailEvent.ExitImageDetail -> {
                onExit()
            }
            is PostDetailEvent.ShowError -> {
            }
        }
    }

    PostDetailScreen(
        exploreState = viewModel.exploreState,
        primaryState = state,
        viewModel = viewModel,
        onSwipe = { newIndex ->
            viewModel.onAction(PostDetailAction.OnSwipePhoto(newIndex))
        },
        onExit = {
            viewModel.onAction(PostDetailAction.ExitImageDetail)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun PostDetailScreen(
    exploreState: ExploreState,
    primaryState: ImageGridState,
    viewModel: PostDetailViewModel,
    onSwipe: (Int) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    Log.e("PostDetailScreen", "state.currentPhotoIndex: ${primaryState.currentPhotoIndex}, wallpapers size: ${primaryState.wallpapers.size}")
    val pagerState = rememberPagerState(initialPage = primaryState.currentPhotoIndex, pageCount = { primaryState.wallpapers.size })

    val isFriendsSheetOpen = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val friendsSheetState = rememberModalBottomSheetState()

    FriendsSelectionBottomSheet(
        isSheetOpen = isFriendsSheetOpen,
        sheetState = friendsSheetState,
        friends = exploreState.friends,
        onFriendsSelected = { selectedFriends ->
            viewModel.sendWallpaperToFriends(
                friends = selectedFriends,
                fileName = primaryState.wallpapers[pagerState.currentPage].fileName
            )
        }
    )

    LaunchedEffect(primaryState.currentPhotoIndex) {
        pagerState.scrollToPage(primaryState.currentPhotoIndex.coerceAtMost(primaryState.wallpapers.size - 1))
    }

    LaunchedEffect(pagerState.currentPage) {
        onSwipe(pagerState.currentPage)
    }
    Scaffold(
        topBar = {
            Toolbars.Primary(
                title = "Wallpaper",
                showBackButton = true,
                onBackClick = onExit,
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        bottomBar = {
            if (exploreState.isOnline) {
                BottomAppBar {
                    Button(
                        modifier = Modifier.padding(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        onClick = {
                            coroutineScope.launch { isFriendsSheetOpen.value = true }
                        }) {
                        Text(text = stringResource(R.string.set_as_friend_s), color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        modifier = Modifier.padding(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        onClick = {
                            viewModel.setAsWallpaper(primaryState.wallpapers[pagerState.currentPage].fileName, context)
                            Toast.makeText(context,
                                context.getString(R.string.done), Toast.LENGTH_SHORT).show()
                        }) {
                        Text(text = stringResource(R.string.set_as_mine), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        floatingActionButton = {
            if (!exploreState.isLoading && exploreState.isOnline) {
                val currentPhoto = primaryState.wallpapers[pagerState.currentPage]
                LikeButton(
                    likeCount = currentPhoto.savedCount,
                    isLiked = currentPhoto.isLiked,
                    onClick = {
                        viewModel.onAction(PostDetailAction.ToggleLike(currentPhoto.id))
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            if (!exploreState.isOnline) {
                Banners.OfflineStatus()
            }
            if (exploreState.isLoading) {
                CircularProgressIndicator()
            }
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (!exploreState.isLoading) {
                    val photo = primaryState.wallpapers[page]
                    Box {
                        GlideImage(
                            model = photo.fileName,
                            contentDescription = "wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "#${photo.type}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.BottomEnd)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = timestampAsDateTime(photo.dateCreated, context),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LikeButton(
    likeCount: Int,
    isLiked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shadow: Boolean = false,
    iconColor: Color? = null,
    counterVisible: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClick) {
            Icon(
                modifier = (if (shadow) Modifier.shadow(2.dp) else Modifier),
                imageVector = if (isLiked) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.error else iconColor ?: MaterialTheme.colorScheme.onSurface // Apply icon color or default
            )
        }
        if (counterVisible) {
            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = iconColor ?: MaterialTheme.colorScheme.onSurface, // Apply the same color to the text
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSelectionBottomSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    friends: List<Friend>,
    onFriendsSelected: (List<Friend>) -> Unit
) {
    // Track the selection state of each friend with a mutable set of friend IDs (Int)
    val selectedFriends = remember { mutableStateListOf<Int>() }
    val context = LocalContext.current

    // Reset selected friends each time the sheet is opened
    if (isSheetOpen.value) {
        selectedFriends.clear()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen.value = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Send to friends",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )

                if (friends.isEmpty()) {
                    Text(
                        text = "Looks like you have no friends",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(friends) { friend ->
                            val isSelected = selectedFriends.contains(friend.id.toInt())

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedFriends.remove(friend.id.toInt())
                                        } else {
                                            selectedFriends.add(friend.id.toInt())
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Images.Circle(
                                    model = friend.avatarId
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = friend.email)
                                Spacer(modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedFriends.add(friend.id.toInt())
                                        } else {
                                            selectedFriends.remove(friend.id.toInt())
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        // Filter selected friends and pass to the callback
                        val selectedFriendList = friends.filter { friend ->
                            selectedFriends.contains(friend.id.toInt())
                        }
                        if (selectedFriendList.isNotEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.sent_to_friend),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "No friends were selected.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        onFriendsSelected(selectedFriendList)
                        isSheetOpen.value = false
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text(text = "Send wallpaper")
                }
            }
        }
    }
}



//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FriendsSelectionBottomSheet(
//    isSheetOpen: MutableState<Boolean>,
//    sheetState: SheetState,
//    friends: List<Friend>,
//    onFriendsSelected: (List<Friend>) -> Unit
//) {
//    if (isSheetOpen.value) {
//        ModalBottomSheet(
//            sheetState = sheetState,
//            onDismissRequest = { isSheetOpen.value = false }
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                Text(
//                    text = "Send to friends",
//                    style = MaterialTheme.typography.titleLarge,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
//                )
//
//                if (friends.isEmpty()) {
//                    Text(
//                        text = "Looks like you have no friends",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        textAlign = TextAlign.Center
//                    )
//                } else {
//                    LazyColumn {
//                        items(friends) { friend ->
//                            var isSelected by remember { mutableStateOf(false) }
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable { isSelected = !isSelected }
//                                    .padding(8.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Checkbox(
//                                    checked = isSelected,
//                                    onCheckedChange = { isSelected = it }
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(text = friend.email)
//                            }
//                        }
//                    }
//                }
//
//                Button(
//                    onClick = {
//                        // Pass selected friends
//                        onFriendsSelected(friends.filter { /* logic for selection */ true })
//                        isSheetOpen.value = false
//                    },
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .padding(16.dp)
//                ) {
//                    Text(text = "Send wallpaper")
//                }
//            }
//        }
//    }
//}
//

