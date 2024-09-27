package com.studios1299.playwall.core.data.networking.api

import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.CommentRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ReactionRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ReportRequest
import com.studios1299.playwall.core.data.networking.response.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.WallpaperHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WallpaperApi {

    @POST("api/changeWallpaper")
    suspend fun changeWallpaper(
        @Header("Authorization") authHeader: String,
        @Body changeWallpaperRequest: ChangeWallpaperRequest
    ): Response<Void>

    @GET("api/wallpaperHistory/{userId}")
    suspend fun getWallpaperHistory(
        @Header("Authorization") authHeader: String,
        @Path("userId") userId: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<WallpaperHistoryResponse>>

    @GET("api/getRecipientData/{recipientId}")
    suspend fun getRecipientData(
        @Header("Authorization") authHeader: String,
        @Path("recipientId") recipientId: Int
    ): Response<UserDataResponse>

    @POST("api/wallpaper/addReaction")
    suspend fun addReaction(
        @Header("Authorization") authHeader: String,
        @Body reactionRequest: ReactionRequest
    ): Response<Unit>

    @POST("api/wallpaper/removeReaction")
    suspend fun removeReaction(
        @Header("Authorization") authHeader: String,
        @Body reactionRequest: ReactionRequest
    ): Response<Unit>

    @POST("api/wallpaper/report")
    suspend fun reportWallpaper(
        @Header("Authorization") authHeader: String,
        @Body reportRequest: ReportRequest
    ): Response<Unit>

    @POST("api/wallpaper/addComment")
    suspend fun addComment(
        @Header("Authorization") authHeader: String,
        @Body commentRequest: CommentRequest
    ): Response<Unit>
}