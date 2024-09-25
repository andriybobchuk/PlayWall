package com.studios1299.playwall.core.presentation

import com.studios1299.playwall.R
import com.studios1299.playwall.core.domain.error_handling.DataError

fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(
            R.string.the_request_timed_out
        )

        DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(
            R.string.youve_hit_your_rate_limit
        )

        DataError.Network.NO_INTERNET -> UiText.StringResource(
            R.string.no_internet
        )

        DataError.Network.PAYLOAD_TOO_LARGE -> UiText.StringResource(
            R.string.file_too_large
        )

        DataError.Network.SERVER_ERROR -> UiText.StringResource(
            R.string.server_error
        )

        DataError.Network.SERIALIZATION -> UiText.StringResource(
            R.string.error_serialization
        )

        DataError.Network.UNKNOWN -> UiText.StringResource(
            R.string.unknown_error
        )

        DataError.Local.DISK_FULL -> UiText.StringResource(
            R.string.error_disk_full
        )

        DataError.Network.UNAUTHORIZED -> UiText.StringResource(
            R.string.error_unauthorized
        )

        DataError.Network.CONFLICT -> UiText.StringResource(
            R.string.error_conflict
        )

        DataError.Network.NOT_FOUND -> TODO()
        DataError.Network.INTERNAL_SERVER_ERROR -> TODO()
        DataError.Network.NOT_IMPLEMENTED -> TODO()
        DataError.Network.BAD_GATEWAY -> TODO()
        DataError.Network.SERVICE_UNAVAILABLE -> TODO()
        DataError.Network.GATEWAY_TIMEOUT -> TODO()
        DataError.Network.FORBIDDEN -> UiText.DynamicString("Forbidden")
        DataError.Network.BAD_REQUEST -> UiText.DynamicString("Bad request")
    }
}