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
import androidx.navigation.compose.rememberNavController
import com.studios1299.vrwallpaper6.core.presentation.designsystem.MessengerTheme
import com.studios1299.vrwallpaper6.navigation.NavigationHost

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MessengerTheme {
                val navController = rememberNavController()
                Scaffold(
//                    bottomBar = {
//                        BottomNavigationBar(navController)
//                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavigationHost(navController, innerPadding)
                }
            }
        }
    }
}
