package com.studios1299.playwall.core.data

import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.domain.CoreRepository

class FirebaseCoreRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : CoreRepository {
    override suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun getUserProfile(): UserProfile {
        return UserProfile(
            name = "John Doe",
            email = "johndoe@example.com",
            avatarUrl = "https://media.licdn.com/dms/image/D4D03AQG510ilgQaD_g/profile-displayphoto-shrink_200_200/0/1709116748493?e=2147483647&v=beta&t=rfehlo_FlkkyBXfptFpsVWBUcNnQbID_dR0Ght21TTw"
        )
    }
}

data class UserProfile(
    val name: String,
    val email: String,
    val avatarUrl: String
)