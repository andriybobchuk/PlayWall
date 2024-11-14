package com.studios1299.playwall.core.data.networking.api

import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.BlockRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.friendships.InviteRequest
import com.studios1299.playwall.core.data.networking.request.friendships.RemoveFriendRequest
import com.studios1299.playwall.core.data.networking.request.friendships.UnblockRequest
import com.studios1299.playwall.play.presentation.play.Friend
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

    @POST("api/friendship/removeFriend")
    suspend fun removeFriend(
        @Header("Authorization") authHeader: String,
        @Body removeFriendRequest: RemoveFriendRequest
    ): Response<Unit>

    @POST("api/friendship/block")
    suspend fun blockUser(
        @Header("Authorization") authHeader: String,
        @Body blockRequest: BlockRequest
    ): Response<Unit>

    @POST("api/friendship/unblock")
    suspend fun unblockUser(
        @Header("Authorization") authHeader: String,
        @Body unblockRequest: UnblockRequest
    ): Response<Unit>
}