package com.studios1299.playwall.core.data.networking.request.wallpapers

data class ReportRequest(
    val wallpaperId: Int?,
    val reportedUserId: Int
)