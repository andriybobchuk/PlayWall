package com.studios1299.playwall.auth.data

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.messaging.FirebaseMessaging
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.domain.User
import com.studios1299.playwall.core.data.local.PreferencesDataSource
import com.studios1299.playwall.core.data.networking.CreateUserRequest
import com.studios1299.playwall.core.data.networking.RetrofitClientExt
import com.studios1299.playwall.core.data.networking.TokenManager
import com.studios1299.playwall.core.data.remote.RetrofitClient
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.asEmptyDataResult
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val preferencesDataSource: PreferencesDataSource
) : AuthRepository {
    override suspend fun login(email: String, password: String): SmartResult<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val idToken = firebaseUser.getIdToken(false).await().token
                preferencesDataSource.setAuthToken(idToken!!)
                SmartResult.Success(User(firebaseUser.uid, firebaseUser.email ?: ""))
            } else {
                SmartResult.Error(DataError.Network.UNAUTHORIZED)
            }
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

    /**
     * TODO("It may be possible that a user is authenticated with firebase but did not create a
     * profile on the backend due to unexpected server error, this is a threat.")
     */
    override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            preferencesDataSource.setAuthToken(firebaseIdToken)
            val firebaseFcmToken = firebaseMessaging.token.await() // For PUSH notifications

            val result = createUser(
                userId = user.uid,
                email = email,
                firebaseIdToken = firebaseIdToken,
                pushToken = firebaseFcmToken
            )
            if (result is SmartResult.Error) {
                firebaseAuth.signOut()
            }
            return result
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
        pushToken: String
    ): EmptyResult<DataError.Network> {
        return try {
            val authHeader = "Bearer $firebaseIdToken"
            Log.e("CreateUser", "Authorization header: $authHeader")

            val requestBody = CreateUserRequest(
                email = email,
                firebaseId = userId,
                pushToken = pushToken
            )

            return RetrofitClientExt.safeCall {
                RetrofitClient.userApi.createUser(authHeader, requestBody)
            }.also { result ->
                when (result) {
                    is SmartResult.Success -> {
                        Log.d("CreateUser", "User created successfully.")
                    }
                    is SmartResult.Error -> {
                        Log.e("CreateUser", "Failed to create user. Error: ${result.error}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CreateUser", "Exception occurred while creating user: ${e.message}", e)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun googleLogin(credential: AuthCredential): SmartResult<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            val firebaseIdToken = user.getIdToken(false).await().token ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
            preferencesDataSource.setAuthToken(firebaseIdToken)
            val firebaseFcmToken = firebaseMessaging.token.await() // For PUSH notifications

            val result = createUser(
                userId = user.uid,
                email = user.email ?: "",
                firebaseIdToken = firebaseIdToken,
                pushToken = firebaseFcmToken
            )
            if (result is SmartResult.Error) {
                return result
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

}