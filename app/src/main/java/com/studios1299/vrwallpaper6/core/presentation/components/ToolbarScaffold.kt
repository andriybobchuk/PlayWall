package com.studios1299.vrwallpaper6.core.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.max

@Composable
fun ToolbarScaffold(
    modifier: Modifier = Modifier,
    toolbar: @Composable () -> Unit,
    navBarPadding: PaddingValues,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = { toolbar() }
    ) { topBarPadding ->

        val combinedPadding = PaddingValues(
            start = max(
                topBarPadding.calculateStartPadding(LocalLayoutDirection.current),
                navBarPadding.calculateStartPadding(LocalLayoutDirection.current)
            ),
            top = max(
                topBarPadding.calculateTopPadding(),
                navBarPadding.calculateTopPadding()
            ),
            end = max(
                topBarPadding.calculateEndPadding(LocalLayoutDirection.current),
                navBarPadding.calculateEndPadding(LocalLayoutDirection.current)
            ),
            bottom = max(
                topBarPadding.calculateBottomPadding(),
                navBarPadding.calculateBottomPadding()
            )
        )

        content(combinedPadding)
    }
}