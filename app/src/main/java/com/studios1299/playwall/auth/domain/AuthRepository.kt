package com.studios1299.playwall.auth.domain

import com.google.firebase.auth.AuthCredential
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.SmartResult

interface AuthRepository {
    suspend fun login(email: String, password: String): SmartResult<User>
    suspend fun register  (email: String, password: String, screenRatio: Float): EmptyResult
    suspend fun googleLogin(credential: AuthCredential): SmartResult<User>
    suspend fun googleRegister(credential: AuthCredential, screenRatio: Float): SmartResult<User>
    suspend fun sendPasswordResetEmail(email: String): SmartResult<Unit>
    suspend fun updatePassword(currentPassword: String, newPassword: String): SmartResult<Unit>
    fun logOut()
    suspend fun updatePushToken(): EmptyResult
    suspend fun deletePushToken(): EmptyResult
}

data class User(
    val id: String,
    val email: String,
)