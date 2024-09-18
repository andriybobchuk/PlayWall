package com.studios1299.playwall.core.data.networking

import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.local.PreferencesDataSource
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import kotlinx.coroutines.tasks.await

object TokenManager {

    suspend fun getFirebaseToken(): String? {
        return MyApp.appModule.preferencesDataSource.getAuthToken() ?: refreshFirebaseToken().let {
            if (it is SmartResult.Success) it.data else null
        }
    }

    suspend fun refreshFirebaseToken(): SmartResult<String, DataError.Network> {
        return try {
            val user = MyApp.appModule.firebaseAuth.currentUser ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)

            val token = user.getIdToken(true).await().token
            if (token != null) {
                MyApp.appModule.preferencesDataSource.setAuthToken(token)
                SmartResult.Success(token)
            } else {
                SmartResult.Error(DataError.Network.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }
}
