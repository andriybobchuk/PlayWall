package com.studios1299.playwall.core.domain

import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.response.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.explore.presentation.explore.Photo
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.play.Friend
import java.io.File

interface CoreRepository {
    // CORE
    suspend fun getCurrentUserId(): String?
    suspend fun getUserData(): SmartResult<UserDataResponse, DataError.Network>
    suspend fun updateProfile(avatarId: String?, nick: String?): SmartResult<Unit, DataError.Network>

    // EXPLORE
    suspend fun getExploreItems(): List<Photo>
    fun getLikeCount(photoId: String): Int

    // CHAT
    suspend fun retrieveMessages(page: Int, pageSize: Int): Result<List<Message>>
   // fun getLastMessageId(): Result<String>
    fun addMessage(message: Message)
    //fun deleteMessage(messageId: String)
    fun updateMessage(message: Message)
    fun getCurrentUser(): User
    fun getRecipient(): User
    fun getUserNameById(userId: String): String

    // S3
    suspend fun uploadFile(file: File, folder: S3Handler.Folder): SmartResult<String, DataError.Network>
    suspend fun loadAvatar(avatarId: String): SmartResult<String, DataError.Network>

    // FRIENDS
    suspend fun inviteFriend(email: String): SmartResult<Unit, DataError.Network>
    suspend fun getFriends(): SmartResult<List<Friend>, DataError.Network>
    suspend fun getFriendRequests(): SmartResult<List<Friend>, DataError.Network>
    suspend fun acceptFriendRequest(acceptRequest: AcceptRequest): SmartResult<Unit, DataError.Network>
    suspend fun declineFriendRequest(declineRequest: DeclineRequest): SmartResult<Unit, DataError.Network>
    // legacy
    //fun getFriends(): List<Friend>
    //fun getFriendRequests(): List<FriendRequest>
//    fun acceptFriendRequest(requestId: String): Boolean
//    fun rejectFriendRequest(requestId: String): Boolean
//    fun searchUsers(query: String): List<User>


    // wall NEW
    suspend fun changeWallpaper(request: ChangeWallpaperRequest): SmartResult<Unit, DataError.Network>
    suspend fun getRecipientData(recipientId: String): SmartResult<UserDataResponse, DataError.Network>
    suspend fun getWallpaperHistory(userId: String, page: Int, pageSize: Int): SmartResult<List<WallpaperHistoryResponse>, DataError.Network>
    suspend fun react(wallpaperId: Int, reaction: String): SmartResult<Unit, DataError.Network>
    suspend fun comment(wallpaperId: Int, comment: String): SmartResult<Unit, DataError.Network>



    // WALLPAPER MANAGEMENT LEGACY
    fun isLiked(wallpaperId: String): Boolean
    fun setLiked(wallpaperId: String, isLiked: Boolean)
    fun getWallpaperDestination(): WallpaperOption
    fun setWallpaperDestination(option: WallpaperOption)
    fun getCurrentWallpaperId(): String?
    fun setCurrentWallpaperId(id: String)
    fun getPreviousWallpaperId(): String?
    fun setPreviousWallpaperId(id: String)
    fun shouldSaveIncomingWallpapers(): Boolean
    fun setSaveIncomingWallpapers(shouldSave: Boolean)
}