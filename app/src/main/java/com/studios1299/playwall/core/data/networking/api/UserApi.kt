package com.studios1299.playwall.core.data.networking.api

import com.studios1299.playwall.core.data.networking.request.user.CreateUserRequest
import com.studios1299.playwall.core.data.networking.request.user.UpdateProfileRequest
import com.studios1299.playwall.core.data.networking.response.user.GetScreenRatioResponse
import com.studios1299.playwall.core.data.networking.response.user.UserDataResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UserApi {

    @GET("api/health")
    suspend fun checkHealth(): Response<Unit>

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
        @Body createUserRequest: Map<String, String?>
    ): Response<Unit>

    @GET("api/user/getFriendScreenRatio")
    suspend fun getFriendScreenRatio(
        @Header("Authorization") authHeader: String,
        @Query("friendId") friendId: Int
    ): Response<GetScreenRatioResponse>

    @Multipart
    @POST("api/uploadWallpaper")
    suspend fun uploadWallpaper(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
        @Part("folder") folder: RequestBody
    ): Response<UploadWallpaperResponse>

    @GET("api/getPresignedUrl")
    suspend fun getPresignedUrl(
        @Header("Authorization") authHeader: String,
        @Query("fileName") fileName: String
    ): Response<PresignedUrlResponse>
}

data class UploadWallpaperResponse(val success: Boolean, val fileKey: String)
data class PresignedUrlResponse(val success: Boolean, val url: String)