package com.studios1299.playwall.auth.data

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.messaging.FirebaseMessaging
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.domain.User
import com.studios1299.playwall.core.data.local.PreferencesDataSource
import com.studios1299.playwall.core.data.networking.PushTokenRequest
import com.studios1299.playwall.core.data.networking.RetrofitClient.userService
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.EmptyResult
import com.studios1299.playwall.core.domain.error_handling.Result
import com.studios1299.playwall.core.domain.error_handling.asEmptyDataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val preferencesDataSource: PreferencesDataSource
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val idToken = firebaseUser.getIdToken(false).await().token
                preferencesDataSource.setAuthToken(idToken!!)
                Result.Success(User(firebaseUser.uid, firebaseUser.email ?: ""))
            } else {
                Result.Error(DataError.Network.UNAUTHORIZED)
            }
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> Result.Error(DataError.Network.UNAUTHORIZED)
                "ERROR_USER_NOT_FOUND" -> Result.Error(DataError.Network.UNAUTHORIZED)
                else -> Result.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun register(email: String, password: String): EmptyResult<DataError.Network> {
        return try {
            val createResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = createResult.user
            if (firebaseUser != null) {
                val pushToken = firebaseMessaging.token.await()
                val firebaseUid = firebaseUser.getIdToken(false).await().token
                preferencesDataSource.setAuthToken(firebaseUid!!)
                Log.e("TOKEN", pushToken)
                Log.e("UIDTOKEN", firebaseUid)

                withContext(Dispatchers.IO) {
                    createUser(firebaseUid, PushTokenRequest(pushToken))
                }
            }
            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> Result.Error(DataError.Network.CONFLICT)
                else -> Result.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }.asEmptyDataResult()
    }

    override suspend fun googleLogin(credential: AuthCredential): Result<User, DataError.Network> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val idToken = firebaseUser.getIdToken(false).await().token
                preferencesDataSource.setAuthToken(idToken!!)
                Result.Success(User(firebaseUser.uid, firebaseUser.email ?: ""))
            } else {
                Result.Error(DataError.Network.UNAUTHORIZED)
            }
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> Result.Error(DataError.Network.UNAUTHORIZED)
                "ERROR_USER_NOT_FOUND" -> Result.Error(DataError.Network.UNAUTHORIZED)
                else -> Result.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override fun logOut() {
        firebaseAuth.signOut()
    }

    override fun createUser(firebaseId: String, pushTokenRequest: PushTokenRequest): Result<Void?, DataError.Network> {
        val authHeader = "Bearer $firebaseId"

        return try {
            Log.e("CreateUser", "Sending request to create user with token: $authHeader and pushToken: ${pushTokenRequest.pushToken}")

            val response = userService.createUser(authHeader, pushTokenRequest).execute()

            if (response.isSuccessful) {
                Log.e("CreateUser", "Backend user creation successful")
                Result.Success(response.body())
            } else {
                Log.e("CreateUser", "Backend user creation failed with code: ${response.code()} and message: ${response.message()}")
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: Exception) {
            Log.e("CreateUser", "Exception occurred during backend user creation", e)
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}