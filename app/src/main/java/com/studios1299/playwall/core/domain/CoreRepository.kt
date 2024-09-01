package com.studios1299.playwall.core.domain

import com.studios1299.playwall.core.data.UserProfile
import com.studios1299.playwall.explore.presentation.explore.Photo
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.play.Friend
import com.studios1299.playwall.feature.play.presentation.play.FriendRequest

interface CoreRepository {
    // CORE
    suspend fun getCurrentUserId(): String?
    suspend fun getUserProfile(): UserProfile

    // EXPLORE
    suspend fun getExploreItems(): List<Photo>

    // CHAT
    suspend fun retrieveMessages(page: Int, pageSize: Int): Result<List<Message>>
    fun getLastMessageId(): Result<String>
    fun addMessage(message: Message)
    fun deleteMessage(messageId: String)
    fun updateMessage(message: Message)
    fun getCurrentUser(): User
    fun getRecipient(): User
    fun getUserNameById(userId: String): String

    // FRIENDS
    fun getFriends(): List<Friend>
    fun getFriendRequests(): List<FriendRequest>
    fun acceptFriendRequest(requestId: String): Boolean
    fun rejectFriendRequest(requestId: String): Boolean
    fun searchUsers(query: String): List<User>


    fun getLikeCount(photoId: String): Int
}