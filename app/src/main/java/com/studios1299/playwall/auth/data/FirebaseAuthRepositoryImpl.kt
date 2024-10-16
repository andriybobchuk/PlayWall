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
import retrofit2.Response

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
) : AuthRepository {

    companion object {
        private const val LOG_TAG = "FirebaseAuthRepositoryImpl"
    }

    override suspend fun login(email: String, password: String): SmartResult<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val idToken = firebaseUser.getIdToken(false).await().token
                Preferences.setAuthToken(idToken!!)

                val updateTokenResult = updatePushToken()
                Log.e("Login", "FCM token updated after login: $updateTokenResult")

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
                //firebaseAuth.signOut()
                return createUserResult
            }

            // Update the push token after successfully creating the user
            val updateTokenResult = updatePushToken()
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

    override suspend fun updatePushToken(): EmptyResult<DataError.Network> {
        return try {
            performAuthRequest { token ->
                Log.e("updatePushToken", "Start updating FCM token in the repo")
                val firebaseFcmToken = firebaseMessaging.token.await()
                Preferences.setFcmToken(firebaseFcmToken)
                val requestBody = mapOf("pushToken" to firebaseFcmToken)

                Log.e("updatePushToken", "Finished updating FCM token in the repo")
                RetrofitClient.userApi.updatePushToken(token, requestBody)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    private suspend fun getFirebaseToken(): String? {
        return Preferences.getAuthToken() ?: refreshFirebaseToken().let {
            if (it is SmartResult.Success) it.data else null
        }
    }

    private suspend fun refreshFirebaseToken(): SmartResult<String, DataError.Network> {
        return try {
            val user = firebaseAuth.currentUser ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)

            val token = user.getIdToken(true).await().token
            if (token != null) {
                Preferences.setAuthToken(token)
                SmartResult.Success(token)
            } else {
                SmartResult.Error(DataError.Network.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    /**
     * This function wraps network calls with the token management and safeCall logic.
     * It first tries with the current token, and if a 401 error occurs, it refreshes the token and retries once.
     * All calls are also wrapped in safeCall to handle exceptions and avoid crashes.
     */
    private suspend inline fun <reified T> performAuthRequest(
        request: (authHeader: String) -> Response<T>
    ): SmartResult<T, DataError.Network> {
        val token = getFirebaseToken() ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)

        return RetrofitClientExt.safeCall {
            val result = request("Bearer $token")
            Log.e(LOG_TAG, "Token: $token")

            if (result.code() == 401 || result.code() == 403) {
                // If the result is unauthorized or forbidden, attempt to refresh the token
                val refreshedToken = refreshFirebaseToken()
                if (refreshedToken is SmartResult.Success) {
                    Log.e(LOG_TAG, "Refreshed token: ${refreshedToken.data}")
                    val retryResult = request("Bearer ${refreshedToken.data}")

                    // Handle the result of the retry attempt with the refreshed token
                    return RetrofitClientExt.responseToSmartResult(retryResult)
                } else {
                    Log.e(LOG_TAG, "Token refresh failed")
                    // Return the original 401/403 error if token refresh failed
                    return RetrofitClientExt.responseToSmartResult(result)
                }
            } else {
                // If no 401/403 occurred, return the result as-is
                return RetrofitClientExt.responseToSmartResult(result)
            }
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
            val updateTokenResult = updatePushToken()
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
            val updateTokenResult = updatePushToken()
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