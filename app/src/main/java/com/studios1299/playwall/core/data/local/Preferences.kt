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
    private const val KEY_DEVILS_COUNT = "devils_count"
    private const val KEY_LAST_CHECK_IN_DATE = "last_check_in_date"
    private const val KEY_CONSECUTIVE_DAYS = "consecutive_days"



    private const val KEY_IS_PREMIUM = "is_premium"
    private const val KEY_CHECKED_IN_TODAY = "checked_in_today"


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

    // Devils management
    fun getDevilsCount(): Int {
        return sharedPreferences.getInt(KEY_DEVILS_COUNT, 0)
    }

    fun setDevilsCount(count: Int) {
        sharedPreferences.edit().putInt(KEY_DEVILS_COUNT, count).apply()
    }

    fun getLastCheckInDate(): String? {
        return sharedPreferences.getString(KEY_LAST_CHECK_IN_DATE, null)
    }

    fun setLastCheckInDate(date: String) {
        sharedPreferences.edit().putString(KEY_LAST_CHECK_IN_DATE, date).apply()
    }

    fun getConsecutiveDays(): Int {
        return sharedPreferences.getInt(KEY_CONSECUTIVE_DAYS, 0)
    }

    fun setConsecutiveDays(days: Int) {
        sharedPreferences.edit().putInt(KEY_CONSECUTIVE_DAYS, days).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }





    //..,..

    // Premium status management
    fun isPremium(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PREMIUM, false)
    }

    fun setPremiumStatus(isPremium: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
    }

    // Daily check-in management
    fun hasCheckedInToday(): Boolean {
        return sharedPreferences.getBoolean(KEY_CHECKED_IN_TODAY, false)
    }

    fun setCheckedInToday(checkedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_CHECKED_IN_TODAY, checkedIn).apply()
    }
}

