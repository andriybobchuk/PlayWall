package com.studios1299.vrwallpaper6.core.domain.error_handling

sealed interface DataError: Error {
    enum class Network: DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNAUTHORIZED,
        CONFLICT,
        UNKNOWN
    }
    enum class Local: DataError {
        DISK_FULL
    }
}


//enum class Network: DataError {
//    WRONG_CREDENTIALS,
//    // write more here
//}