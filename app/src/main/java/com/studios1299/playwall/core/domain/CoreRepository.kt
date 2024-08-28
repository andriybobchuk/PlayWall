package com.studios1299.playwall.core.domain

import com.studios1299.playwall.core.data.UserProfile

interface CoreRepository {
    suspend fun getCurrentUserId(): String?
    suspend fun getUserProfile(): UserProfile
}