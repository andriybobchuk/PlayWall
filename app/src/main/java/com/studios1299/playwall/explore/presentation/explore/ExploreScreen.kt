package com.studios1299.playwall.explore.presentation.explore

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGrid
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGridState

@Composable
fun ExploreScreenRoot(
    viewModel: ExploreViewModel,
    onNavigateToPhotoDetail: (String) -> Unit,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreState,
    onAction: (ExploreAction) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
        ImageGrid(innerPadding, state, onAction)
    }
}



