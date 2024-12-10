package com.studios1299.playwall.core.data.networking.response

data class ApiResponse<T>(
    val success: Boolean,
    val errorCode: String? = null,
    val message: String? = null,
    val data: T? = null
)