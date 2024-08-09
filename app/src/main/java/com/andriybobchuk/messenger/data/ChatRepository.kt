package com.andriybobchuk.messenger.data

import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.User

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
