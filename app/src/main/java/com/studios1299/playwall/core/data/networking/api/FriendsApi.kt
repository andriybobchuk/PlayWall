package com.studios1299.playwall.core.data.networking.api

import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.friendships.InviteRequest
import com.studios1299.playwall.feature.play.presentation.play.Friend
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface FriendsApi {
    @POST("api/friendship")
    suspend fun inviteFriend(
        @Header("Authorization") authHeader: String,
        @Body inviteRequest: InviteRequest
    ): Response<Unit>

    @GET("api/friendship")
    suspend fun getFriends(@Header("Authorization") s: String): Response<List<Friend>>

    @GET("api/friendship/requests")
    suspend fun getFriendRequests(@Header("Authorization") s: String): Response<List<Friend>>

    @POST("api/friendship/accept")
    suspend fun acceptFriendRequest(
        @Header("Authorization") authHeader: String,
        @Body acceptRequest: AcceptRequest
    ): Response<Unit>

    @POST("api/friendship/decline")
    suspend fun declineFriendRequest(
        @Header("Authorization") authHeader: String,
        @Body declineRequest: DeclineRequest
    ): Response<Unit>
//
//    @GET("api/wallpaperHistory/{userId}")
//    suspend fun getWallpaperHistory(
//        @Header("Authorization") authHeader: String,
//        @Path("userId") userId: Int
//    ): List<Wallpaper>
//
//    @POST("api/wallpaper/addReaction")
//    suspend fun addReaction(
//        @Header("Authorization") authHeader: String,
//        @Body reactionRequest: ReactionRequest
//    ): Response<Void>
//
//    @POST("api/wallpaper/removeReaction")
//    suspend fun removeReaction(
//        @Header("Authorization") authHeader: String,
//        @Body reactionRequest: ReactionRequest
//    ): Response<Void>
//
//    @POST("api/wallpaper/report")
//    suspend fun reportWallpaper(
//        @Header("Authorization") authHeader: String,
//        @Body reportRequest: ReportRequest
//    ): Response<Void>
//
//    @POST("api/wallpaper/addComment")
//    suspend fun addComment(
//        @Header("Authorization") authHeader: String,
//        @Body commentRequest: CommentRequest
//    ): Response<Void>
}