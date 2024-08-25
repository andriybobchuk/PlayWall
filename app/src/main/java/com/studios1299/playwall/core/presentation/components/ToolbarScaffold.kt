package com.studios1299.playwall.core.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
        topBar = { toolbar() },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share contact"
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Mark as favorite"
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email contact"
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call contact"
                        )
                    }
                }
            )
        }
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