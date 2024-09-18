package com.studios1299.playwall.core.data.networking

import android.util.Log
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import retrofit2.Response


object RetrofitClientExt {
    inline fun <reified T> safeCall(
        call: () -> Response<T>
    ): SmartResult<T, DataError.Network> {
        return try {
            val response = call()
            responseToSmartResult(response)
        } catch (e: Exception) {
            Log.e("RetrofitClientExt.safeCall()", "safeCall just saved you from exception.")
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    inline fun <reified T> responseToSmartResult(response: Response<T>): SmartResult<T, DataError.Network> {
        return when {
            response.isSuccessful -> {
                val body = response.body()
                if (body != null) {
                    SmartResult.Success(body)
                } else {
                    SmartResult.Error(DataError.Network.UNKNOWN) // Handle case where body is null
                }
            }
            response.code() == 401 -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
            response.code() == 408 -> SmartResult.Error(DataError.Network.REQUEST_TIMEOUT)
            response.code() == 409 -> SmartResult.Error(DataError.Network.CONFLICT)
            response.code() == 413 -> SmartResult.Error(DataError.Network.PAYLOAD_TOO_LARGE)
            response.code() == 429 -> SmartResult.Error(DataError.Network.TOO_MANY_REQUESTS)
            response.code() in 500..599 -> SmartResult.Error(DataError.Network.SERVER_ERROR)
            else -> SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

}
