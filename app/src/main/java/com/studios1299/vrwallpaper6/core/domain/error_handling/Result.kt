package com.studios1299.vrwallpaper6.core.domain.error_handling

typealias CustomError = Error // `Error` is already taken, so using `RootError`

sealed interface Result<out D, out E: CustomError> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: CustomError>(val error: E): Result<Nothing, E>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map {  }
}

typealias EmptyResult<E> = Result<Unit, E>