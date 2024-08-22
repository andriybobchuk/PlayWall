package com.studios1299.vrwallpaper6.feature.chat.domain

import com.studios1299.vrwallpaper6.feature.chat.data.model.Message
import com.studios1299.vrwallpaper6.feature.chat.data.model.User

interface ChatRepository {
    suspend fun retrieveMessages(page: Int, pageSize: Int): Result<List<Message>>
    fun getLastMessageId(): Result<String>
    fun addMessage(message: Message)
    fun deleteMessage(messageId: String)
    fun updateMessage(message: Message)
    fun getCurrentUser(): User
    fun getRecipient(): User
    fun getUserNameById(userId: String): String
}
