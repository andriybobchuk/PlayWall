package com.studios1299.vrwallpaper6.auth.domain

import com.studios1299.vrwallpaper6.core.domain.error_handling.DataError
import com.studios1299.vrwallpaper6.core.domain.error_handling.EmptyResult
import com.studios1299.vrwallpaper6.core.domain.error_handling.Result

interface AuthRepository {
    //fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
    suspend fun login(email: String, password: String): Result<User, DataError.Network>


    //fun registerUser(name: String, email: String, password: String): Flow<Resource<AuthResult>>
    suspend fun register  (email: String, password: String): EmptyResult<DataError.Network>



//    fun createBasicProfileInFirestore(profileData: ProfileData)


//    fun googleLogin(credential: AuthCredential): Flow<Resource<AuthResult>>


//    fun facebookLogin(credential: AuthCredential): Flow<Resource<AuthResult>>


    fun logOut()
    suspend fun getCurrentUserId(): String?
}

data class User(
    val id: String,
    val email: String,
)