package com.studios1299.playwall.core.domain

import com.studios1299.playwall.core.data.UserProfile
import com.studios1299.playwall.explore.presentation.Photo

interface CoreRepository {
    suspend fun getCurrentUserId(): String?
    suspend fun getUserProfile(): UserProfile
    suspend fun getExploreItems(): List<Photo>
}