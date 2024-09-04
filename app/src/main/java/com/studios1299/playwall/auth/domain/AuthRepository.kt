package com.studios1299.playwall.auth.domain

import com.google.firebase.auth.AuthCredential
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User, DataError.Network>
    suspend fun register  (email: String, password: String): EmptyResult<DataError.Network>
    suspend fun googleLogin(credential: AuthCredential): Result<User, DataError.Network>
    fun logOut()
}

data class User(
    val id: String,
    val email: String,
)