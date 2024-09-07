package com.studios1299.playwall.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.studios1299.playwall.app.config.FirebaseManager
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

        // Initialize manual DI:
        appModule = AppModuleImpl(this)

        // Initialize Firebase and app config
        FirebaseManager.init(this)

        sharedPrefs = getSharedPreferences("playwall", Context.MODE_PRIVATE)
    }
}