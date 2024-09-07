package com.studios1299.playwall.app.config

import androidx.lifecycle.MutableLiveData
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class AppConfigLegacy private constructor() {

    var enableRewardAd = DEFAULT_ENABLE_REWARD_AD
    var enableBannerAd = DEFAULT_ENABLE_BANNER_AD
    var enableGridAd = DEFAULT_ENABLE_GRID_AD
    var enableInterstitialAd = DEFAULT_ENABLE_INTERSTITIAL_AD
    var allowPremiumPreview = DEFAULT_ALLOW_PREMIUM_PREVIEW
    var enableAppOpenAd = DEFAULT_ENABLE_APP_OPEN_AD
    var enableAppOpenAdOnResume = DEFAULT_ENABLE_APP_OPEN_AD_ON_RESUME
    var enableUserDailyReward = DEFAULT_ENABLE_USER_DAILY_REWARD
    var enableUserOpinionPrompt = DEFAULT_ENABLE_USER_OPINION_PROMPT
    val remoteConfigLoaded = MutableLiveData<Boolean>()

    companion object {
        private const val DEFAULT_ENABLE_REWARD_AD = true
        private const val DEFAULT_ENABLE_BANNER_AD = true
        private const val DEFAULT_ENABLE_GRID_AD = true
        private const val DEFAULT_ENABLE_INTERSTITIAL_AD = true
        private const val DEFAULT_ENABLE_APP_OPEN_AD = true
        private const val DEFAULT_ENABLE_APP_OPEN_AD_ON_RESUME = false
        private const val DEFAULT_ALLOW_PREMIUM_PREVIEW = true
        private const val DEFAULT_ENABLE_USER_DAILY_REWARD = true
        private const val DEFAULT_ENABLE_USER_OPINION_PROMPT = true

        @Volatile
        private var instance: AppConfigLegacy? = null


        fun getInstance(): AppConfigLegacy {
            return instance ?: synchronized(this) {
                instance ?: AppConfigLegacy().also { instance = it }
            }
        }

    }

    fun updateConfig(remoteConfig: FirebaseRemoteConfig) {
        enableRewardAd = remoteConfig.getBoolean("enable_reward_ad")
        enableBannerAd = remoteConfig.getBoolean("enable_banner_ad")
        enableGridAd = remoteConfig.getBoolean("enable_grid_ad")
        enableInterstitialAd = remoteConfig.getBoolean("enable_interstitial_ad")
        allowPremiumPreview = remoteConfig.getBoolean("allow_premium_preview")
        enableAppOpenAd = remoteConfig.getBoolean("enable_app_open_ad")
        enableAppOpenAdOnResume = remoteConfig.getBoolean("enable_app_open_ad_on_resume")
        enableUserDailyReward = remoteConfig.getBoolean("enable_user_daily_reward")
        enableUserOpinionPrompt = remoteConfig.getBoolean("enable_user_opinion_prompt")
        remoteConfigLoaded.postValue(true)
    }
}


//// MyApplication -> onCreate()
//val remoteConfig = Firebase.remoteConfig
//val configSettings = remoteConfigSettings {
//    minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
//}
//remoteConfig.setConfigSettingsAsync(configSettings)
//var appConfig = AppConfig.getInstance()

//// Opcjonalnie -> tak sobie zaczynam wczytywać App Open Ada
//appConfig.remoteConfigLoaded.observeForever { isLoaded ->
//    Log.d("MyApplication", "App config loaded: $isLoaded")
//    if(isLoaded) {
//
//        if(!preferenceManager.isPremiumUser() && !preferenceManager.appInstalledToday() && appConfig.enableAppOpenAd) {
//            appOpenAdManager?.loadAd(this)
//        }
//    }
//}


//// Splash Activity -> onCreate()
//val appConfig = AppConfig.getInstance()
//remoteConfig = Firebase.remoteConfig
//initializeFirebaseConfig(remoteConfig, appConfig, this)



//if (preferenceManager.isPremiumUser() || preferenceManager.appInstalledToday()) {
//    // no app open ad
//    appConfig.remoteConfigLoaded.observeForever { isLoaded ->
//        if(isLoaded) {
//            Log.d("SplashActivity", "Premium or first day, navigating to MainActivity")
//            navigateToMainActivity(400)
//        }
//    }
//} else {
//    appOpenAdManager?.appOpenAdLoaded?.observeForever { isLoaded ->
//        Log.d("SplashActivity", "App open loaded: $isLoaded")
//        if(isLoaded && !isMainActivityLaunched) {
//            (application as MyApplication).appOpenAdLoaded = true
//            Log.d("SplashActivity", "Trying to run main activity, because app open ad is loaded")
//            navigateToMainActivity(400)
//        }
//    }
//
//    appConfig.remoteConfigLoaded.observeForever { isLoaded ->
//        Log.d("SplashActivity", "Config loaded: $isLoaded")
//        if(!appConfig.enableAppOpenAd) {
//            Log.d("SplashActivity", "Trying to run main activity, because app open ad is disabled")
//            navigateToMainActivity(0)
//        }
//    }
//}


// i na koniec (w SplashActivity) mam handler jak nic się nie wydarzy. wtedy wczytają się domyślne wartości w AppConfig
//Handler(Looper.getMainLooper()).postDelayed({
//    navigateToMainActivity(0)
//}, 4500)


//// i tu jeszcze funkcja, która jako helper pomaga mi odpalić config (funkcja jest używana w MyApplication)
//fun initializeFirebaseConfig(
//    remoteConfig: FirebaseRemoteConfig,
//    appConfig: AppConfig,
//    activity: Activity
//) {
//    remoteConfig.fetchAndActivate()
//        .addOnCompleteListener(activity) { task ->
//            if (task.isSuccessful) {
//                Log.d("initializeFirebaseConfig", "Config initialized")
//                appConfig.updateConfig(remoteConfig)
//            }
//        }
//
//    remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
//        override fun onUpdate(configUpdate : ConfigUpdate) {
//            Log.d("initializeFirebaseConfig", "Updated keys: " + configUpdate.updatedKeys)
//            remoteConfig.activate()
//            appConfig.updateConfig(remoteConfig)
//        }
//
//
//        override fun onError(error : FirebaseRemoteConfigException) {
//            Log.w("initializeFirebaseConfig", "Config update error with code: " + error.code, error)
//        }
//    })
//}