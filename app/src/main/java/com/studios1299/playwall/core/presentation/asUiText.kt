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

        DataError.Network.NOT_FOUND -> UiText.DynamicString("Not found")
        DataError.Network.INTERNAL_SERVER_ERROR -> UiText.DynamicString("Internal server error")
        DataError.Network.NOT_IMPLEMENTED -> UiText.DynamicString("Not implemented")
        DataError.Network.BAD_GATEWAY -> UiText.DynamicString("Bad gateway")
        DataError.Network.SERVICE_UNAVAILABLE -> UiText.DynamicString("Service unavailable")
        DataError.Network.GATEWAY_TIMEOUT -> UiText.DynamicString("Gateway timeout")
        DataError.Network.FORBIDDEN -> UiText.DynamicString("Forbidden")
        DataError.Network.BAD_REQUEST -> UiText.DynamicString("Bad request")
    }
}