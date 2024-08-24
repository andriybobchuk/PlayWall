package com.studios1299.playwall.app

import android.app.Application
import com.studios1299.playwall.app.di.AppModule
import com.studios1299.playwall.app.di.AppModuleImpl

/**
 * The main Application class for setting up the dependency injection module.
 *
 * Initialize your dependencies here in `onCreate()`. To add more modules, include
 * them in the companion object. Access the module's dependencies via `MyApp.appModule`.
 */
class MyApp: Application() {

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }
}