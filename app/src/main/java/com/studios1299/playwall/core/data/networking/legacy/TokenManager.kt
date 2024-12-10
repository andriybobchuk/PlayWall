package com.studios1299.playwall.core.data.networking.legacy

import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import kotlinx.coroutines.tasks.await

object TokenManager {

    suspend fun getFirebaseToken(): String? {
        return Preferences.getAuthToken() ?: refreshFirebaseToken().let {
            if (it is SmartResult.Success) it.data else null
        }
    }

    suspend fun refreshFirebaseToken(): SmartResult<String> {
        return try {
            val user = MyApp.appModule.firebaseAuth.currentUser ?: return SmartResult.Error(401, null,"Not authorized")
            val token = user.getIdToken(true).await().token
            if (token != null) {
                Preferences.setAuthToken(token)
                SmartResult.Success(token)
            } else {
                SmartResult.Error(401, null,"Not authorized")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, null,e.message)
        }
    }
}
