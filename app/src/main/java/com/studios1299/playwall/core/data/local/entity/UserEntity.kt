package com.studios1299.playwall.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val avatarId: String,
    val since: String,
    val status: String,
    val requesterId: Int,
    val friendshipId: Int,
    val screenRatio: Float
)
