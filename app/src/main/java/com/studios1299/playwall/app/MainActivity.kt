package com.studios1299.playwall.app

import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.studios1299.playwall.app.navigation.NavigationHostLegacy
import com.studios1299.playwall.core.presentation.designsystem.PlayWallTheme
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_BLACK
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_WHITE
import com.studios1299.playwall.core.presentation.wrzutomat.SubscriptionOption
import com.studios1299.playwall.core.presentation.wrzutomat.SubscriptionWeeklyScreen
import com.studios1299.playwall.monetization.data.AdManager
import com.studios1299.playwall.monetization.data.ConsentManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adManager: AdManager
    private lateinit var consentManager: ConsentManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb())
        )

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        adManager = AdManager(this)
        consentManager = ConsentManager.getInstance(this)

        try {
            gatherConsent()
        } catch (e: Exception) {
            Log.d("MainActivity", "Error gathering consent: $e")
        }

        actionBar?.hide()
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.state.keepSplashScreen
            }
        }
        setContent {
            PlayWallTheme {

//                runBlocking {
//
//                    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
//                        println("Caught exception: $exception")
//                    }
//
//                    CoroutineScope(
//                        Dispatchers.IO +
//                                SupervisorJob() +
//                                CoroutineName("") +
//                                CoroutineExceptionHandler { _, exception ->
//                                    println("Caught exception: $exception")
//                                }
//                    )
//
//
//                    GlobalScope.launch { }
//
//                    lifecycleScope.launch {
//
//                    }
//
//                    supervisorScope {  }
//
//                    MainScope().launch {
//
//                    }
//
//                    SharedFlow()
//
//
//
//                    lifecycleScope.async { }
//
//                    suspendCancellableCoroutine { continuation ->
//                        println("Before too")
//                        continuation.resume(1)
//                    }
//
//
//                }
//
               // NotificationManager.IMPORTANCE_HIGH

                if(!viewModel.state.keepSplashScreen) {
                    NavigationHostLegacy(
                        adManager = adManager,
                        isLoggedIn = viewModel.state.isLoggedIn
                    )
                }
            }
        }
    }

    private fun gatherConsent() {
        consentManager.gatherConsent(this) { error ->
            if (error == null) {
                Log.d("MainActivity", "Consent gathered")
            } else {
                Log.e("MainActivity", "Consent gathering error: ${error.message}")
            }
        }
    }
}
