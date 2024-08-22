package com.studios1299.vrwallpaper6.feature.chat.domain

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}