package com.studios1299.vrwallpaper6.feature.auth.data

import com.studios1299.vrwallpaper6.core.domain.error_handling.DataError
import com.studios1299.vrwallpaper6.core.domain.error_handling.EmptyResult
import com.studios1299.vrwallpaper6.core.domain.error_handling.Result
import com.studios1299.vrwallpaper6.feature.auth.domain.AuthRepository
import com.studios1299.vrwallpaper6.feature.auth.domain.User
import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User, DataError.Network> {

        delay(2000)

        return Result.Error(DataError.Network.SERVER_ERROR)

    }

    override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {

        delay(2000)

        return Result.Success(Unit)
    }

//    override suspend fun register(
//        name: String,
//        email: String,
//        password: String
//    ): Result<User, DataError.Network> {
//        TODO("Not yet implemented")
//
//        // API call logic
////        return try {
////            val user = User("dummy", "dummy", "dummy")
////            Result.Success(user)
////        } catch(e: HttpException) {
////            when(e.code()) {
////                408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
////                413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
////                else -> Result.Error(DataError.Network.UNKNOWN)
////            }
////        }
//    }

    override fun logOut() {
        TODO("Not yet implemented")
    }
}