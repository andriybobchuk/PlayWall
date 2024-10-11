package com.studios1299.playwall.auth.data

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.messaging.FirebaseMessaging
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.domain.User
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.request.user.CreateUserRequest
import com.studios1299.playwall.core.data.networking.RetrofitClientExt
import com.studios1299.playwall.core.data.networking.RetrofitClient
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.asEmptyDataResult
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
) : AuthRepository {
    override suspend fun login(email: String, password: String): SmartResult<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val idToken = firebaseUser.getIdToken(false).await().token
                Preferences.setAuthToken(idToken!!)

                val fcmToken = Preferences.getFcmToken()
                if (fcmToken != null) {

                    try {
                        val requestBody = mapOf("pushToken" to fcmToken)
                        val res = RetrofitClient.userApi.updatePushToken("Bearer $idToken", requestBody)
                        if(res.isSuccessful){
                            Log.e("Login", "FCM token updated after login")
                        } else {
                            Log.e("Login", "FCM token fucked " + res)
                        }
                    } catch (e: Exception) {
                        Log.e("Login", "Exception"+ e.message)
                    }

                } else {
                    Log.e("Login", "Why isn't there any FCM token in Preferences?")
                }

                SmartResult.Success(User(firebaseUser.uid, firebaseUser.email ?: ""))
            } else {
                SmartResult.Error(DataError.Network.UNAUTHORIZED)
            }
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
                "ERROR_USER_NOT_FOUND" -> SmartResult.Error(DataError.Network.NOT_FOUND)
                else -> SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun register(email: String, password: String, screenRatio: Float): EmptyResult<DataError.Network> {
        return try {
            // Create user in Firebase
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            Preferences.setAuthToken(firebaseIdToken)

            // Create user in DB
            val createUserResult = createUser(
                userId = user.uid,
                email = email,
                firebaseIdToken = firebaseIdToken,
                screenRatio = screenRatio
            )
            if (createUserResult is SmartResult.Error) {
                firebaseAuth.signOut()
                return createUserResult
            }

            // Update the push token after successfully creating the user
            val updateTokenResult = updatePushToken(firebaseIdToken)
            if (updateTokenResult is SmartResult.Error) {
                return updateTokenResult
            }

            return SmartResult.Success(Unit)

        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> SmartResult.Error(DataError.Network.CONFLICT)
                else -> SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }.asEmptyDataResult()
    }

    suspend fun createUser(
        userId: String,
        email: String,
        firebaseIdToken: String,
        screenRatio: Float
    ): EmptyResult<DataError.Network> {
        return try {
            val authHeader = "Bearer $firebaseIdToken"

            val requestBody = CreateUserRequest(
                email = email,
                firebaseId = userId,
                screenRatio = screenRatio
            )

            return RetrofitClientExt.safeCall {
                RetrofitClient.userApi.createUser(authHeader, requestBody)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    suspend fun updatePushToken(firebaseIdToken: String): EmptyResult<DataError.Network> {
        return try {
            val firebaseFcmToken = firebaseMessaging.token.await()

            val authHeader = "Bearer $firebaseIdToken"
            val requestBody = mapOf("pushToken" to firebaseFcmToken)

            return RetrofitClientExt.safeCall {
                RetrofitClient.userApi.updatePushToken(authHeader, requestBody)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

//    override suspend fun register(email: String, password: String, screenRatio: Float): EmptyResult<DataError.Network> {
//        return try {
//            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
//            val user = authResult.user ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
//            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
//            Preferences.setAuthToken(firebaseIdToken)
//            val firebaseFcmToken = firebaseMessaging.token.await() // For PUSH notifications
//
//            val result = createUser(
//                userId = user.uid,
//                email = email,
//                firebaseIdToken = firebaseIdToken,
//                pushToken = firebaseFcmToken
//            )
//            if (result is SmartResult.Error) {
//                firebaseAuth.signOut()
//            }
//            return result
//        } catch (e: FirebaseAuthException) {
//            when (e.errorCode) {
//                "ERROR_EMAIL_ALREADY_IN_USE" -> SmartResult.Error(DataError.Network.CONFLICT)
//                else -> SmartResult.Error(DataError.Network.UNKNOWN)
//            }
//        } catch (e: Exception) {
//            SmartResult.Error(DataError.Network.UNKNOWN)
//        }.asEmptyDataResult()
//    }
//
//    suspend fun createUser(
//        userId: String,
//        email: String,
//        firebaseIdToken: String,
//        pushToken: String
//    ): EmptyResult<DataError.Network> {
//        return try {
//            val authHeader = "Bearer $firebaseIdToken"
//            Log.e("CreateUser", "Authorization header: $authHeader")
//
//            val requestBody = CreateUserRequest(
//                email = email,
//                firebaseId = userId,
//                pushToken = pushToken
//            )
//
//            return RetrofitClientExt.safeCall {
//                RetrofitClient.userApi.createUser(authHeader, requestBody)
//            }.also { result ->
//                when (result) {
//                    is SmartResult.Success -> {
//                        Log.d("CreateUser", "User created successfully.")
//                    }
//                    is SmartResult.Error -> {
//                        Log.e("CreateUser", "Failed to create user. Error: ${result.error}")
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("CreateUser", "Exception occurred while creating user: ${e.message}", e)
//            SmartResult.Error(DataError.Network.UNKNOWN)
//        }
//    }

    override suspend fun googleRegister(credential: AuthCredential, screenRatio: Float): SmartResult<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            Preferences.setAuthToken(firebaseIdToken)
            //val firebaseFcmToken = firebaseMessaging.token.await() // For PUSH notifications

            val createUserResult = createUser(
                userId = user.uid,
                email = user.email ?: "",
                firebaseIdToken = firebaseIdToken,
                screenRatio = screenRatio
            )
            if (createUserResult is SmartResult.Error) {
                firebaseAuth.signOut()
                return createUserResult
            }

            // Update the push token after successfully creating the user
            val updateTokenResult = updatePushToken(firebaseIdToken)
            if (updateTokenResult is SmartResult.Error) {
                return updateTokenResult
            }

            return SmartResult.Success(User(user.uid, user.email ?: ""))
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
                "ERROR_USER_NOT_FOUND" -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
                else -> SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun googleLogin(credential: AuthCredential): SmartResult<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            Preferences.setAuthToken(firebaseIdToken)

            // Update the push token after successfully login
            val updateTokenResult = updatePushToken(firebaseIdToken)
            if (updateTokenResult is SmartResult.Error) {
                Log.e("Google login", updateTokenResult.error.toString())
                //return updateTokenResult
            }

            return SmartResult.Success(User(user.uid, user.email ?: ""))
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
                "ERROR_USER_NOT_FOUND" -> SmartResult.Error(DataError.Network.UNAUTHORIZED)
                else -> SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override fun logOut() {
        firebaseAuth.signOut()
    }

//    override suspend fun updatePushToken(pushToken: String): SmartResult<Unit, DataError.Network> {
//        return try {
//            val response = performAuthRequest { token ->
//
//            }
//            if (response is SmartResult.Success) {
//                Log.e("FcmService", "New token was updated")
//                SmartResult.Success(Unit)
//            } else {
//                Log.e("FcmService", "New token was NOT updated")
//                SmartResult.Error(DataError.Network.UNKNOWN)
//            }
//        } catch (e: Exception) {
//            Log.e("FcmService", "New token was excepted" + e.message)
//            SmartResult.Error(DataError.Network.UNKNOWN)
//        }
//    }

}