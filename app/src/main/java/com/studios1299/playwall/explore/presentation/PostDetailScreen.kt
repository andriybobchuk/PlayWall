package com.studios1299.playwall.explore.presentation

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.explore.presentation.detail.PostDetailAction
import com.studios1299.playwall.explore.presentation.detail.PostDetailEvent
import com.studios1299.playwall.explore.presentation.detail.PostDetailState
import com.studios1299.playwall.explore.presentation.detail.PostDetailViewModel
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.explore.presentation.explore.ExploreViewModel


@Composable
fun PostDetailScreenRoot(
    viewModel: PostDetailViewModel,
    onExit: () -> Unit
) {
    val state = viewModel.state

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PostDetailEvent.ExitImageDetail -> {
                onExit()
            }
            is PostDetailEvent.ShowError -> {
                // Handle error
            }
        }
    }

    PostDetailScreen(
        state = state,
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
    state: PostDetailState,
    viewModel: PostDetailViewModel,
    onSwipe: (Int) -> Unit,
    onExit: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = state.currentPhotoIndex, pageCount = { state.photos.size })
    Log.e("D", state.currentPhotoIndex.toString())


    LaunchedEffect(state.currentPhotoIndex) {
        pagerState.scrollToPage(state.currentPhotoIndex.coerceAtMost(state.photos.size - 1))
    }

    LaunchedEffect(pagerState.currentPage) {
        onSwipe(pagerState.currentPage)
    }


    Scaffold(
        topBar = {
            Toolbars.Primary(
                title = "Explore",
                showBackButton = true,
                onBackClick = onExit,
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        bottomBar = {
            BottomAppBar {
                IconButton(onClick = {

                }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Send to Friend")
                }
                IconButton(onClick = {

                }) {
                    Icon(imageVector = Icons.Default.Wallpaper, contentDescription = "Set as Friend's Wallpaper")
                }
                IconButton(onClick = {

                }) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Add to Favorites")
                }
                IconButton(onClick = {

                }) {
                    Icon(imageVector = Icons.Default.Wallpaper, contentDescription = "Set as My Wallpaper")
                }
            }
        },
        floatingActionButton = {
            if (!state.isLoading) {
                val currentPhoto = state.photos[pagerState.currentPage]
                LikeButton(
                    likeCount = viewModel.getLikeCount(currentPhoto.id),
                    isLiked = viewModel.isLiked(currentPhoto.id)
                ) {
                    viewModel.onAction(PostDetailAction.ToggleLike(currentPhoto.id))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay // Center the FAB in the bottom app bar
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (!state.isLoading) {
                        val photo = state.photos[page]
                        GlideImage(
                            model = photo.url,
                            contentDescription = photo.description,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }


                }
           // }

//
//            CustomTopBar(
//                title = "Explore",
//                onBackClick = onExit
//            )
//
//            if (!state.isLoading) {
//                Log.e("HEY", "state ${state.photos}")
//                val currentPhoto = state.photos[pagerState.currentPage]
//                CustomBottomBar(
//                    likeCount = viewModel.getLikeCount(currentPhoto.id),
//                    isLiked = viewModel.isLiked(currentPhoto.id),
//                    onLikeClick = { viewModel.onAction(PostDetailAction.ToggleLike(currentPhoto.id)) },
//                    onSendToFriendClick = { /* Handle send to friend */ },
//                    onSetAsFriendWallpaperClick = { /* Handle set as friend's wallpaper */ },
//                    onAddToFavoritesClick = { /* Handle add to favorites */ },
//                    onSetAsMyWallpaperClick = { /* Handle set as my wallpaper */ }
//                )
//            }

        }

    }


}



@Composable
fun CustomTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun CustomBottomBar(
    likeCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onSendToFriendClick: () -> Unit,
    onSetAsFriendWallpaperClick: () -> Unit,
    onAddToFavoritesClick: () -> Unit,
    onSetAsMyWallpaperClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LikeButton(likeCount, isLiked, onLikeClick)
        IconButton(onClick = onSendToFriendClick) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Send to Friend")
        }
        IconButton(onClick = onSetAsFriendWallpaperClick) {
            Icon(imageVector = Icons.Default.Wallpaper, contentDescription = "Set as Friend's Wallpaper")
        }
        IconButton(onClick = onAddToFavoritesClick) {
            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Add to Favorites")
        }
        IconButton(onClick = onSetAsMyWallpaperClick) {
            Icon(imageVector = Icons.Default.Wallpaper, contentDescription = "Set as My Wallpaper")
        }
    }
}

@Composable
fun LikeButton(likeCount: Int, isLiked: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(text = likeCount.toString(), style = MaterialTheme.typography.bodyLarge)
    }
}