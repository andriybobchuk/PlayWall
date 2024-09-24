package com.studios1299.playwall.core.data.networking.api

import com.studios1299.playwall.core.data.networking.request.user.CreateUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApi {

    @POST("api/user/createUser")
    suspend fun createUser(
        @Header("Authorization") authHeader: String,
        @Body user: CreateUserRequest
    ): Response<Unit>

    @POST("api/user/updatePushToken")
    fun updatePushToken(
        @Header("Authorization") authHeader: String,
        @Body createUserRequest: CreateUserRequest
    ): Response<Void>
//    @POST("api/changeWallpaper")
//    fun changeWallpaper(
//        @Header("Authorization") authHeader: String,
//        @Body changeWallpaperRequest: ChangeWallpaperRequest
//    ): Call<Void>
}