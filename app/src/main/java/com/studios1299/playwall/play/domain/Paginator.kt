package com.studios1299.playwall.play.domain

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}