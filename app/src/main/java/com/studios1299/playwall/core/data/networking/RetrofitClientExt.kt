package com.studios1299.playwall.core.data.networking

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import retrofit2.Response


object RetrofitClientExt {

    const val LOG_TAG = "RetrofitClientExt"

    inline fun <reified T> safeCall(
        call: () -> Response<T>
    ): SmartResult<T> {
        return try {
            val response = call()
            responseToSmartResult(response)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            Log.e(LOG_TAG, "This should virtually never happen: responseToSmartResult couldn't convert response to SmartResult")
            SmartResult.Error(600, "Runtime Exception", "Originated from $LOG_TAG: \n${e.message}")
        }
    }

    inline fun <reified T> responseToSmartResult(response: Response<T>): SmartResult<T> {
        return if (response.isSuccessful) {
            SmartResult.Success(response.body())
        } else {
            // Extract detailed information from the response for logging
            val code = response.code()
            val message = response.message()
            val errorBody = response.errorBody()?.string()
            val headers = response.headers().toMultimap().toString()

            // Log the error with as much detail as possible
            Log.e(LOG_TAG, "API call failed with response code: $code")
            Log.e(LOG_TAG, "Response message: $message")
            Log.e(LOG_TAG, "Response headers: $headers")
            Log.e(LOG_TAG, "Response error body: $errorBody")

            // Details for Crashlytics
            val apiException = Exception("API call failed with code: $code, message: $message, error body: $errorBody, headers: $headers")
            // Send a detailed non-fatal report to Crashlytics
            Firebase.crashlytics.recordException(apiException)

            SmartResult.Error(code, message, errorBody?.takeUnless { it.startsWith("<") } ?: message)
        }
    }
}
