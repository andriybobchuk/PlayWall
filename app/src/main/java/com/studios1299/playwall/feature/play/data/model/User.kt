package com.studios1299.playwall.feature.play.data.model

data class User(
    val id: String,
    val name: String,
    val profilePictureUrl: String,
    val lastOnline: Long = 0,
    val email: String = "random@email.com"
)