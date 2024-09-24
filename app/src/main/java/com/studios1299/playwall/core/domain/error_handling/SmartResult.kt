package com.studios1299.playwall.core.domain.error_handling

typealias CustomError = Error // `Error` is already taken, so using `CustomError`

sealed interface SmartResult<out D, out E: CustomError> {
    data class Success<out D>(val data: D): SmartResult<D, Nothing>
    data class Error<out E: CustomError>(val error: E): SmartResult<Nothing, E>
    //data class ErrorWithMessage<out E: CustomError>(val error: E, val message: String): SmartResult<Nothing, E>
}

inline fun <T, E: Error, R> SmartResult<T, E>.map(map: (T) -> R): SmartResult<R, E> {
    return when(this) {
        is SmartResult.Error -> SmartResult.Error(error)
        is SmartResult.Success -> SmartResult.Success(map(data))
        //is SmartResult.ErrorWithMessage -> SmartResult.ErrorWithMessage(error, message)
    }
}

fun <T, E: Error> SmartResult<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

typealias EmptyResult<E> = SmartResult<Unit, E>