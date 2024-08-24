package com.studios1299.vrwallpaper6.play.domain

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}