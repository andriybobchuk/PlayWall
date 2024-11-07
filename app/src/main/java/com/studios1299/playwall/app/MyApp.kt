package com.studios1299.playwall.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.studios1299.playwall.app.config.FirebaseManager
import com.studios1299.playwall.app.di.AppModule
import com.studios1299.playwall.app.di.AppModuleImpl
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.NetworkMonitor

/**
 * Used to initialize application wide utilities
 */
class MyApp: Application() {

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this) // Initialize my custom lovely manual DI
        FirebaseManager.init(this) // Initialize Firebase and app config
        Preferences.initialize(this) // Shared prefs
        NetworkMonitor.initialize(this)
    }
}