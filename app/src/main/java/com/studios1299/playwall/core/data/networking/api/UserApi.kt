package com.studios1299.playwall.core.data.networking.api

import com.studios1299.playwall.core.data.networking.request.user.CreateUserRequest
import com.studios1299.playwall.core.data.networking.request.user.UpdateProfileRequest
import com.studios1299.playwall.core.data.networking.response.UserDataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApi {

    @POST("api/user/createUser")
    suspend fun createUser(
        @Header("Authorization") authHeader: String,
        @Body user: CreateUserRequest
    ): Response<Unit>

    @GET("api/user/getUserData")
    suspend fun getUserData(
        @Header("Authorization") authHeader: String
    ): Response<UserDataResponse>

    @POST("api/user/updateProfile")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Body updateProfileRequest: UpdateProfileRequest
    ): Response<Unit>

    @POST("api/user/updatePushToken")
    suspend fun updatePushToken(
        @Header("Authorization") authHeader: String,
        @Body createUserRequest: Map<String, String>
    ): Response<Unit>

}