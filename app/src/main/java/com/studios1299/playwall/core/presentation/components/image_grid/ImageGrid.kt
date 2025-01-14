package com.studios1299.playwall.core.presentation.components.image_grid

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.Images
import com.studios1299.playwall.core.presentation.components.ShimmerLoadingForWallpaperGrid
import com.studios1299.playwall.explore.presentation.detail.LikeButton
import com.studios1299.playwall.explore.presentation.detail.PostDetailAction
import com.studios1299.playwall.explore.presentation.explore.ExploreAction
import com.studios1299.playwall.explore.presentation.explore.ExploreState
import com.studios1299.playwall.explore.presentation.explore.ExploreViewModel

@Composable
fun ImageGrid(
    viewModel: ExploreViewModel,
    state: ImageGridState,
    onAction: (ExploreAction) -> Unit,
    exploreState: ExploreState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (state.wallpapers.isEmpty()) {
            ShimmerLoadingForWallpaperGrid()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.wallpapers.size) { index ->
                    val photo = state.wallpapers[index]

                    if (index >= state.wallpapers.size - 1 && !viewModel.paginationState.endReached && !viewModel.paginationState.isLoading) {
                        viewModel.loadPhotos()
                    }

                    Box {
                        Images.Square(
                            modifier = Modifier
                                .aspectRatio(1f),
                            model = photo.fileName,
                            onClick = {
                                Log.e("ImageGrid", "Clicked on photo with id ${photo.id}")
                                if (state.wallpapers.isNotEmpty()) {
                                    onAction(ExploreAction.OnPhotoClick(photo.id))
                                }
                            }
                        )
                        if (exploreState.isOnline) {
                            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                                LikeButton(
                                    likeCount = photo.savedCount,
                                    isLiked = photo.isLiked,
                                    onClick = { onAction(ExploreAction.ToggleLike(photo.id)) },
                                    modifier = Modifier,
                                    iconColor = Color.White,
                                    counterVisible = false
                                )
                            }
                        }
                    }
                }
                item {
                    if (viewModel.paginationState.isLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}