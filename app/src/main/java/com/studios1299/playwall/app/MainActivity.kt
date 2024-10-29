package com.studios1299.playwall.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.studios1299.playwall.app.config.AppConfigManager
import com.studios1299.playwall.app.di.AppModuleImpl
import com.studios1299.playwall.core.presentation.designsystem.PlayWallTheme
import com.studios1299.playwall.app.navigation.NavigationHostLegacy
import com.studios1299.playwall.monetization.data.AdManager
import com.studios1299.playwall.monetization.data.ConsentManager

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adManager: AdManager
    private lateinit var consentManager: ConsentManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                Log.e("MainActivity", "playwalltheme")
                if(!viewModel.state.keepSplashScreen) {
                    Log.e("MainActivity", "after theme")
                    NavigationHostLegacy(
                        adManager = adManager,
                        isLoggedIn = viewModel.state.isLoggedIn
                    )
                    if (AppConfigManager.enableAppOpenAd) {
                        // Load App Open Ad here
                    }
                    Log.e("MainActivity", "before loading rewarded ad")
//                    adManager.loadRewardedAd { adLoaded ->
//                        if (adLoaded) {
//                            adManager.showRewardedAdIfLoaded(
//                                onAdClosed = { Log.e("MainActivity", "onAdClosed")},
//                                onRewardEarned = { Log.e("MainActivity", "onRewardEarned")},
//                                onAdNotLoaded = { Log.e("MainActivity", "onAdNotLoaded")},
//                            )
//                        } else {
//                            Log.e("MainActivity", "else adLoaded == false")
//                        }
//                    }
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
