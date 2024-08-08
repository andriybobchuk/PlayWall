package com.andriybobchuk.messenger.presentation.pagination

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}