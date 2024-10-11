package com.studios1299.playwall.core.data.networking.request.user

data class CreateUserRequest(
    val firebaseId: String,
    val email: String,
    val screenRatio: Float,
)