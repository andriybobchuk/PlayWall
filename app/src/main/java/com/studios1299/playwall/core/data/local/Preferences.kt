package com.studios1299.playwall.core.data.local

import android.content.Context
import android.content.SharedPreferences
import com.studios1299.playwall.BuildConfig
import com.studios1299.playwall.core.domain.model.WallpaperOption


object Preferences {

    private const val APP_NAME = BuildConfig.APPLICATION_ID
    private const val KEY_LIKED_WALLPAPERS_ = "liked_wallpapers"
    private const val KEY_WALLPAPER_DESTINATION = "wallpaper_destination"
    private const val KEY_SAVE_INCOMING_WALLPAPERS = "save_incoming_wallpapers"
    private const val KEY_CURRENT_WALLPAPER_ID = "current_wallpaper_id"
    private const val KEY_PREVIOUS_WALLPAPER_ID = "previous_wallpaper_id"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_FCM_TOKEN = "fcm_token"


    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
    }

    fun isWallpaperLiked(wallpaperId: Int): Boolean {
        return sharedPreferences.getBoolean("$KEY_LIKED_WALLPAPERS_$wallpaperId", false)
    }

    fun setWallpaperLiked(wallpaperId: Int, liked: Boolean) {
        sharedPreferences.edit().putBoolean("$KEY_LIKED_WALLPAPERS_$wallpaperId", liked).apply()
    }

    fun getWallpaperDestination(): WallpaperOption {
        val destinationName = sharedPreferences.getString(KEY_WALLPAPER_DESTINATION, WallpaperOption.HomeScreen.toString())
        return WallpaperOption.getEnumByDisplayName(destinationName!!) ?: WallpaperOption.HomeScreen
    }

    fun setWallpaperDestination(option: WallpaperOption) {
        sharedPreferences.edit().putString(KEY_WALLPAPER_DESTINATION, option.toString()).apply()
    }

    fun isSavingIncomingWallpapersEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SAVE_INCOMING_WALLPAPERS, false)
    }

    fun setSaveIncomingWallpapers(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SAVE_INCOMING_WALLPAPERS, enabled).apply()
    }

    fun getCurrentWallpaperId(): String? {
        return sharedPreferences.getString(KEY_CURRENT_WALLPAPER_ID, null)
    }

    fun setCurrentWallpaperId(wallpaperId: String) {
        sharedPreferences.edit().putString(KEY_CURRENT_WALLPAPER_ID, wallpaperId).apply()
    }

    fun getPreviousWallpaperId(): String? {
        return sharedPreferences.getString(KEY_PREVIOUS_WALLPAPER_ID, null)
    }

    fun setPreviousWallpaperId(wallpaperId: String) {
        sharedPreferences.edit().putString(KEY_PREVIOUS_WALLPAPER_ID, wallpaperId).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun setAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getFcmToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    fun setFcmToken(fcmToken: String) {
        sharedPreferences.edit().putString(KEY_FCM_TOKEN, fcmToken).apply()
    }
}


//interface PreferencesDataSource {
//    fun isWallpaperLiked(wallpaperId: String): Boolean
//    fun setWallpaperLiked(wallpaperId: String, liked: Boolean)
//
//    fun getWallpaperDestination(): WallpaperOption
//    fun setWallpaperDestination(option: WallpaperOption)
//
//    fun isSavingIncomingWallpapersEnabled(): Boolean
//    fun setSaveIncomingWallpapers(enabled: Boolean)
//
//    fun getCurrentWallpaperId(): String?
//    fun setCurrentWallpaperId(wallpaperId: String)
//
//    fun getPreviousWallpaperId(): String?
//    fun setPreviousWallpaperId(wallpaperId: String)
//
//    fun getAuthToken(): String?
//    fun setAuthToken(token: String)
//}
//
//class PreferencesDataSourceImpl(private val sharedPreferences: SharedPreferences) : PreferencesDataSource {
//
//    companion object {
//        private const val KEY_LIKED_WALLPAPERS_ = "liked_wallpapers"
//        private const val KEY_WALLPAPER_DESTINATION = "wallpaper_destination"
//        private const val KEY_SAVE_INCOMING_WALLPAPERS = "save_incoming_wallpapers"
//        private const val KEY_CURRENT_WALLPAPER_ID = "current_wallpaper_id"
//        private const val KEY_PREVIOUS_WALLPAPER_ID = "previous_wallpaper_id"
//        private const val KEY_TOKEN = "auth_token"
//    }
//
//    override fun isWallpaperLiked(wallpaperId: String): Boolean {
//        return sharedPreferences.getBoolean("$KEY_LIKED_WALLPAPERS_$wallpaperId", false)
//    }
//
//    override fun setWallpaperLiked(wallpaperId: String, liked: Boolean) {
//        sharedPreferences.edit().putBoolean("$KEY_LIKED_WALLPAPERS_$wallpaperId", liked).apply()
//    }
//
//    override fun getWallpaperDestination(): WallpaperOption {
//        val destinationName = sharedPreferences.getString(KEY_WALLPAPER_DESTINATION, WallpaperOption.HomeScreen.toString())
//        return WallpaperOption.getEnumByDisplayName(destinationName!!) ?: WallpaperOption.HomeScreen
//    }
//
//    override fun setWallpaperDestination(option: WallpaperOption) {
//        sharedPreferences.edit().putString(KEY_WALLPAPER_DESTINATION, option.toString()).apply()
//    }
//
//    override fun isSavingIncomingWallpapersEnabled(): Boolean {
//        return sharedPreferences.getBoolean(KEY_SAVE_INCOMING_WALLPAPERS, false)
//    }
//
//    override fun setSaveIncomingWallpapers(enabled: Boolean) {
//        sharedPreferences.edit().putBoolean(KEY_SAVE_INCOMING_WALLPAPERS, enabled).apply()
//    }
//
//    override fun getCurrentWallpaperId(): String? {
//        return sharedPreferences.getString(KEY_CURRENT_WALLPAPER_ID, null)
//    }
//
//    override fun setCurrentWallpaperId(wallpaperId: String) {
//        sharedPreferences.edit().putString(KEY_CURRENT_WALLPAPER_ID, wallpaperId).apply()
//    }
//
//    override fun getPreviousWallpaperId(): String? {
//        return sharedPreferences.getString(KEY_PREVIOUS_WALLPAPER_ID, null)
//    }
//
//    override fun setPreviousWallpaperId(wallpaperId: String) {
//        sharedPreferences.edit().putString(KEY_PREVIOUS_WALLPAPER_ID, wallpaperId).apply()
//    }
//
//    override fun getAuthToken(): String? {
//        return sharedPreferences.getString(KEY_TOKEN, null)
//    }
//
//    override fun setAuthToken(token: String) {
//        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
//    }
//}
