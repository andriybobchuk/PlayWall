package com.andriybobchuk.messenger.feature.chat.domain

import com.andriybobchuk.messenger.feature.chat.data.model.Message
import com.andriybobchuk.messenger.feature.chat.data.model.User

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
