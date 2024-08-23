package com.studios1299.vrwallpaper6.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.studios1299.vrwallpaper6.core.presentation.designsystem.PlayWallTheme
import com.studios1299.vrwallpaper6.app.navigation.NavigationHostLegacy
import com.studios1299.vrwallpaper6.core.presentation.viewModelFactory
import com.studios1299.vrwallpaper6.feature.auth.domain.UserDataValidator
import com.studios1299.vrwallpaper6.feature.auth.presentation.register.RegisterViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.isCheckingAuth
            }
        }
        setContent {
            PlayWallTheme {
                Scaffold(
//                    bottomBar = {
//                        BottomNavigationBar(navController)
//                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    if(!viewModel.state.isCheckingAuth) {
                        val navController = rememberNavController()
                        NavigationHostLegacy(
                            navController = navController,
                            isLoggedIn = viewModel.state.isLoggedIn,
                            innerPadding = innerPadding
                        )
                    }
                }
            }
        }
    }
}
