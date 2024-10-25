package com.studios1299.playwall.core.domain.error_handling


typealias CustomError = Error // `Error` is already taken, so using `CustomError`

sealed interface SmartResult<out D> {
    data class Success<out D>(val data: D?): SmartResult<D>
    data class Error(
        val code: Int?,
        val message: String?,
        val errorBody: String?
    ): SmartResult<Nothing>
}

inline fun <T, R> SmartResult<T>.map(map: (T?) -> R?): SmartResult<R> {
    return when(this) {
        is SmartResult.Error -> SmartResult.Error(code, message, errorBody)
        is SmartResult.Success -> SmartResult.Success(map(data))
    }
}

fun <T> SmartResult<T>.asEmptyDataResult(): EmptyResult {
    return map {  }
}

typealias EmptyResult = SmartResult<Unit>
//
//fun <T> logSmartResult(result: SmartResult<T, DataError.Network>) {
//    when (result) {
//        is SmartResult.Success -> {
//            if (result.data is Unit) {
//                Log.d("SmartResult", "Success: No content (Unit)")
//            } else {
//                Log.d("SmartResult", "Success: ${result.data}")
//            }
//        }
//        is SmartResult.Error -> {
//            when (result.error) {
//                DataError.Network.UNAUTHORIZED -> {
//                    Log.e("SmartResult", "Error: Unauthorized")
//                }
//                DataError.Network.FORBIDDEN -> {
//                    Log.e("SmartResult", "Error: Forbidden")
//                }
//                DataError.Network.NOT_FOUND -> {
//                    Log.e("SmartResult", "Error: Not Found")
//                }
//                DataError.Network.INTERNAL_SERVER_ERROR -> {
//                    Log.e("SmartResult", "Error: Internal Server Error")
//                }
//                DataError.Network.REQUEST_TIMEOUT -> {
//                    Log.e("SmartResult", "Error: Request Timeout")
//                }
//                DataError.Network.CONFLICT -> {
//                    Log.e("SmartResult", "Error: Conflict")
//                }
//                DataError.Network.PAYLOAD_TOO_LARGE -> {
//                    Log.e("SmartResult", "Error: Payload Too Large")
//                }
//                DataError.Network.TOO_MANY_REQUESTS -> {
//                    Log.e("SmartResult", "Error: Too Many Requests")
//                }
//                DataError.Network.BAD_GATEWAY -> {
//                    Log.e("SmartResult", "Error: Bad Gateway")
//                }
//                DataError.Network.SERVICE_UNAVAILABLE -> {
//                    Log.e("SmartResult", "Error: Service Unavailable")
//                }
//                DataError.Network.GATEWAY_TIMEOUT -> {
//                    Log.e("SmartResult", "Error: Gateway Timeout")
//                }
//                DataError.Network.UNKNOWN -> {
//                    Log.e("SmartResult", "Error: Unknown Error")
//                }
//                DataError.Network.SERVER_ERROR -> {
//                    Log.e("SmartResult", "Error: Server Error")
//                }
//                DataError.Network.NOT_IMPLEMENTED -> {
//                    Log.e("SmartResult", "Error: Not Implemented")
//                }
//                else -> {
//                    Log.e("SmartResult", "Error: Unhandled error type")
//                }
//            }
//        }
//    }
//}
