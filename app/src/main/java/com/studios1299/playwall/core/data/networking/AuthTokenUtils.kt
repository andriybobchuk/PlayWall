//package com.studios1299.playwall.core.data.networking
//
//object AuthTokenUtils {
//    fun withAuthToken(
//        onTokenReceived: (authToken: String?) -> Unit,
//        onFailure: () -> Unit = {}
//    ) {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val authToken = task.result?.token
//                onTokenReceived(authToken)
//            } else {
//                Log.e("TokenUtils", "Failed to get Firebase ID token")
//                onFailure.invoke()
//            }
//        } ?: run {
//            Log.e("TokenUtils", "No authenticated user found")
//            onFailure.invoke()
//        }
//    }
//
//    fun withUserData(
//        onSuccess: (result: Any) -> Unit,
//        onFailure: () -> Unit = {}
//    ) {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val result = task.result
//                onSuccess(result)
//            } else {
//                Log.e("TokenUtils", "Failed to get Firebase ID token")
//                onFailure.invoke()
//            }
//        } ?: run {
//            Log.e("TokenUtils", "No authenticated user found")
//            onFailure.invoke()
//        }
//    }
//}