package com.andriybobchuk.messenger.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.andriybobchuk.messenger.app.MyApp
import com.andriybobchuk.messenger.core.presentation.viewModelFactory
import com.andriybobchuk.messenger.feature.auth.presentation.intro.IntroScreenRoot
import com.andriybobchuk.messenger.feature.auth.presentation.register.RegisterScreenRoot
import com.andriybobchuk.messenger.feature.auth.presentation.register.RegisterViewModel
import com.andriybobchuk.messenger.feature.chat.presentation.screens.chat.MessengerScreen
import com.andriybobchuk.messenger.feature.chat.presentation.screens.chat.viewmodel.ChatViewModel

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
    NavHost(
        navController = navController,
        startDestination = Screens.RegisterScreen
    ) {
        composable<Screens.IntroScreen> {
            IntroScreenRoot(onSignUpClick = { /*TODO*/ }) {

            }
        }
        composable<Screens.RegisterScreen> {
            RegisterScreenRoot(
                onSignInClick = { /*TODO*/ },
                onSuccessfulRegistration = { /*TODO*/ },
                viewModel = viewModel<RegisterViewModel>(
                    factory = viewModelFactory {
                        RegisterViewModel()
                    }
                ),
            )
        }
        composable<Screens.LoginScreen> {
           // LoginScreenContent()
        }
        composable<Screens.HomeScreen> {
            HomeScreenContent()
        }
        composable<Screens.ChatScreen> {
            MessengerScreen(
                viewModel = viewModel<ChatViewModel>(
                    factory = viewModelFactory {
                        ChatViewModel(MyApp.appModule.chatRepository)
                    }
                ),
                onBackClick = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
        composable<Screens.SettingsScreen> {
            SettingsScreenContent()
        }
        composable<Screens.ProfileScreen> {
            ProfileScreenContent()
        }
    }
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
        Text(text = "Home Screen")
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

