package com.andriybobchuk.messenger.feature.auth.domain

import com.andriybobchuk.messenger.core.domain.error_handling.DataError
import com.andriybobchuk.messenger.core.domain.error_handling.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    //fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
    fun loginUser(email: String, password: String): Result<User, DataError.Network>


    //fun registerUser(name: String, email: String, password: String): Flow<Resource<AuthResult>>
    fun registerUser(name: String, email: String, password: String): Result<User, DataError.Network>



//    fun createBasicProfileInFirestore(profileData: ProfileData)


//    fun googleLogin(credential: AuthCredential): Flow<Resource<AuthResult>>


//    fun facebookLogin(credential: AuthCredential): Flow<Resource<AuthResult>>


    fun logOut()
}

data class User(
    val fullName: String,
    val token: String,
    val email: String,
)