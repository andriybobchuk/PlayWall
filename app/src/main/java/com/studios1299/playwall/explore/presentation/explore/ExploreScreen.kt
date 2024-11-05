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
import androidx.compose.runtime.collectAsState
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
import com.studios1299.playwall.feature.play.presentation.play.PlayAction
import com.studios1299.playwall.monetization.presentation.AppState
import com.studios1299.playwall.monetization.presentation.components.DiamondsDisplay
import kotlinx.coroutines.delay

@Composable
fun ExploreScreenRoot(
    viewModel: ExploreViewModel,
    onNavigateToPhotoDetail: (Int) -> Unit,
    onNavigateToDiamonds: () -> Unit,
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
        viewModel = viewModel,
        state = state,
        onNavigateToDiamonds = onNavigateToDiamonds,
        onAction = { action ->
            viewModel.onAction(action)
        },
        bottomNavbar = bottomNavbar
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    state: ExploreState,
    onNavigateToDiamonds: () -> Unit,
    onAction: (ExploreAction) -> Unit,
    bottomNavbar: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isPremium = AppState.isPremium.collectAsState().value
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Toolbars.Primary(
                title = "Explore",
                scrollBehavior = scrollBehavior,
                customContent = {
                    DiamondsDisplay(
                        diamondsCount = AppState.devilCount.collectAsState().value,
                        isPremium = isPremium,
                        onClick = {
                            if (!isPremium) {
                                onNavigateToDiamonds()
                            }
                        }
                    )
                }
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
                    delay(1200)
                    refreshing = false
                }
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = refreshing),
                onRefresh = {
                    refreshing = true
                    viewModel.paginationState = viewModel.paginationState.copy(page = 0)
                    Log.e("ExploreScreen", "Screen refresh initiated. Reloading page to: ${viewModel.paginationState.page}")
                    onAction(ExploreAction.LoadPhotos(true))
                },
            ) {
                ImageGrid(viewModel, state, onAction, state)
            }
        }
    }
}


