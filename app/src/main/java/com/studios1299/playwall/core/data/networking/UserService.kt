package com.studios1299.playwall.core.data.networking

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UserService {

    @POST("api/user/createUser")
    fun createUser(
        @Header("Authorization") authHeader: String,
        @Body pushToken: PushTokenRequest
    ): Call<Void>

    @POST("api/user/updatePushToken")
    fun updatePushToken(
        @Header("Authorization") authHeader: String,
        @Body pushTokenRequest: PushTokenRequest
    ): Call<Void>
//    @POST("api/changeWallpaper")
//    fun changeWallpaper(
//        @Header("Authorization") authHeader: String,
//        @Body changeWallpaperRequest: ChangeWallpaperRequest
//    ): Call<Void>
}