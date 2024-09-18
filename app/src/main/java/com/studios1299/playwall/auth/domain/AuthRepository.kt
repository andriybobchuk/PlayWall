package com.studios1299.playwall.auth.domain

import com.google.firebase.auth.AuthCredential
import com.studios1299.playwall.core.data.networking.CreateUserRequest
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.SmartResult

interface AuthRepository {
    suspend fun login(email: String, password: String): SmartResult<User, DataError.Network>
    suspend fun register  (email: String, password: String): EmptyResult<DataError.Network>
    suspend fun googleLogin(credential: AuthCredential): SmartResult<User, DataError.Network>
    fun logOut()
}

data class User(
    val id: String,
    val email: String,
)