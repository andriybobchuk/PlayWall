package com.studios1299.playwall.feature.play.domain

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}