package com.studios1299.playwall.core.domain

import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.response.wallpapers.ExploreWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.user.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.ChangeWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.presentation.play.Friend
import java.io.File

interface CoreRepository {
    // CORE
    suspend fun getCurrentUserId(): String?
    suspend fun getUserData(): SmartResult<UserDataResponse>
    suspend fun updateProfile(avatarId: String?, nick: String?): SmartResult<Unit>

    // S3
    suspend fun uploadFile(file: File, folder: S3Handler.Folder): SmartResult<String>
    suspend fun pathToLink(path: String): SmartResult<String>

    // FRIENDS
    suspend fun inviteFriend(email: String): SmartResult<Unit>
    suspend fun getFriends(forceUpdate: Boolean): SmartResult<List<Friend>>
    suspend fun getFriendRequests(forceUpdate: Boolean): SmartResult<List<Friend>>
    suspend fun acceptFriendRequest(acceptRequest: AcceptRequest): SmartResult<Unit>
    suspend fun declineFriendRequest(declineRequest: DeclineRequest): SmartResult<Unit>
    suspend fun removeUser(friendshipId: Int): SmartResult<Unit>
    suspend fun blockUser(friendshipId: Int, userId: Int): SmartResult<Unit>
    suspend fun unblockUser(friendshipId: Int, userId: Int): SmartResult<Unit>

    // WALLPAPER MANAGEMENT
    suspend fun changeWallpaper(request: ChangeWallpaperRequest): SmartResult<ChangeWallpaperResponse?>
    suspend fun getUserDataById(recipientId: String): SmartResult<UserDataResponse>
    suspend fun getWallpaperHistory(userId: String, page: Int, pageSize: Int, forceUpdate: Boolean): SmartResult<List<WallpaperHistoryResponse>>
    suspend fun react(wallpaperId: Int, reaction: String?): SmartResult<Unit>
    suspend fun comment(wallpaperId: Int, comment: String?): SmartResult<Unit>
    suspend fun markMessagesAsRead(friendshipId: Int, lastMessageId: Int): SmartResult<Unit>
    suspend fun saveMessageToLocal(message: Message)

    // EXPLORE WALLPAPER MANAGEMENT
    suspend fun loadExploreWallpapers(page: Int, pageSize: Int, forceRefresh: Boolean): SmartResult<List<ExploreWallpaperResponse>>
    suspend fun saveWallpaper(wallpaperId: Int): SmartResult<Unit>
    suspend fun removeSavedWallpaper(wallpaperId: Int): SmartResult<Unit>
    suspend fun loadSavedWallpapers(page: Int, pageSize: Int): SmartResult<List<ExploreWallpaperResponse>>

    suspend fun reportWallpaper(wallpaperId: Int): SmartResult<Unit>
    suspend fun getFriendScreenRatio(friendId: Int): SmartResult<Float>

    // MONETIZATION
    fun addDevils(count: Int)
    fun getDevilCount(): Int
    fun updatePremiumStatus(isPremium: Boolean)
    fun getLastCheckInDate(): String?
    fun setLastCheckInDate(date: String)
    fun getConsecutiveDays(): Int
    fun setConsecutiveDays(days: Int)
    fun hasCheckedInToday(): Boolean


    fun markCheckedInToday()
    fun resetDailyCheckin()

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