package com.andriybobchuk.messenger.core.domain.error_handling

typealias RootError = Error // `Error` is already taken, so using `RootError`

sealed interface Result<out D, out E: RootError> {
    data class Success<out D, out E: RootError>(val data: D): Result<D, E>
    data class Error<out D, out E: RootError>(val error: E): Result<D, E>
}