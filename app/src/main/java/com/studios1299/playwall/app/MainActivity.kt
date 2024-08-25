package com.studios1299.playwall.app

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
import androidx.navigation.compose.rememberNavController
import com.studios1299.playwall.app.navigation.BottomNavigationBar
import com.studios1299.playwall.core.presentation.designsystem.PlayWallTheme
import com.studios1299.playwall.app.navigation.NavigationHostLegacy

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        actionBar?.hide()
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.isCheckingAuth
            }
        }
        setContent {
            PlayWallTheme {
                if(!viewModel.state.isCheckingAuth) {
                    NavigationHostLegacy(
                        isLoggedIn = viewModel.state.isLoggedIn
                    )
                }
            }
        }
    }
}
