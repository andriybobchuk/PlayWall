package com.studios1299.playwall.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_BLACK
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_MAGENTA
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_PURPLE

@Composable
fun BottomNavigationBar(navController: NavHostController, selectedItemIndex: Int) {
    val items = listOf(
        BottomNavigationItem(
            "Play",
            Icons.Filled.ChatBubble,
            Icons.Outlined.ChatBubbleOutline,
            false
        ),
        BottomNavigationItem("Explore", Icons.Filled.Search, Icons.Outlined.Search, false),
        BottomNavigationItem(
            "Create",
            Icons.Filled.AddCircle,
            Icons.Outlined.AddCircleOutline,
            false
        ),
        BottomNavigationItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, false)
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                // Apply gradient background
                Brush.radialGradient(
                    colors = listOf(

                        Color(0xFFFF006E),
                        Color(0xFF4A1C59),
                        ZEDGE_BLACK,


//                        Color(0xFFFF006E), // Bright Pink (peak color)
//                        Color(0xFF4A1C59), // Deep Magenta
//                        Color(0xFF000000)  // Black (edges)
                    ),
                    center = Offset(
                        x = 500f, // Center horizontally
                        y = 400f  // Shift the peak to the bottom center
                    ),
                    radius = 400f // Adjust to control the spread of the gradient
                )
            ),
        containerColor = Color.Transparent, // Make the container transparent
        contentColor = Color.White
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    when (index) {
                        0 -> navController.navigate("${Graphs.Main.Screens.play}/${-1}/${-1}") {
                            popUpTo(
                                Graphs.Main.root
                            )
                        }

                        1 -> navController.navigate(Graphs.Main.Screens.explore) { popUpTo(Graphs.Main.root) }
                        2 -> navController.navigate(Graphs.Main.Screens.create) { popUpTo(Graphs.Main.root) }
                        3 -> navController.navigate(Graphs.Main.Screens.profile) { popUpTo(Graphs.Main.root) }
                    }
                },
                label = { Text(text = item.title) },
                alwaysShowLabel = true,
                icon = {
                    BadgedBox(
                        badge = {
                            when {
                                item.badgeCount != null -> Badge {
                                    Text(text = item.badgeCount.toString())
                                }

                                item.hasNews -> Badge()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedItemIndex == index) {
                                item.selectedIcon
                            } else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    }
                },
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}