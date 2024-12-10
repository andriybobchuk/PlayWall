package com.studios1299.playwall.core.data.networking.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserAppDataApi {

    @GET("api/user/getAppData")
    suspend fun getAppData(
        @Header("Authorization") authHeader: String
    ): Response<UserAppDataResponse>

    @POST("api/user/updateAppData")
    suspend fun updateAppData(
        @Header("Authorization") authHeader: String,
        @Body updateAppDataRequest: UpdateAppDataRequest
    ): Response<Unit>
}

data class UserAppDataResponse(
    val devilCount: Int,
    val isPremium: Int,
    val consecutiveDays: Int,
    val lastCheckInDate: String
)

data class UpdateAppDataRequest(
    val devilCount: Int? = null,
    val isPremium: Boolean? = null,
    val consecutiveDays: Int? = null,
    val lastCheckInDate: String? = null
)
