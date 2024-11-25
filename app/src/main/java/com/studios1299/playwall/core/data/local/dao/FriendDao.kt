package com.studios1299.playwall.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studios1299.playwall.core.data.local.entity.FriendEntity

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends WHERE status = 'accepted' ORDER BY orderIndex ASC")
    suspend fun getAllFriendsSortedByOrder(): List<FriendEntity>

    @Deprecated("No more used")
    @Query("SELECT * FROM friends WHERE status = 'accepted'")
    suspend fun getAllFriends(): List<FriendEntity>

    @Query("SELECT * FROM friends WHERE status = 'pending' AND requesterId = id")
    suspend fun getAllFriendRequests(): List<FriendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Query("DELETE FROM friends")
    suspend fun deleteAllFriends()
}
