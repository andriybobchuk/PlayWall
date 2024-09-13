package com.studios1299.playwall.app.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.presentation.viewModelFactory
import com.studios1299.playwall.auth.data.UserDataValidator
import com.studios1299.playwall.auth.presentation.intro.IntroScreenRoot
import com.studios1299.playwall.auth.presentation.login.LoginScreenRoot
import com.studios1299.playwall.auth.presentation.login.LoginViewModel
import com.studios1299.playwall.auth.presentation.register.RegisterScreenRoot
import com.studios1299.playwall.auth.presentation.register.RegisterViewModel
import com.studios1299.playwall.core.presentation.components.WebViewScreen
import com.studios1299.playwall.core.presentation.components.WebContent
import com.studios1299.playwall.create.presentation.CreateScreenRoot
import com.studios1299.playwall.create.presentation.CreateViewModel
import com.studios1299.playwall.explore.presentation.explore.ExploreScreenRoot
import com.studios1299.playwall.explore.presentation.explore.ExploreViewModel
import com.studios1299.playwall.explore.presentation.detail.PostDetailScreenRoot
import com.studios1299.playwall.explore.presentation.detail.PostDetailViewModel
import com.studios1299.playwall.feature.play.presentation.chat.MessengerScreen
import com.studios1299.playwall.feature.play.presentation.chat.viewmodel.ChatViewModel
import com.studios1299.playwall.feature.play.presentation.play.PlayScreenRoot
import com.studios1299.playwall.feature.play.presentation.play.PlayViewModel
import com.studios1299.playwall.profile.presentation.ProfileDestination
import com.studios1299.playwall.profile.presentation.ProfileScreenRoot
import com.studios1299.playwall.profile.presentation.ProfileViewModel


@Composable
fun NavigationHostLegacy(
    isLoggedIn: Boolean,
) {
    val navController = rememberNavController()
    val selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    NavHost(
        navController = navController,
        startDestination = if(isLoggedIn) Graphs.Main.root else Graphs.Auth.root
    ) {
        authGraph(navController)
        mainGraph(navController, selectedItemIndex)
        sharedGraph(navController)
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
                },
                onTermsClick = {
                    navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.TOS.name))
                },
                onPrivacyClick = {
                    navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.PP.name))
                },
                onContentPolicyClick = {
                    navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.CP.name))
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
                    //navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.TIKTOK.name))
                },
                onSuccessfulRegistration = {
                    navController.navigate("main") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                },
                onTermsOfServiceClick = {
                    navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.TOS.name))
                },
                onPrivacyPolicyClick = {
                    navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.PP.name))
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
                   // navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.TIKTOK.name))
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

private fun NavGraphBuilder.mainGraph(navController: NavHostController, selectedItemIndex: Int) {
    navigation(
        startDestination = Graphs.Main.Screens.play,
        route = Graphs.Main.root
    ) {
        composable(Graphs.Main.Screens.play) {
            PlayScreenRoot(
                viewModel = viewModel<PlayViewModel>(
                    factory = viewModelFactory {
                        PlayViewModel(
                            MyApp.appModule.coreRepository
                        )
                    }
                ),
                {
                    navController.navigate(Graphs.Main.Screens.play_chat)
                },
                bottomNavbar = { BottomNavigationBar(navController, 0) }
            )
        }
        composable(Graphs.Main.Screens.explore) {
            ExploreScreenRoot(
                viewModel = viewModel<ExploreViewModel>(
                    factory = viewModelFactory {
                        ExploreViewModel(
                            MyApp.appModule.coreRepository
                        )
                    }
                ),
                onNavigateToPhotoDetail = { selectedPhoto ->
                    navController.navigate("${Graphs.Main.Screens.explore_image}/${selectedPhoto}")
                },
                bottomNavbar = { BottomNavigationBar(
                    navController = navController,
                    selectedItemIndex = 1
                ) }
            )
        }
        composable(
            "${Graphs.Main.Screens.explore_image}/{photoId}",
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId") ?: run {
                Log.e("PostDetailScreen", "Photo ID is null, returning from composable")
                return@composable
            }

            PostDetailScreenRoot(
                viewModel = viewModel<PostDetailViewModel>(
                    factory = viewModelFactory {
                        PostDetailViewModel(
                            MyApp.appModule.coreRepository,
                            photoId
                        )
                    }
                ),
                onExit = {
                    navController.popBackStack()
                }
            )
        }

        composable(Graphs.Main.Screens.create) {
            CreateScreenRoot(
                viewModel = viewModel<CreateViewModel>(
                    factory = viewModelFactory {
                        CreateViewModel()
                    }
                ),
                bottomNavbar = { BottomNavigationBar(
                    navController = navController,
                    selectedItemIndex = 2
                ) }
            )
        }
        composable(Graphs.Main.Screens.profile) {
            ProfileScreenRoot(
                viewModel = viewModel<ProfileViewModel>(
                    factory = viewModelFactory {
                        ProfileViewModel(
                            MyApp.appModule.coreRepository,
                            MyApp.appModule.authRepository
                        )
                    }
                ),
                onNavigateTo = { destination ->
                    when (destination) {
                        ProfileDestination.TermsOfService -> {
                            navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.TOS.name))
                        }
                        ProfileDestination.PrivacyPolicy -> {
                            navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.PP.name))
                        }
                        ProfileDestination.ContentPolicy -> {
                            navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.CP.name))
                        }
                        ProfileDestination.Faq -> {
                            navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.FAQ.name))
                        }
                        ProfileDestination.Instagram -> {
                            navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.IG.name))
                        }
                        ProfileDestination.TikTok -> {
                            navController.navigate(Graphs.Shared.Screens.web.replace("{webType}", WebContent.TIKTOK.name))
                        }
                    }
                },
                onNavigateToPhotoDetail = { selectedPhoto ->
                    navController.navigate("${Graphs.Main.Screens.explore_image}/${selectedPhoto}")
                },
                onLogOut = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                bottomNavbar = { BottomNavigationBar(
                    navController = navController,
                    selectedItemIndex = 3
                ) }
            )
        }
        composable(Graphs.Main.Screens.play_chat) {
            MessengerScreen(
                viewModel = viewModel<ChatViewModel>(
                    factory = viewModelFactory {
                        ChatViewModel(
                            MyApp.appModule.coreRepository
                        )
                    }
                ),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

private fun NavGraphBuilder.sharedGraph(navController: NavHostController) {
    composable(Graphs.Shared.Screens.web) { backStackEntry ->
        val policyType = WebContent.valueOf(backStackEntry.arguments?.getString("webType") ?: WebContent.TOS.name)
        WebViewScreen(
            policyType = policyType,
            onBackClick = {
                navController.popBackStack()
            }
        )
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

