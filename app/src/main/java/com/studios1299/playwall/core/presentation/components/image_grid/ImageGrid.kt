package com.studios1299.playwall.core.presentation.components.image_grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.explore.presentation.explore.ExploreAction
import com.studios1299.playwall.profile.presentation.ProfileAction

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
                            .clickable {
                                if (state.photos.isNotEmpty()) {
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