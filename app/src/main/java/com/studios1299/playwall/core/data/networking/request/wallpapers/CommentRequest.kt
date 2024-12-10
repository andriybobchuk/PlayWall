package com.studios1299.playwall.core.data.networking.request.wallpapers

data class CommentRequest(
    val wallpaperId: Int,
    val comment: String?
)