package com.studios1299.playwall.core.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.designsystem.ArrowLeftIcon

object Toolbars {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Primary(
        modifier: Modifier = Modifier,
        showBackButton: Boolean = false,
        title: String,
        onBackClick: () -> Unit = {},
        scrollBehavior: TopAppBarScrollBehavior,
        actions: List<ToolBarAction> = emptyList()
    ) {
        TopAppBar(
            title = { Text(text = title) },
            modifier = modifier,
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                if (showBackButton) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = ArrowLeftIcon,
                            contentDescription = stringResource(id = R.string.go_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            },
            actions = {
                actions.take(3).forEach { actionIcon ->
                    IconButton(onClick = actionIcon.onClick) {
                        Icon(
                            imageVector = actionIcon.icon,
                            contentDescription = actionIcon.contentDescription,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        )
    }

    data class ToolBarAction(
        val icon: ImageVector,
        val contentDescription: String,
        val onClick: () -> Unit
    )
}