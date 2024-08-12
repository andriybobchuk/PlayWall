package com.andriybobchuk.messenger.feature.chat.presentation.pagination

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}