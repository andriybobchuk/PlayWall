package com.studios1299.playwall.app.navigation

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.studios1299.playwall.core.presentation.components.PolicyType

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    val hideNavigationBarRoutes = listOf(
        Graphs.Auth.Screens.intro,
        Graphs.Auth.Screens.login,
        Graphs.Auth.Screens.register,
        Graphs.Shared.Screens.policy
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    if (currentRoute in hideNavigationBarRoutes) {
       return
    }
    // todo ("Chnage the routes with classes")
    val items = listOf(
        BottomNavigationItem("Play", Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow, false, 4),
        BottomNavigationItem("Explore", Icons.Filled.Search, Icons.Outlined.Search, false),
        BottomNavigationItem("Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircle, false),
        BottomNavigationItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, false)
    )
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    when (index) {
//                        0 -> navController.navigate(Screens.HomeScreen)
//                        1 -> navController.navigate(Screens.ChatScreen)
//                        2 -> navController.navigate(Screens.SettingsScreen)
//                        3 -> navController.navigate(Screens.ProfileScreen)
                        0 -> navController.navigate("play")
                        1 -> navController.navigate("explore")
                        2 -> navController.navigate("create")
                        3 -> navController.navigate("profile")
                    }
                },
                label = { Text(text = item.title) },
                alwaysShowLabel = false,
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
                            imageVector = if (index == selectedItemIndex) {
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