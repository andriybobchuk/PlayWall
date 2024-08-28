package com.studios1299.playwall.explore.presentation

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Toolbars

@Composable
fun ExploreScreenRoot(
    viewModel: ExploreViewModel,
    onNavigateToPhotoDetail: (Int) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ExploreEvent.NavigateToPhotoDetail -> {
                onNavigateToPhotoDetail(event.initialPhotoIndex)
            }
            is ExploreEvent.ShowError -> {
                Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
            }
            ExploreEvent.ExitPhotoDetail -> TODO()
        }
    }

    ExploreScreen(
        state = state,
        onAction = { action ->
            viewModel.onAction(action)
        },
        bottomNavbar = bottomNavbar
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ExploreScreen(
    state: ExploreState,
    onAction: (ExploreAction) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Avoid recompositions by only passing minimal and stable references to Scaffold
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Toolbars.Primary(
                title = "Explore",
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = { bottomNavbar() }
    ) { innerPadding ->
        // Box with padding from Scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.photos.isEmpty()) {
                Text(
                    text = "No photos available",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.photos.size) { index ->
                        val photo = state.photos[index]
                        GlideImage(
                            model = photo.url,
                            contentDescription = photo.description,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onAction(ExploreAction.OnPhotoClick(photo.id)) }
                                .background(MaterialTheme.colorScheme.outline),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}




@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun ImageDetailScreen(
    state: ExploreState,
    viewModel: ExploreViewModel,
    onSwipe: (Int) -> Unit,
    onExit: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = state.currentPhotoIndex, pageCount = { state.photos.size })

    LaunchedEffect(pagerState.currentPage) {
        onSwipe(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val photo = state.photos[page]
            GlideImage(
                model = photo.url,
                contentDescription = photo.description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        CustomTopBar(
            title = "Explore",
            onBackClick = onExit
        )

        val currentPhoto = state.photos[pagerState.currentPage]
        CustomBottomBar(
            likeCount = viewModel.getLikeCount(currentPhoto.id),
            isLiked = viewModel.isLiked(currentPhoto.id),
            onLikeClick = { viewModel.toggleLike(currentPhoto.id) },
            onSendToFriendClick = { /* Handle send to friend */ },
            onSetAsFriendWallpaperClick = { /* Handle set as friend's wallpaper */ },
            onAddToFavoritesClick = { /* Handle add to favorites */ },
            onSetAsMyWallpaperClick = { /* Handle set as my wallpaper */ }
        )
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
