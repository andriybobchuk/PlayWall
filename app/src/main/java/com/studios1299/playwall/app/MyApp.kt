package com.studios1299.playwall.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.studios1299.playwall.BuildConfig
import com.studios1299.playwall.app.di.AppModule
import com.studios1299.playwall.app.di.AppModuleImpl

class MyApp: Application() {

    lateinit var sharedPrefs: SharedPreferences
        private set

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)

        Firebase.initialize(this)
        initializeCrashlytics()

        sharedPrefs = getSharedPreferences("playwall", Context.MODE_PRIVATE)
    }

    private fun initializeCrashlytics() {
        //val isCrashlyticsEnabled = !BuildConfig.DEBUG
        val isCrashlyticsEnabled = true

        val crashlytics = appModule.crashlytics
        crashlytics.setCrashlyticsCollectionEnabled(isCrashlyticsEnabled)

        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("device", android.os.Build.MODEL)

        crashlytics.log("Crashlytics initialized")

        //throw RuntimeException("Test Crash for crashlytics")
    }

}