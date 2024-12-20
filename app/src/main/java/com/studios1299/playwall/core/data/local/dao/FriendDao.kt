package com.studios1299.playwall.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studios1299.playwall.core.data.local.entity.FriendEntity

@Dao
interface FriendDao {

    @Query("""
    SELECT * 
    FROM friends 
    WHERE status = 'accepted' 
       OR status = 'blocked' AND id <> :userId
       OR (status = 'pending' AND id <> :userId)
    ORDER BY orderIndex ASC
""")
    suspend fun getAllFriendsSortedByOrder(userId: String): List<FriendEntity>

    @Deprecated(
        message = "No more used",
        level = DeprecationLevel.ERROR
    )
    @Query("SELECT * FROM friends WHERE status = 'accepted'")
    suspend fun getAllFriends(): List<FriendEntity>

    @Query("SELECT * FROM friends WHERE status = 'pending' AND requesterId = id")
    suspend fun getAllFriendRequests(): List<FriendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Query("DELETE FROM friends")
    suspend fun deleteAllFriends()
}
