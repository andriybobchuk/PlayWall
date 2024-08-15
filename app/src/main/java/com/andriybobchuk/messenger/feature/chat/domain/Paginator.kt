package com.andriybobchuk.messenger.feature.chat.domain

interface Paginator<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}