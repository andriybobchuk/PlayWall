package com.studios1299.playwall.core.data.networking

data class CreateUserRequest(
    val firebaseId: String,
    val email: String,
    val pushToken: String
)