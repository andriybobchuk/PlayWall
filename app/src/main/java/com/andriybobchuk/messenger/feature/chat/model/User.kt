package com.andriybobchuk.messenger.feature.chat.model

data class User(
    val id: String,
    val name: String,
    val profilePictureUrl: String,
    val lastOnline: Long
)