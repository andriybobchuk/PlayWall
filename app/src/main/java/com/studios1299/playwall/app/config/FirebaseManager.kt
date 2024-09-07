package com.studios1299.playwall.app.config

import android.content.Context
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.initialize
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.studios1299.playwall.BuildConfig

object FirebaseManager {
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var remoteConfig: FirebaseRemoteConfig

    private var isCrashlyticsInitialized = false
    private var isRemoteConfigInitialized = false

    fun init(context: Context) {
        Firebase.initialize(context)
        crashlytics = FirebaseCrashlytics.getInstance()
        remoteConfig = Firebase.remoteConfig

        initializeCrashlytics()
        initializeRemoteConfig()
    }

    private fun initializeCrashlytics() {
        crashlytics.setCrashlyticsCollectionEnabled(true)
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("device", Build.MODEL)
        crashlytics.log("Crashlytics initialized")
        isCrashlyticsInitialized = true
    }

    private fun initializeRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        fetchAndUpdateConfig()
    }

    private fun fetchAndUpdateConfig() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                AppConfigManager.updateConfig(getConfigValues())
                isRemoteConfigInitialized = true
            }
        }
    }

    fun isFirebaseSetupComplete(): Boolean {
        return isCrashlyticsInitialized && isRemoteConfigInitialized
    }

    private fun getConfigValues(): Map<String, Boolean> {
        return mapOf(
            "enable_reward_ad" to remoteConfig.getBoolean("enable_reward_ad"),
            "enable_banner_ad" to remoteConfig.getBoolean("enable_banner_ad"),
            "enable_grid_ad" to remoteConfig.getBoolean("enable_grid_ad"),
            "enable_interstitial_ad" to remoteConfig.getBoolean("enable_interstitial_ad"),
            "allow_premium_preview" to remoteConfig.getBoolean("allow_premium_preview"),
            "enable_app_open_ad" to remoteConfig.getBoolean("enable_app_open_ad"),
            "enable_app_open_ad_on_resume" to remoteConfig.getBoolean("enable_app_open_ad_on_resume"),
            "enable_user_daily_reward" to remoteConfig.getBoolean("enable_user_daily_reward"),
            "enable_user_opinion_prompt" to remoteConfig.getBoolean("enable_user_opinion_prompt")
        )
    }
}
