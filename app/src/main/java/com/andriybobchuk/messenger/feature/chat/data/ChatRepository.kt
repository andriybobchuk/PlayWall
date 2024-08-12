package com.andriybobchuk.messenger.feature.chat.data

import com.andriybobchuk.messenger.feature.chat.model.Message
import com.andriybobchuk.messenger.feature.chat.model.User

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
