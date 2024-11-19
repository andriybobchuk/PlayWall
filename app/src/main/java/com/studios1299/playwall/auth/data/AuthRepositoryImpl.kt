package com.studios1299.playwall.auth.data

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.messaging.FirebaseMessaging
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.domain.User
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.request.user.CreateUserRequest
import com.studios1299.playwall.core.data.networking.RetrofitClientExt
import com.studios1299.playwall.core.data.networking.RetrofitClient
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.asEmptyDataResult
import kotlinx.coroutines.tasks.await
import retrofit2.Response

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
) : AuthRepository {

    companion object {
        private const val LOG_TAG = "FirebaseAuthRepositoryImpl"
    }

    override suspend fun login(email: String, password: String): SmartResult<User> {
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
                SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", "firebaseUser == null")
            }
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> SmartResult.Error(
                    600,
                    "Runtime exception in ${LOG_TAG}:",
                    "Invalid email or password!"
                )

                "ERROR_USER_NOT_FOUND" -> SmartResult.Error(
                    600,
                    "Runtime exception in ${LOG_TAG}:",
                    "User not found"
                )

                else -> SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        screenRatio: Float
    ): EmptyResult {
        return try {
            // Create user in Firebase
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return SmartResult.Error(
                600,
                "Runtime exception in ${LOG_TAG}:",
                "You are not authorized"
            )
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(
                600,
                "Runtime exception in ${LOG_TAG}:",
                "Not authorized"
            )
            Preferences.setAuthToken(firebaseIdToken)

            // Create user in DB
            val createUserResult = createUser(
                userId = user.uid,
                email = email,
                username = username,
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
                "ERROR_EMAIL_ALREADY_IN_USE" -> SmartResult.Error(
                    600,
                    "Runtime exception in ${LOG_TAG}:",
                    "Email already in use"
                )

                else -> SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }.asEmptyDataResult()
    }

    suspend fun createUser(
        userId: String,
        email: String,
        username: String,
        firebaseIdToken: String,
        screenRatio: Float
    ): EmptyResult {
        return try {
            val authHeader = "Bearer $firebaseIdToken"

            val requestBody = CreateUserRequest(
                email = email,
                nick = username,
                firebaseId = userId,
                screenRatio = screenRatio
            )

            return RetrofitClientExt.safeCall {
                RetrofitClient.userApi.createUser(authHeader, requestBody)
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    override suspend fun updatePushToken(): EmptyResult {
        return try {
            performAuthRequest { token ->
                Log.e("updatePushToken", "Start updating FCM token in the repo")
                val firebaseFcmToken = firebaseMessaging.token.await()
                Preferences.setFcmToken(firebaseFcmToken)
                val requestBody = mapOf("pushToken" to firebaseFcmToken)

                Log.e("updatePushToken", "Calling userApi.updatePushToken()...")
                RetrofitClient.userApi.updatePushToken(token, requestBody)
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    override suspend fun deletePushToken(): EmptyResult {
        return try {
            performAuthRequest { token ->
                Log.e("deletePushToken", "Start updating FCM token in the repo")
                val requestBody = mapOf("pushToken" to null as String?)
                Log.e("deletePushToken", "Calling userApi.updatePushToken()...")
                RetrofitClient.userApi.updatePushToken(token, requestBody)
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    private suspend fun getFirebaseToken(): String? {
        return Preferences.getAuthToken() ?: refreshFirebaseToken().let {
            if (it is SmartResult.Success) it.data else null
        }
    }

    private suspend fun refreshFirebaseToken(): SmartResult<String> {
        return try {
            val user = firebaseAuth.currentUser ?: return SmartResult.Error(
                600,
                "Runtime exception in ${LOG_TAG}:",
                "CurrentUser is null"
            )

            val token = user.getIdToken(true).await().token
            if (token != null) {
                Preferences.setAuthToken(token)
                SmartResult.Success(token)
            } else {
                SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", "Token == null")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    /**
     * This function wraps network calls with the token management and safeCall logic.
     * It first tries with the current token, and if a 401 error occurs, it refreshes the token and retries once.
     * All calls are also wrapped in safeCall to handle exceptions and avoid crashes.
     */
    private suspend inline fun <reified T> performAuthRequest(
        request: (authHeader: String) -> Response<T>
    ): SmartResult<T> {
        val token = getFirebaseToken() ?: return SmartResult.Error(
            600,
            "Runtime exception in ${LOG_TAG}:",
            "No token"
        )

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

    override suspend fun googleRegister(
        credential: AuthCredential,
        screenRatio: Float
    ): SmartResult<User> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return SmartResult.Error(
                600,
                "Runtime exception in ${LOG_TAG}:",
                "No user"
            )
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(
                600,
                "Runtime exception in ${LOG_TAG}:",
                "No ID token"
            )
            Preferences.setAuthToken(firebaseIdToken)
            //val firebaseFcmToken = firebaseMessaging.token.await() // For PUSH notifications

            val createUserResult = createUser(
                userId = user.uid,
                email = user.email ?: "",
                username = user.email ?: "",
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
                "ERROR_INVALID_CREDENTIAL" -> SmartResult.Error(
                    600,
                    "Runtime exception in ${LOG_TAG}:",
                    "Invalid credential"
                )

                "ERROR_USER_NOT_FOUND" -> SmartResult.Error(
                    600,
                    "Runtime exception in ${LOG_TAG}:",
                    "User not found"
                )

                else -> SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    override suspend fun googleLogin(credential: AuthCredential): SmartResult<User> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return SmartResult.Error(
                600,
                "Runtime exception in ${LOG_TAG}:",
                "not authorized"
            )
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(
                600,
                "Runtime exception in ${LOG_TAG}:",
                "No id token"
            )
            Preferences.setAuthToken(firebaseIdToken)

            // Update the push token after successfully login
            val updateTokenResult = updatePushToken()
            if (updateTokenResult is SmartResult.Error) {
                Log.e("Google login", updateTokenResult.errorBody.toString())
                //return updateTokenResult
            }

            return SmartResult.Success(User(user.uid, user.email ?: ""))
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> SmartResult.Error(
                    600,
                    "Runtime exception in ${LOG_TAG}:",
                    "invalid credential"
                )

                "ERROR_USER_NOT_FOUND" -> SmartResult.Error(
                    600,
                    "Runtime exception in ${LOG_TAG}:",
                    "User not found"
                )

                else -> SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): SmartResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            SmartResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): SmartResult<Unit> {
        val user = firebaseAuth.currentUser ?: run {
            Log.e("UpdatePassword", "No current user found, returning UNAUTHORIZED")
            return SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", "Not authorized")
        }
        // Log email being used for authentication
        Log.e("UpdatePassword", "User email: ${user.email}")

        // Get user's credential with current password
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        Log.e("UpdatePassword", "User credentials created for re-authentication")

        return try {
            // Re-authenticate the user
            Log.e("UpdatePassword", "Re-authenticating user")
            user.reauthenticate(credential).await()
            Log.e("UpdatePassword", "Re-authentication successful")

            // Update the password
            Log.e("UpdatePassword", "Updating password")
            user.updatePassword(newPassword).await()
            Log.e("UpdatePassword", "Password updated successfully")

            SmartResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Log.e("UpdatePassword", "FirebaseAuthException occurred: ${e.errorCode} - ${e.message}")
            return when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> {
                    Log.e("UpdatePassword", "Wrong password error")
                    SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", "Invalid credential")
                }

                else -> {
                    Log.e("UpdatePassword", "Unknown FirebaseAuth error")
                    SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
                }
            }
        } catch (e: Exception) {
            Log.e("UpdatePassword", "Exception occurred: ${e.message}")
            SmartResult.Error(600, "Runtime exception in ${LOG_TAG}:", e.message)
        }
    }

    override fun logOut() {
        firebaseAuth.signOut()
    }

}