package com.studios1299.playwall.core.data

import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.explore.presentation.Photo

class FakeCoreRepository : CoreRepository {
    override suspend fun getCurrentUserId(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun getUserProfile(): UserProfile {
        return UserProfile(
            name = "John Doe",
            email = "johndoe@example.com",
            avatarUrl = "https://media.licdn.com/dms/image/D4D03AQG510ilgQaD_g/profile-displayphoto-shrink_200_200/0/1709116748493?e=2147483647&v=beta&t=rfehlo_FlkkyBXfptFpsVWBUcNnQbID_dR0Ght21TTw"
        )
    }

    override suspend fun getExploreItems(): List<Photo> {
        return listOf()
    }
}

