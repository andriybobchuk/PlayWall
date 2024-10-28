package com.studios1299.playwall.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studios1299.playwall.core.data.local.entity.MessageEntity

@Dao
interface ChatDao {
    @Query("SELECT * FROM messages WHERE requesterId = :userId OR recipientId = :userId ORDER BY timeSent DESC LIMIT :pageSize OFFSET :page * :pageSize")
    suspend fun getMessagesForUser(userId: Int, page: Int, pageSize: Int): List<MessageEntity>

    @Query("SELECT COUNT(*) FROM messages WHERE requesterId = :userId OR recipientId = :userId")
    suspend fun getMessageCountForUser(userId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
}
