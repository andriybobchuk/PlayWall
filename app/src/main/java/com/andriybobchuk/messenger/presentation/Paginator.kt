package com.andriybobchuk.messenger.presentation

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}