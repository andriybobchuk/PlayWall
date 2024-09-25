package com.studios1299.playwall.core.data.networking

import android.util.Log
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import retrofit2.Response


object RetrofitClientExt {

    const val LOG_TAG = "RetrofitClientExt"

    inline fun <reified T> safeCall(
        call: () -> Response<T>
    ): SmartResult<T, DataError.Network> {
        return try {
            val response = call()
            responseToSmartResult(response)
        } catch (e: Exception) {
            Log.e("RetrofitClientExt.safeCall()", "safeCall just saved you from exception: ${e.message}")
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    inline fun <reified T> responseToSmartResult(response: Response<T>): SmartResult<T, DataError.Network> {
        Log.e(LOG_TAG, "responseToSmartResult(): Response message: ${response.message()}")
        return when {
            response.isSuccessful -> {
                val body = response.body()
                if (body != null) {
                    SmartResult.Success(body)
                } else {
                    SmartResult.Error(DataError.Network.UNKNOWN)
                }
            }
            response.code() == 401 -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
            response.code() == 403 -> SmartResult.Error(DataError.Network.FORBIDDEN)
            response.code() == 404 -> SmartResult.Error(DataError.Network.NOT_FOUND)
            response.code() == 408 -> SmartResult.Error(DataError.Network.REQUEST_TIMEOUT)
            response.code() == 409 -> SmartResult.Error(DataError.Network.CONFLICT)
            response.code() == 413 -> SmartResult.Error(DataError.Network.PAYLOAD_TOO_LARGE)
            response.code() == 429 -> SmartResult.Error(DataError.Network.TOO_MANY_REQUESTS)
            response.code() == 500 -> SmartResult.Error(DataError.Network.INTERNAL_SERVER_ERROR)
            response.code() == 501 -> SmartResult.Error(DataError.Network.NOT_IMPLEMENTED)
            response.code() == 502 -> SmartResult.Error(DataError.Network.BAD_GATEWAY)
            response.code() == 503 -> SmartResult.Error(DataError.Network.SERVICE_UNAVAILABLE)
            response.code() == 504 -> SmartResult.Error(DataError.Network.GATEWAY_TIMEOUT)
            response.code() in 505..599 -> SmartResult.Error(DataError.Network.SERVER_ERROR)
            else -> SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }
}
