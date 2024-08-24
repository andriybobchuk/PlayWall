package com.studios1299.playwall.core.domain

interface CoreRepository {
    suspend fun getCurrentUserId(): String?
}