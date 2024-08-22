package com.studios1299.vrwallpaper6.core.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * This wrapper class is a convenient usage of string recourses instead of hardcoded strings.
 * It accommodates for both the Dynamic strings (those from the API) and Static StringResource
 * strings. Also it can be called from both within the Composable or outside of the composable
 * (if you pass a context)
 *
 * Usage example:
 * ```kotlin
 * UiText.StringResource(
 *        resId = R.string.min_name_length_error,
 *        MIN_NAME_LENGTH
 *  )
 *  ```
 */
sealed class UiText {
    data class DynamicString(val value: String): UiText()
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ): UiText()

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}