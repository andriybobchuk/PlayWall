package com.studios1299.playwall.explore.presentation.explore


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.studios1299.playwall.core.presentation.ObserveAsEvents
import com.studios1299.playwall.core.presentation.components.Banners
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.core.presentation.components.image_grid.ImageGrid
import kotlinx.coroutines.delay

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
                Log.e("Rerouting", "rereouting... in root")
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
        Column(modifier = Modifier.padding(innerPadding)) {

            if (!state.isOnline) {
                Banners.OfflineStatus()
            }

            var refreshing by remember { mutableStateOf(false) }
            LaunchedEffect(refreshing) {
                if (refreshing) {
                    delay(1600)
                    refreshing = false
                }
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = refreshing),
                onRefresh = {
                    refreshing = true
                    onAction(ExploreAction.LoadPhotos(true))
                },
            ) {
                ImageGrid(state, onAction, state)
            }
        }
    }
}


