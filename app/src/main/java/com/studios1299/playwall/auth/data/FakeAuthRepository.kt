package com.studios1299.playwall.auth.data

import com.google.firebase.auth.AuthCredential
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.Result
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.domain.User
import kotlinx.coroutines.delay

//class FakeAuthRepository : AuthRepository {
//    override suspend fun login(email: String, password: String): Result<User, DataError.Network> {
//        delay(2000)
//        if(email == "test" || password == "test") {
//            return Result.Success(User("token", "andriybobchuk@gmail.com"))
//        }
//        return Result.Error(DataError.Network.SERVER_ERROR)
//    }
//
//    override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {
//        delay(2000)
//        return Result.Success(Unit)
//    }
//
//    override suspend fun googleLogin(credential: AuthCredential): Result<User, DataError.Network> {
//        TODO("Not yet implemented")
//    }
//
//    override fun logOut() {
//        TODO("Not yet implemented")
//    }
//}