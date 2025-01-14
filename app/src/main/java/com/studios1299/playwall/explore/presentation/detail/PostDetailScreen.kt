package com.studios1299.playwall.explore.presentation.detail

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhonelinkRing
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Wallpaper
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.studios1299.playwall.R
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.downloadImageToDevice
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Banners
import com.studios1299.playwall.core.presentation.components.ExpandableFab
import com.studios1299.playwall.core.presentation.components.ExpendableFabItem
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.play.presentation.chat.overlays.ImageViewer
import com.studios1299.playwall.play.presentation.chat.util.timestampAsDate
import com.studios1299.playwall.play.presentation.chat.util.timestampAsDateTime
import com.studios1299.playwall.play.presentation.chat.viewmodel.ChatViewModel
import com.studios1299.playwall.play.presentation.chat.viewmodel.MessengerUiState
import com.studios1299.playwall.play.presentation.play.Friend
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PostDetailScreenRoot(
    viewModel: PostDetailViewModel,
    onExit: () -> Unit,
    onOpenWrzutomat: () -> Unit,
) {
    val state = if (viewModel.fromProfile) {
        viewModel.profileState
    } else {
        viewModel.exploreState
    }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.errorMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onOpenWrzutomat()
        }
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
    val coroutineScope = rememberCoroutineScope()

    val isFriendsSheetOpen = remember { mutableStateOf(false) }
    val friendsSheetState = rememberModalBottomSheetState()

    val cropOpen = remember { mutableStateOf(false) }
    val cropState = rememberModalBottomSheetState()

    val currentPhoto = primaryState.wallpapers[pagerState.currentPage]

    if (isFriendsSheetOpen.value) {
        FriendsSelectionBottomSheet(
            state = exploreState,
            isSheetOpen = isFriendsSheetOpen,
            sheetState = friendsSheetState,
            cropState = cropState,
            cropOpen = cropOpen,
            friends = exploreState.friends,
            viewModel = viewModel
        )
    }

    if (cropOpen.value) {
        CropScreen(
            cropOpen = cropOpen,
            state = exploreState,
            onWallpaperSent = { croppedImagePath ->

                // Toast.makeText(context, "Send image to friend", Toast.LENGTH_LONG).show()
            },
            viewModel = viewModel
        )
    }

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
        floatingActionButton = {
            ExpandableFab(
                icon = Icons.Default.Send,
                iconExpanded = Icons.Default.Send,
                text = "Options",
                items = listOf(
                    ExpendableFabItem(
                        icon = Icons.Rounded.Smartphone,
                        text = "Set wallpaper on friend's phone",
                        onClick = { coroutineScope.launch { isFriendsSheetOpen.value = true } }
                    ),
                    ExpendableFabItem(
                        icon = Icons.Rounded.Wallpaper,
                        text = "Set wallpaper on my phone",
                        onClick = {
                            viewModel.setAsWallpaper(primaryState.wallpapers[pagerState.currentPage].fileName, context)
                            Toast.makeText(context,
                                context.getString(R.string.done), Toast.LENGTH_SHORT).show()
                        }
                    ),
                    ExpendableFabItem(
                        icon = Icons.Rounded.Download,
                        text = "Download wallpaper",
                        onClick = {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                Dexter.withContext(context)
                                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    .withListener(object : PermissionListener {
                                        override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse?) {
                                            downloadImageToDevice(MyApp.appModule.context, currentPhoto.fileName) { success ->
                                                Log.e("DownloadDebug", "Saving status: $success")
                                            }
                                        }
                                        override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse?) {
                                            Log.e("DownloadDebug", "Permission Denied: $permissionDeniedResponse")
                                        }
                                        override fun onPermissionRationaleShouldBeShown(permissionRequest: PermissionRequest?, token: PermissionToken?) {
                                            token?.continuePermissionRequest()
                                        }
                                    }).check()
                            } else {
                                downloadImageToDevice(MyApp.appModule.context, currentPhoto.fileName) { success ->
                                    Log.e("DownloadDebug", "Saving status: $success")
                                }
                            }
                        }
                    ),
                    ExpendableFabItem(
                        icon = if (currentPhoto.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        text = if (currentPhoto.isLiked) "Unlike wallpaper" else "Like wallpaper",
                        onClick = { viewModel.onAction(PostDetailAction.ToggleLike(currentPhoto.id)) }
                    )
                )
            )
        },
        bottomBar = {
            if (exploreState.isOnline) {
                BottomAppBar {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "${timestampAsDate(currentPhoto.dateCreated, context)}, ${currentPhoto.savedCount} likes",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder
                ,
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


@Composable
private fun CropScreen(
    cropOpen: MutableState<Boolean>,
    state: ExploreState,
    onWallpaperSent: (String) -> Unit, // Callback after sending the wallpaper
    viewModel: PostDetailViewModel
) {
    val context = LocalContext.current

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultUri = UCrop.getOutput(result.data ?: return@rememberLauncherForActivityResult)
        if (resultUri != null) {
            Log.e("ANDDRII", "cropped img: $resultUri")
            viewModel.sendWallpaperToFriends(listOf(state.selectedFriend), resultUri)

            // Notify parent to close the CropScreen or reset the state
            onWallpaperSent(resultUri.toString())
            cropOpen.value = false
        }
    }

    fun launchUCrop(sourceUri: Uri, screenRatio: Float?) {
        val destinationUri =
            Uri.fromFile(File(context.cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(screenRatio ?: 1f, 1f)
            .withMaxResultSize(4096, 4096)
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(100)
                setFreeStyleCropEnabled(false)
                setHideBottomControls(true)
                setToolbarTitle("Adjust wallpaper")
            })

        cropLauncher.launch(uCrop.getIntent(context))
    }

    // Trigger the UCrop launch whenever the relevant state changes
    LaunchedEffect(state.currentPhotoIndex, state.selectedFriend) {
        val sourceUri = Uri.parse(state.wallpapers[state.currentPhotoIndex].fileName)
        val screenRatio = 1 / (state.selectedFriend.screenRatio ?: 2f)
        launchUCrop(sourceUri, screenRatio)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSelectionBottomSheet(
    state: ExploreState,
    viewModel: PostDetailViewModel,
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    cropOpen: MutableState<Boolean>,
    cropState: SheetState,
    friends: List<Friend>,
) {
    //val context = LocalContext.current

    if (isSheetOpen.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen.value = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Send to a friend",
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        cropOpen.value = true
                                        isSheetOpen.value = false
                                        viewModel.updateExploreState(state.copy(selectedFriend = friend))
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Images.Circle(
                                    model = friend.avatarId
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = friend.nick ?: friend.email)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}


