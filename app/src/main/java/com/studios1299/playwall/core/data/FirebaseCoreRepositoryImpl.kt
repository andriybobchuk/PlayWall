package com.studios1299.playwall.core.data

import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.domain.CoreRepository

class FirebaseCoreRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : CoreRepository {
    override suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}