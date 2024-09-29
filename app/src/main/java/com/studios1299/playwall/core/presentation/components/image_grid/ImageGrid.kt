package com.studios1299.playwall.core.presentation.components.image_grid

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.R
import com.studios1299.playwall.explore.presentation.explore.ExploreAction

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun ImageGrid(
    innerPadding: PaddingValues,
    state: ImageGridState,
    onAction: (ExploreAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.wallpapers.isEmpty()) {
            Text(
                text = "No photos available",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.wallpapers.size) { index ->
                    val photo = state.wallpapers[index]
                    GlideImage(
                        model = photo.fileName,
                        contentDescription = "wallpaper",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                Log.e("ImageGrid", "Clicked on photo with id ${photo.id}")
                                if (state.wallpapers.isNotEmpty()) {
                                    onAction(ExploreAction.OnPhotoClick(photo.id))
                                }
                            }
                            .background(MaterialTheme.colorScheme.outline),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}