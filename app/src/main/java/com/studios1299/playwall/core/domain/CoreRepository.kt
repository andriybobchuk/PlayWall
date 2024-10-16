package com.studios1299.playwall.core.domain

import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.response.wallpapers.ExploreWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.user.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.feature.play.presentation.play.Friend
import java.io.File

interface CoreRepository {
    // CORE
    suspend fun getCurrentUserId(): String?
    suspend fun getUserData(): SmartResult<UserDataResponse, DataError.Network>
    suspend fun updateProfile(avatarId: String?, nick: String?): SmartResult<Unit, DataError.Network>

    // S3
    suspend fun uploadFile(file: File, folder: S3Handler.Folder): SmartResult<String, DataError.Network>
    suspend fun pathToLink(path: String): SmartResult<String, DataError.Network>

    // FRIENDS
    suspend fun inviteFriend(email: String): SmartResult<Unit, DataError.Network>
    suspend fun getFriends(forceUpdate: Boolean): SmartResult<List<Friend>, DataError.Network>
    suspend fun getFriendRequests(forceUpdate: Boolean): SmartResult<List<Friend>, DataError.Network>
    suspend fun acceptFriendRequest(acceptRequest: AcceptRequest): SmartResult<Unit, DataError.Network>
    suspend fun declineFriendRequest(declineRequest: DeclineRequest): SmartResult<Unit, DataError.Network>
    suspend fun removeUser(friendshipId: Int): SmartResult<Unit, DataError.Network>
    suspend fun blockUser(friendshipId: Int, userId: Int): SmartResult<Unit, DataError.Network>
    suspend fun unblockUser(friendshipId: Int, userId: Int): SmartResult<Unit, DataError.Network>

    // WALLPAPER MANAGEMENT
    suspend fun changeWallpaper(request: ChangeWallpaperRequest): SmartResult<Unit, DataError.Network>
    suspend fun getUserDataById(recipientId: String): SmartResult<UserDataResponse, DataError.Network>
    suspend fun getWallpaperHistory(userId: String, page: Int, pageSize: Int): SmartResult<List<WallpaperHistoryResponse>, DataError.Network>
    suspend fun react(wallpaperId: Int, reaction: String?): SmartResult<Unit, DataError.Network>
    suspend fun comment(wallpaperId: Int, comment: String?): SmartResult<Unit, DataError.Network>
    suspend fun markMessagesAsRead(friendshipId: Int, lastMessageId: Int): SmartResult<Unit, DataError.Network>

    // EXPLORE WALLPAPER MANAGEMENT
    suspend fun loadExploreWallpapers(page: Int, pageSize: Int, forceRefresh: Boolean): SmartResult<List<ExploreWallpaperResponse>, DataError.Network>
    suspend fun saveWallpaper(wallpaperId: Int): SmartResult<Unit, DataError.Network>
    suspend fun removeSavedWallpaper(wallpaperId: Int): SmartResult<Unit, DataError.Network>
    suspend fun loadSavedWallpapers(page: Int, pageSize: Int): SmartResult<List<ExploreWallpaperResponse>, DataError.Network>


    suspend fun reportWallpaper(wallpaperId: Int): SmartResult<Unit, DataError.Network>

    suspend fun getFriendScreenRatio(friendId: Int): SmartResult<Float, DataError.Network>

    // PREFERENCE MANAGEMENT (DEPRECATED)
    fun getWallpaperDestination(): WallpaperOption
    fun setWallpaperDestination(option: WallpaperOption)
    fun getCurrentWallpaperId(): String?
    fun setCurrentWallpaperId(id: String)
    fun getPreviousWallpaperId(): String?
    fun setPreviousWallpaperId(id: String)
    fun shouldSaveIncomingWallpapers(): Boolean
    fun setSaveIncomingWallpapers(shouldSave: Boolean)
}