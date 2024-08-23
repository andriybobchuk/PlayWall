package com.studios1299.vrwallpaper6.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.studios1299.vrwallpaper6.app.MyApp
import com.studios1299.vrwallpaper6.core.presentation.viewModelFactory
import com.studios1299.vrwallpaper6.feature.auth.domain.UserDataValidator
import com.studios1299.vrwallpaper6.feature.auth.presentation.intro.IntroScreenRoot
import com.studios1299.vrwallpaper6.feature.auth.presentation.login.LoginScreenRoot
import com.studios1299.vrwallpaper6.feature.auth.presentation.login.LoginViewModel
import com.studios1299.vrwallpaper6.feature.auth.presentation.register.RegisterScreenRoot
import com.studios1299.vrwallpaper6.feature.auth.presentation.register.RegisterViewModel
import com.studios1299.vrwallpaper6.feature.play.presentation.screens.play.PlayScreenRoot
import com.studios1299.vrwallpaper6.feature.play.presentation.screens.play.PlayViewModel


@Composable
fun NavigationHostLegacy(
    navController: NavHostController,
    isLoggedIn: Boolean,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = if(isLoggedIn) Graphs.Main.root else Graphs.Auth.root
    ) {
        authGraph(navController)
        mainGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = "intro",
        route = "auth"
    ) {
        composable(route = "intro") {
            IntroScreenRoot(
                onSignUpClick = {
                    navController.navigate("register")
                },
                onSignInClick = {
                    navController.navigate("login")
                }
            )
        }
        composable(route = "register") {
            RegisterScreenRoot(
                onSignInClick = {
                    navController.navigate("login") {
                        popUpTo("register") {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                onSuccessfulRegistration = {
                    navController.navigate("login")
                },
                viewModel = viewModel<RegisterViewModel>(
                    factory = viewModelFactory {
                        RegisterViewModel(
                            MyApp.appModule.authRepository,
                            UserDataValidator(MyApp.appModule.emailPatternValidator)
                        )
                    }
                )
            )
        }
        composable("login") {
            LoginScreenRoot(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                },
                onSignUpClick = {
                    navController.navigate("register") {
                        popUpTo("login") {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                viewModel = viewModel<LoginViewModel>(
                    factory = viewModelFactory {
                        LoginViewModel(
                            MyApp.appModule.authRepository,
                            UserDataValidator(MyApp.appModule.emailPatternValidator)
                        )
                    }
                )
            )
        }
    }
}

private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = Graphs.Main.Screens.play,
        route = Graphs.Main.root
    ) {
        composable(Graphs.Main.Screens.play) {
            PlayScreenRoot(
                viewModel = viewModel<PlayViewModel>(
                    factory = viewModelFactory {
                        PlayViewModel(
                            MyApp.appModule.chatRepository
                        )
                    }
                ),
                {

                }
            )
        }
        composable(Graphs.Main.Screens.explore) {
            Text(text = "Explore Tab!")
        }
        composable(Graphs.Main.Screens.create) {
            Text(text = "Create Tab!")
        }
        composable(Graphs.Main.Screens.profile) {
            Text(text = "Profile Tab!")
        }
    }
}


/**
 * Use this navigation host to define your app's screens and pass the ViewModel
 * using my custom [viewModelFactory] with the required dependencies. Add your screens
 * and corresponding ViewModels as needed.
 *
 * Example of adding viewModel:
 *
 * ```
 * MessengerScreen(
 *       viewModel = viewModel<ChatViewModel>(
 *           factory = viewModelFactory {
 *               ChatViewModel(MyApp.appModule.chatRepository)
 *           }
 *       ),
 *       onBackClick = {},
 *       modifier = Modifier.padding(innerPadding)
 * )
 * ```
 */
@Composable
fun NavigationHost(navController: NavHostController, innerPadding: PaddingValues) {
//    NavHost(
//        navController = navController,
//        startDestination = Screens.RegisterScreen
//    ) {
//        composable<Screens.IntroScreen> {
//            IntroScreenRoot(onSignUpClick = { /*TODO*/ }) {
//
//            }
//        }
//        composable<Screens.RegisterScreen> {
//            RegisterScreenRoot(
//                onSignInClick = { /*TODO*/ },
//                onSuccessfulRegistration = { /*TODO*/ },
//                viewModel = viewModel<RegisterViewModel>(
//                    factory = viewModelFactory {
//                        RegisterViewModel()
//                    }
//                ),
//            )
//        }
//        composable<Screens.LoginScreen> {
//           // LoginScreenContent()
//        }
//        composable<Screens.HomeScreen> {
//            HomeScreenContent()
//        }
//        composable<Screens.ChatScreen> {
//            MessengerScreen(
//                viewModel = viewModel<ChatViewModel>(
//                    factory = viewModelFactory {
//                        ChatViewModel(MyApp.appModule.chatRepository)
//                    }
//                ),
//                onBackClick = {},
//                modifier = Modifier.padding(innerPadding)
//            )
//        }
//        composable<Screens.SettingsScreen> {
//            SettingsScreenContent()
//        }
//        composable<Screens.ProfileScreen> {
//            ProfileScreenContent()
//        }
//    }
}

// For test purposes only:
@Composable
fun HomeScreenContent() {
    // Implement your Home screen UI here
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Play Screen")
    }
}

@Composable
fun SettingsScreenContent() {
    // Implement your Settings screen UI here
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Settings Screen")
    }
}

@Composable
fun ProfileScreenContent() {
    // Implement your Profile screen UI here
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Screen")
    }
}

