package com.andriybobchuk.messenger.feature.auth.data

import com.andriybobchuk.messenger.core.domain.error_handling.DataError
import com.andriybobchuk.messenger.core.domain.error_handling.Result
import com.andriybobchuk.messenger.feature.auth.domain.AuthRepository
import com.andriybobchuk.messenger.feature.auth.domain.User

class AuthRepositoryImpl : AuthRepository {
    override fun loginUser(email: String, password: String): Result<User, DataError.Network> {
        TODO("Not yet implemented")

    }

    override fun registerUser(
        name: String,
        email: String,
        password: String
    ): Result<User, DataError.Network> {
        TODO("Not yet implemented")

        // API call logic
//        return try {
//            val user = User("dummy", "dummy", "dummy")
//            Result.Success(user)
//        } catch(e: HttpException) {
//            when(e.code()) {
//                408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
//                413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
//                else -> Result.Error(DataError.Network.UNKNOWN)
//            }
//        }
    }

    override fun logOut() {
        TODO("Not yet implemented")
    }
}