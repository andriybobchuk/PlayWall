package com.studios1299.playwall.core.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
        actions: List<ToolBarAction> = emptyList(),
        customContent: @Composable (() -> Unit)? = null
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
                customContent?.invoke()
                actions.take(3).forEach { actionIcon ->
                    IconButton(
                        onClick = actionIcon.onClick,
                        enabled = actionIcon.enabled,
                    ) {
                        Icon(
                            imageVector = actionIcon.icon,
                            contentDescription = actionIcon.contentDescription,
                        )
                    }
                }
            },
//            colors = TopAppBarColors(
//                containerColor = MaterialTheme.colorScheme.background,
//                scrolledContainerColor = MaterialTheme.colorScheme.primary.copy(0.1f),
//                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
//                titleContentColor = MaterialTheme.colorScheme.onBackground,
//                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
//            )
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WithMenu(
        modifier: Modifier = Modifier,
        showBackButton: Boolean = false,
        title: String,
        onBackClick: () -> Unit = {},
        scrollBehavior: TopAppBarScrollBehavior,
        actions: List<ToolBarAction> = emptyList(),
        customContent: @Composable (() -> Unit)? = null
    ) {
        var isMenuExpanded by remember { mutableStateOf(false) }

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
                customContent?.invoke()
                IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    actions.forEach { action ->
                        DropdownMenuItem(
                            onClick = {
                                action.onClick()
                                isMenuExpanded = false
                            },
                            enabled = action.enabled,
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = action.contentDescription,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = action.contentDescription)
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    data class ToolBarAction(
        val icon: ImageVector,
        val contentDescription: String,
        val onClick: () -> Unit,
        val enabled: Boolean = true
    )
}