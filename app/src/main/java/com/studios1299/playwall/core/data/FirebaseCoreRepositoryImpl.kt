package com.studios1299.playwall.core.data

import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.explore.presentation.Photo
import kotlinx.coroutines.delay
import java.util.UUID

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

    override suspend fun getExploreItems(): List<Photo> {
        delay(2000)
        return listOf(
            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/736x/68/8d/d3/688dd325dbbdc238f4b70caffe77a5af.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://www.androidauthority.com/wp-content/uploads/2024/02/Cool-wallpaper-1.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/236x/c8/00/45/c800451e3ef64f9bdf8a86a6f9c26e96.jpg", ""),

            Photo(UUID.randomUUID().toString(), "https://w0.peakpx.com/wallpaper/944/187/HD-wallpaper-ganesh-black-cool-thumbnail.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://images.rawpixel.com/image_800/cHJpdmF0ZS9sci9pbWFnZXMvd2Vic2l0ZS8yMDI0LTAyL2ZyZWVpbWFnZXNjb21wYW55X2FfcGhvdG9fb2ZfaGFuZ2luZ19nbG93aW5nX3JhbWFkYW5fY2VsZWJyYXRpb180YjQ4YWY1NC1jNzE5LTRlMjQtOGYwNy1jN2NjMTI1NWY5NjVfMS5qcGc.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/236x/72/be/42/72be42c1a0988932ea3cc969f4d6f4e7.jpg", ""),

            Photo(UUID.randomUUID().toString(), "https://wallpapershome.com/images/pages/ico_v/26344.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://img1.wallspic.com/previews/2/2/1/1/7/171122/171122-ios-water-purple-liquid-art-x750.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/736x/df/43/30/df43305962dfdb5e5433cb73e7be3dbe.jpg", ""),

            Photo(UUID.randomUUID().toString(), "https://r1.ilikewallpaper.net/iphone-wallpapers/download-151523/Deep-Purple-iPhone-14-Stock-Pro-Wallpaper_200.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/736x/38/e4/ff/38e4ff058759191aaf3f85558ae02292.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://e0.pxfuel.com/wallpapers/740/397/desktop-wallpaper-xiaomi-note-10.jpg", ""),

            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/236x/76/b5/e2/76b5e25475b35c48cc43d4ab1347f014.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/736x/68/8d/d3/688dd325dbbdc238f4b70caffe77a5af.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://www.androidauthority.com/wp-content/uploads/2024/02/Cool-wallpaper-1.jpg", ""),

            Photo(UUID.randomUUID().toString(), "https://i.pinimg.com/236x/c8/00/45/c800451e3ef64f9bdf8a86a6f9c26e96.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://w0.peakpx.com/wallpaper/944/187/HD-wallpaper-ganesh-black-cool-thumbnail.jpg", ""),
            Photo(UUID.randomUUID().toString(), "https://images.rawpixel.com/image_800/cHJpdmF0ZS9sci9pbWFnZXMvd2Vic2l0ZS8yMDI0LTAyL2ZyZWVpbWFnZXNjb21wYW55X2FfcGhvdG9fb2ZfaGFuZ2luZ19nbG93aW5nX3JhbWFkYW5fY2VsZWJyYXRpb180YjQ4YWY1NC1jNzE5LTRlMjQtOGYwNy1jN2NjMTI1NWY5NjVfMS5qcGc.jpg", ""),


            )
    }
}

data class UserProfile(
    val name: String,
    val email: String,
    val avatarUrl: String
)