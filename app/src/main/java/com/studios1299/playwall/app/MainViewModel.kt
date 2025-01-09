package com.studios1299.playwall.app

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.app.config.AppConfigManager
import com.studios1299.playwall.app.config.FirebaseManager
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.monetization.presentation.AppState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(
    private val coreRepository: CoreRepository = MyApp.appModule.coreRepository
) : ViewModel() {

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
            state = state.copy(keepSplashScreen = false)
        }
        logConfigValues()
        setAdUnitId()
    }

    private fun setAdUnitId() {
        val appContext = MyApp.appModule.context
        val appInfo = appContext.packageManager.getApplicationInfo(appContext.packageName, PackageManager.GET_META_DATA)
        val metaData = appInfo.metaData
        metaData.putString("com.google.android.gms.ads.APPLICATION_ID", AppConfigManager.adUnitId)
    }

    private fun logConfigValues() {
        Log.d("MainViewModel", "Config Values:")
        Log.d("MainViewModel", "enableRewardAd: ${AppConfigManager.enableRewardAd}")
        Log.d("MainViewModel", "enableBannerAd: ${AppConfigManager.enableBannerAd}")
        Log.d("MainViewModel", "enableGridAd: ${AppConfigManager.enableGridAd}")
        Log.d("MainViewModel", "enableInterstitialAd: ${AppConfigManager.enableInterstitialAd}")
        Log.d("MainViewModel", "allowPremiumPreview: ${AppConfigManager.allowPremiumPreview}")
        Log.d("MainViewModel", "enableAppOpenAd: ${AppConfigManager.enableAppOpenAd}")
        Log.d(
            "MainViewModel",
            "enableAppOpenAdOnResume: ${AppConfigManager.enableAppOpenAdOnResume}"
        )
        Log.d("MainViewModel", "enableUserDailyReward: ${AppConfigManager.enableUserDailyReward}")
        Log.d(
            "MainViewModel",
            "enableUserOpinionPrompt: ${AppConfigManager.enableUserOpinionPrompt}"
        )
    }
}