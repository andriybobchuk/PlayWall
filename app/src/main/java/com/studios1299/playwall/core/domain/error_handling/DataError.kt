package com.studios1299.playwall.core.domain.error_handling

sealed interface DataError: Error {
    enum class Network: DataError {
        REQUEST_TIMEOUT,
        FORBIDDEN,
        NOT_FOUND,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNAUTHORIZED,
        CONFLICT,
        INTERNAL_SERVER_ERROR,
        NOT_IMPLEMENTED,
        BAD_GATEWAY,
        SERVICE_UNAVAILABLE,
        GATEWAY_TIMEOUT,
        BAD_REQUEST,
        UNKNOWN
    }
    enum class Local: DataError {
        DISK_FULL
    }
}
