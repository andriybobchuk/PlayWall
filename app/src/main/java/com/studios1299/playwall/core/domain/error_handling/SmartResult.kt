package com.studios1299.playwall.core.domain.error_handling

sealed interface SmartResult<out D> {
    data class Success<out D>(val data: D?): SmartResult<D>
    data class Error(
        val code: Int?,
        val message: String?,
        val errorBody: String?
    ): SmartResult<Nothing>
}

inline fun <T, R> SmartResult<T>.map(map: (T?) -> R?): SmartResult<R> {
    return when(this) {
        is SmartResult.Error -> SmartResult.Error(code, message, errorBody)
        is SmartResult.Success -> SmartResult.Success(map(data))
    }
}

fun <T> SmartResult<T>.asEmptyDataResult(): EmptyResult {
    return map {  }
}

typealias EmptyResult = SmartResult<Unit>
