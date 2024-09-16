package com.studios1299.playwall.core.data.networking

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call

class PushTokenUtils {
    companion object {
        fun updateUserPushToken(token: String? = null) {
//            withAuthToken(
//                onTokenReceived = { authToken ->
//                    if (token != null) {
//                        val pushToken = PushTokenRequest(token)
//                        sendRequest(pushToken, authToken)
//                    } else {
//                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                val pushToken = PushTokenRequest(task.result)
//                                sendRequest(pushToken, authToken)
//                                Log.d("TokenUtils", "Device␣token␣retrieved:␣${task.result}")
//                            } else {
//                                Log.w(
//                                    "TokenUtils", "Fetching␣FCM␣registration␣token␣failed",
//                                    task.exception
//                                )
//                            }
//                        }
//                        private fun sendRequest(
//                            pushTokenRequest: PushTokenRequest,
//                            authToken: String?
//                        ) {
//                            RetrofitClient.userService.updatePushToken(
//                                "Bearer␣$authToken",
//                                pushTokenRequest
//                            )
//                            )
//                        }
//                    }
//                }
//                    .enqueue(object : retrofit2.Callback<Void> {
//                        override fun onFailure(call: Call<Void>, t: Throwable) {
//                            Log.e(
//                                "PushTokenUtils", "Push␣token␣update␣error:␣${
//                                    t.localizedMessage
//                                }"
//                            )
//                        }
//
//                        override fun onResponse(
//                            call: Call<Void>, response: retrofit2.Response<
//                                    Void>
//                        ) {
//                            if (response.isSuccessful) {
//                                Log.d("PushTokenUtils", "User’s␣push␣token␣updated␣successfully.")
//                            } else {
//                                Log.e("PushTokenUtils",
//                                    "Push␣token␣update␣error:␣${response.errorBody()?.string()}")
//                            }
//
//                        }
//                    })
        }
    }
}