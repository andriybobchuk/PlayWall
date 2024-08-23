package com.studios1299.vrwallpaper6.feature.play.domain

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}