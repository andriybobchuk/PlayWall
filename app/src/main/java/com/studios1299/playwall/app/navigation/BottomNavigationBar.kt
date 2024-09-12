package com.studios1299.playwall.app.navigation

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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(navController: NavHostController, selectedItemIndex: Int) {
    val items = listOf(
        BottomNavigationItem("Play", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, false),
        BottomNavigationItem("Explore", Icons.Filled.Search, Icons.Outlined.Search, false),
        BottomNavigationItem("Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline, false),
        BottomNavigationItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, false)
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    when (index) {
                        0 -> navController.navigate(Graphs.Main.Screens.play) { popUpTo(Graphs.Main.root) }
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
                }
            )
        }
    }
}