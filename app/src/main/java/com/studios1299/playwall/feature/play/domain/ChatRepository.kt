package com.studios1299.playwall.feature.play.domain

import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.play.Friend
import com.studios1299.playwall.feature.play.presentation.play.FriendRequest

interface ChatRepository {
    suspend fun retrieveMessages(page: Int, pageSize: Int): Result<List<Message>>
    fun getLastMessageId(): Result<String>
    fun addMessage(message: Message)
    fun deleteMessage(messageId: String)
    fun updateMessage(message: Message)
    fun getCurrentUser(): User
    fun getRecipient(): User
    fun getUserNameById(userId: String): String


    fun getFriends(): List<Friend>
    fun getFriendRequests(): List<FriendRequest>
    fun acceptFriendRequest(requestId: String): Boolean
    fun rejectFriendRequest(requestId: String): Boolean
    fun searchUsers(query: String): List<User>
}
