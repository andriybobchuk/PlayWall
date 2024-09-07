package com.studios1299.playwall.app

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.app.config.AppConfigManager
import com.studios1299.playwall.app.config.FirebaseManager
import com.studios1299.playwall.core.domain.CoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(
    private val coreRepository: CoreRepository = MyApp.appModule.coreRepository
): ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(keepSplashScreen = true)
            state = state.copy(
                isLoggedIn = coreRepository.getCurrentUserId() != null
            )
            state = state.copy(keepSplashScreen = false)
        }
    }
    init {
        viewModelScope.launch {
            state = state.copy(keepSplashScreen = true)
            state = state.copy(isLoggedIn = coreRepository.getCurrentUserId() != null)

            // Wait until Firebase is done initializing
//            while (!FirebaseManager.isFirebaseSetupComplete()) {
//                delay(100) // Poll every 100ms until Firebase setup is complete
//            }
//            logConfigValues()

            state = state.copy(keepSplashScreen = false)
        }
    }

    private fun logConfigValues() {
        // Log config values to ensure they are loaded correctly
        Log.d("MainViewModel", "Config Values:")
        Log.d("MainViewModel", "enableRewardAd: ${AppConfigManager.enableRewardAd}")
        Log.d("MainViewModel", "enableBannerAd: ${AppConfigManager.enableBannerAd}")
        Log.d("MainViewModel", "enableGridAd: ${AppConfigManager.enableGridAd}")
        Log.d("MainViewModel", "enableInterstitialAd: ${AppConfigManager.enableInterstitialAd}")
        Log.d("MainViewModel", "allowPremiumPreview: ${AppConfigManager.allowPremiumPreview}")
        Log.d("MainViewModel", "enableAppOpenAd: ${AppConfigManager.enableAppOpenAd}")
        Log.d("MainViewModel", "enableAppOpenAdOnResume: ${AppConfigManager.enableAppOpenAdOnResume}")
        Log.d("MainViewModel", "enableUserDailyReward: ${AppConfigManager.enableUserDailyReward}")
        Log.d("MainViewModel", "enableUserOpinionPrompt: ${AppConfigManager.enableUserOpinionPrompt}")
    }
}