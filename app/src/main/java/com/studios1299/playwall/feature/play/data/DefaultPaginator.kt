package com.studios1299.playwall.feature.play.data

import android.util.Log
import com.studios1299.playwall.core.domain.error_handling.CustomError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.feature.play.domain.Paginator

class DefaultPaginator<Key, Item>(
    private val initialKey: Key,
    private inline val onLoadUpdated: (Boolean) -> Unit,
    private inline val onRequest: suspend (nextKey: Key) -> SmartResult<List<Item>>,
    private inline val getNextKey: suspend (List<Item>) -> Key,
    private inline val onError: suspend (Throwable?) -> Unit,
    private inline val onSuccess: suspend (items: List<Item>, newKey: Key) -> Unit
): Paginator<Key, Item> {

    companion object {
        const val LOG_TAG = "DefaultPaginator"
    }

    private var currentKey = initialKey
    private var isMakingRequest = false

    override suspend fun loadNextItems() {
        if(isMakingRequest) {
            return
        }
        isMakingRequest = true
        onLoadUpdated(true)
        val result = onRequest(currentKey)
        isMakingRequest = false
        val items = when (result) {
            is SmartResult.Success -> {
                Log.e(LOG_TAG, "Elements in loadNextItems(): ${result.data}")
                result.data
            }
            is SmartResult.Error -> {
                Log.e(LOG_TAG, "Error in loadNextItems(): ${result.errorBody}")
                emptyList()
            }
        }

        currentKey = getNextKey(items!!)
        onSuccess(items, currentKey)
        onLoadUpdated(false)
    }

    override fun reset() {
        currentKey = initialKey
    }
}