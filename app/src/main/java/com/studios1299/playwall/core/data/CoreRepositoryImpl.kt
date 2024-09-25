package com.studios1299.playwall.core.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.data.local.PreferencesDataSource
import com.studios1299.playwall.core.data.networking.RetrofitClient
import com.studios1299.playwall.core.data.networking.RetrofitClientExt
import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.friendships.InviteRequest
import com.studios1299.playwall.core.data.networking.request.user.UpdateProfileRequest
import com.studios1299.playwall.core.data.networking.response.UserDataResponse
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.explore.presentation.explore.Photo
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.data.model.Reaction
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.play.Friend
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.io.File
//import software.amazon.awssdk.core.sync.RequestBody
//import software.amazon.awssdk.services.s3.model.GetObjectRequest
//import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

class FirebaseCoreRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val preferencesDataSource: PreferencesDataSource
) : CoreRepository {
    companion object {
        private const val LOG_TAG = "FirebaseCoreRepositoryImpl"
    }

    override suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private suspend fun getFirebaseToken(): String? {
        return preferencesDataSource.getAuthToken() ?: refreshFirebaseToken().let {
            if (it is SmartResult.Success) it.data else null
        }
    }

    private suspend fun refreshFirebaseToken(): SmartResult<String, DataError.Network> {
        return try {
            val user = firebaseAuth.currentUser ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)

            val token = user.getIdToken(true).await().token
            if (token != null) {
                preferencesDataSource.setAuthToken(token)
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
            if (result.code() == 401 || result.code() == 403) {
                val refreshedToken = refreshFirebaseToken()
                if (refreshedToken is SmartResult.Success) {
                    request("Bearer ${refreshedToken.data}")
                } else {
                    return@safeCall result // Return the original unauthorized result
                }
            } else {
                result
            }
        }
    }

    override suspend fun inviteFriend(email: String): SmartResult<Unit, DataError.Network> {
        return performAuthRequest { token ->
            val inviteRequest = InviteRequest(email = email)
            RetrofitClient.friendsApi.inviteFriend(token, inviteRequest)
        }
    }

    override suspend fun getFriends(): SmartResult<List<Friend>, DataError.Network> {
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.getFriends(token)
        }
    }

    override suspend fun getFriendRequests(): SmartResult<List<Friend>, DataError.Network> {
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.getFriendRequests(token)
        }
    }

    override suspend fun acceptFriendRequest(acceptRequest: AcceptRequest): SmartResult<Unit, DataError.Network> {
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.acceptFriendRequest(token, acceptRequest)
        }
    }

    override suspend fun declineFriendRequest(declineRequest: DeclineRequest): SmartResult<Unit, DataError.Network> {
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.declineFriendRequest(token, declineRequest)
        }
    }

    override suspend fun getUserData(): SmartResult<UserDataResponse, DataError.Network> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.userApi.getUserData(token)
            }

            if (result is SmartResult.Success) {
                val userData = result.data
                val avatarUrlResult = loadAvatar(userData.avatarId)
                val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                    avatarUrlResult.data
                } else {
                    ""
                }

                SmartResult.Success(
                    UserDataResponse(
                        name = userData.name,
                        email = userData.email,
                        avatarId = avatarUrl
                    )
                )
            } else {
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }


    override suspend fun updateProfile(avatarId: String?, nick: String?): SmartResult<Unit, DataError.Network> {
        return try {
            performAuthRequest { token ->
                val updateProfileRequest = UpdateProfileRequest(
                    avatarId = avatarId,
                    nick = nick
                )
                Log.e(LOG_TAG, "Updating profile.. nick = $nick, avatar = $avatarId")
                RetrofitClient.userApi.updateProfile(token, updateProfileRequest)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }


    // S3:
    override suspend fun uploadAvatar(file: File): SmartResult<String, DataError.Network> {
        return try {
            val avatarId = S3Handler.uploadToS3(file)
            if (avatarId != null) {
                SmartResult.Success(avatarId)
            } else {
                SmartResult.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun loadAvatar(avatarId: String): SmartResult<String, DataError.Network> {
        return try {
            val avatarUrl = S3Handler.loadFromS3(avatarId)
            if (avatarUrl != null) {
                SmartResult.Success(avatarUrl)
            } else {
                SmartResult.Error(DataError.Network.NOT_FOUND)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }



















    override suspend fun getExploreItems(): List<Photo> {
        delay(1000)
        return listOf(
            Photo("7922e7f6b33a1", "https://i.pinimg.com/736x/68/8d/d3/688dd325dbbdc238f4b70caffe77a5af.jpg", ""),
            Photo("7922e7f6b33a2", "https://www.androidauthority.com/wp-content/uploads/2024/02/Cool-wallpaper-1.jpg", ""),
            Photo("7922e7f6b33a3", "https://i.pinimg.com/236x/c8/00/45/c800451e3ef64f9bdf8a86a6f9c26e96.jpg", ""),

            Photo("7922e7f6b33a4", "https://w0.peakpx.com/wallpaper/944/187/HD-wallpaper-ganesh-black-cool-thumbnail.jpg", ""),
            Photo("7922e7f6b33a5", "https://images.rawpixel.com/image_800/cHJpdmF0ZS9sci9pbWFnZXMvd2Vic2l0ZS8yMDI0LTAyL2ZyZWVpbWFnZXNjb21wYW55X2FfcGhvdG9fb2ZfaGFuZ2luZ19nbG93aW5nX3JhbWFkYW5fY2VsZWJyYXRpb180YjQ4YWY1NC1jNzE5LTRlMjQtOGYwNy1jN2NjMTI1NWY5NjVfMS5qcGc.jpg", ""),
            Photo("7922e7f6b33a6", "https://i.pinimg.com/236x/72/be/42/72be42c1a0988932ea3cc969f4d6f4e7.jpg", ""),

            Photo("7922e7f6b33a7", "https://i.pinimg.com/736x/df/43/30/df43305962dfdb5e5433cb73e7be3dbe.jpg", ""),
            Photo("7922e7f6b33a8", "https://img1.wallspic.com/previews/2/2/1/1/7/171122/171122-ios-water-purple-liquid-art-x750.jpg", ""),
            Photo("7922e7f6b33a9", "https://i.pinimg.com/736x/df/43/30/df43305962dfdb5e5433cb73e7be3dbe.jpg", ""),

            Photo("7922e7f6b33a10", "https://r1.ilikewallpaper.net/iphone-wallpapers/download-151523/Deep-Purple-iPhone-14-Stock-Pro-Wallpaper_200.jpg", ""),
            Photo("7922e7f6b33a11", "https://i.pinimg.com/736x/38/e4/ff/38e4ff058759191aaf3f85558ae02292.jpg", ""),
            Photo("7922e7f6b33a12", "https://e0.pxfuel.com/wallpapers/740/397/desktop-wallpaper-xiaomi-note-10.jpg", ""),

            Photo("7922e7f6b33a13", "https://i.pinimg.com/236x/76/b5/e2/76b5e25475b35c48cc43d4ab1347f014.jpg", ""),
            Photo("7922e7f6b33a14", "https://i.pinimg.com/736x/68/8d/d3/688dd325dbbdc238f4b70caffe77a5af.jpg", ""),
            Photo("7922e7f6b33a15", "https://www.androidauthority.com/wp-content/uploads/2024/02/Cool-wallpaper-1.jpg", ""),

            Photo("7922e7f6b33a16", "https://i.pinimg.com/236x/c8/00/45/c800451e3ef64f9bdf8a86a6f9c26e96.jpg", ""),
            Photo("7922e7f6b33a17", "https://w0.peakpx.com/wallpaper/944/187/HD-wallpaper-ganesh-black-cool-thumbnail.jpg", ""),
            Photo("7922e7f6b33a18", "https://images.rawpixel.com/image_800/cHJpdmF0ZS9sci9pbWFnZXMvd2Vic2l0ZS8yMDI0LTAyL2ZyZWVpbWFnZXNjb21wYW55X2FfcGhvdG9fb2ZfaGFuZ2luZ19nbG93aW5nX3JhbWFkYW5fY2VsZWJyYXRpb180YjQ4YWY1NC1jNzE5LTRlMjQtOGYwNy1jN2NjMTI1NWY5NjVfMS5qcGc.jpg", ""),
            )
    }


private val _messages = mutableListOf<Message>()


private val _currentUser = User(
    id = "user1",
    name = "Andrii Bobchuk",
    profilePictureUrl = "https://media.licdn.com/dms/image/D4D03AQG510ilgQaD_g/profile-displayphoto-shrink_200_200/0/1709116748493?e=2147483647&v=beta&t=rfehlo_FlkkyBXfptFpsVWBUcNnQbID_dR0Ght21TTw",
    lastOnline = System.currentTimeMillis(),
    email = "andrii.bobchuk@gmail.com"
)
private val _recipient = User(
    id = "user2",
    name = "Tom Sawyer",
    profilePictureUrl = "https://lithelper.com/wp-content/uploads/2020/05/tom1.jpeg",
    lastOnline = System.currentTimeMillis(),
    email = "andrii.bobchuk@gmail.com"
)

init {
    _messages.addAll(
        listOf(
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.thesprucepets.com/thmb/OoMBiCxD3B02Jx-WO9dmY0DAaaI=/4000x0/filters:no_upscale():strip_icc()/cats-recirc3_2-1f5de201af94447a9063f83249260aff.jpg",
                caption = "Message #17",
                timestamp = System.currentTimeMillis() - 4500000,
                status = MessageStatus.DELIVERED,
                reactions = listOf(
                    Reaction("user2", "❤️")
                ),
                senderId = _recipient.id,
                recipientId = _currentUser.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.washingtonpost.com/resizer/7tLgbjOZeTsaTiPuxZ1DaxKbWOA=/arc-anglerfish-washpost-prod-washpost/public/FPGDGYJXM56KI5CTHHDX3DN2WQ.jpg",
                caption = "Message #16",
                timestamp = System.currentTimeMillis() - 5000000,
                status = MessageStatus.SENT,
                reactions = listOf(
                    Reaction("user1", "❤️"),
                    Reaction("user2", "👍")
                ),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://static.scientificamerican.com/sciam/cache/file/2AE14CDD-1265-470C-9B15F49024186C10_source.jpg?w=1200",
                caption = "Message #15",
                timestamp = System.currentTimeMillis() - 86400000 * 6,
                status = MessageStatus.SENT,
                reactions = listOf(
                    Reaction("user1", "😍"),
                    Reaction("user2", "❤️")
                ),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://i.pinimg.com/736x/87/4c/9a/874c9a34512567d6b370e66c74bb8b28.jpg",
                caption = "Message #14",
                timestamp = System.currentTimeMillis() - 86400000 * 7,
                status = MessageStatus.DELIVERED,
                reactions = listOf(
                    Reaction("user2", "❤️")
                ),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.petethevet.com/wp-content/uploads/2019/02/cat-1739091_1920.jpg",
                caption = "Message #13",
                timestamp = System.currentTimeMillis() - 86400000 * 8,
                status = MessageStatus.SENT,
                reactions = listOf(
                    Reaction("user2", "❤️")
                ),
                senderId = _recipient.id,
                recipientId = _currentUser.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://i.pinimg.com/736x/c3/f1/ab/c3f1ab06a04e326d71c8c8f835ab0f11.jpg",
                caption = "Message #12",
                timestamp = System.currentTimeMillis() - 86400000 * 9,
                status = MessageStatus.SENT,
                reactions = listOf(Reaction("user2", "❤️")),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.purina.co.nz/sites/default/files/2020-11/6-Small-Cat-BreedsHERO.jpg",
                caption = "Message #11",
                timestamp = System.currentTimeMillis() - 86400000 * 10,
                status = MessageStatus.SENT,
                reactions = listOf(Reaction("user2", "😍")),
                senderId = _recipient.id,
                recipientId = _currentUser.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://ichef.bbci.co.uk/news/976/cpsprodpb/114BC/production/_127844807_henry_daniella_hutchinson.jpg",
                caption = "Message #10",
                timestamp = System.currentTimeMillis() - 86400000 * 11,
                status = MessageStatus.SENT,
                reactions = listOf(
                    Reaction("user1", "😂"),
                    Reaction("user2", "❤️")
                ),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.pbs.org/wnet/nature/files/2014/09/ExtraordinaryCats-Main.jpg",
                caption = "Message #9",
                timestamp = System.currentTimeMillis() - 86400000 * 12,
                status = MessageStatus.SENT,
                reactions = listOf(
                    Reaction("user1", "👍"),
                    Reaction("user2", "❤️")
                ),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://st3.depositphotos.com/30152186/37144/i/450/depositphotos_371445966-stock-photo-funny-kitten-grass-summer.jpg",
                caption = "Message #8",
                timestamp = System.currentTimeMillis() - 86400000 * 13,
                status = MessageStatus.READ,
                reactions = listOf(Reaction("user1", "❤️")),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://static.scientificamerican.com/sciam/cache/file/32665E6F-8D90-4567-9769D59E11DB7F26_source.jpg?w=1200",
                caption = "Message #7",
                timestamp = System.currentTimeMillis() - 86400000 * 14,
                status = MessageStatus.DELIVERED,
                reactions = listOf(),
                senderId = _recipient.id,
                recipientId = _currentUser.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://cdn.britannica.com/34/235834-050-C5843610/two-different-breeds-of-cats-side-by-side-outdoors-in-the-garden.jpg",
                caption = "Message #6",
                timestamp = System.currentTimeMillis() - 86400000 * 15,
                status = MessageStatus.SENT,
                reactions = listOf(Reaction("user1", "👍")),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.purina.co.nz/sites/default/files/styles/ttt_image_510/public/2020-11/6-Small-Cat-Breeds1.jpg?itok=vRRyOFAB",
                caption = "Message #5",
                timestamp = System.currentTimeMillis() - 86400000 * 16,
                status = MessageStatus.DELIVERED,
                reactions = listOf(),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcJVO3i3NJtecXei14edIi7shKj2F2fpRcjg&s",
                caption = "Message #4",
                timestamp = System.currentTimeMillis() - 86400000 * 17,
                status = MessageStatus.DELIVERED,
                reactions = listOf(Reaction("user1", "😂")),
                senderId = _recipient.id,
                recipientId = _currentUser.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.joiipetcare.com/wp-content/uploads/2023/05/BOAS-cat-1-compressed.jpg",
                caption = "Message #3",
                timestamp = System.currentTimeMillis() - 86400000 * 18,
                status = MessageStatus.SENT,
                reactions = listOf(Reaction("user2", "❤️")),
                senderId = _currentUser.id,
                recipientId = _recipient.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.omlet.us/images/originals/Cat-Cat_Guide-A_Devon_Rex_cat_showing_off_its_wonderful_pointed_ear_tips.jpg",
                caption = "Message #2",
                timestamp = System.currentTimeMillis() - 86400000 * 19,
                status = MessageStatus.READ,
                reactions = listOf(Reaction("user2", "👍")),
                senderId = _recipient.id,
                recipientId = _currentUser.id
            ),
            Message(
                id = UUID.randomUUID().toString(),
                imageUrl = "https://www.catster.com/wp-content/uploads/2023/12/rsz_shutterstock_147812990-1.jpg",
                caption = "Message #1, oldest one",
                timestamp = System.currentTimeMillis() - 86400000 * 20,
                status = MessageStatus.DELIVERED,
                reactions = listOf(Reaction("user1", "👍")),
                senderId = _recipient.id,
                recipientId = _currentUser.id
            )
        )
    )

}

override suspend fun retrieveMessages(page: Int, pageSize: Int): Result<List<Message>> {
    Log.d(LOG_TAG, "retrieveMessages called with page: $page, pageSize: $pageSize")

    if (page != 0) {
        delay(2000L) // I dont need delay on the very first set of messages
    }
    val startingIndex = page * pageSize
    Log.d(LOG_TAG, "Calculated startingIndex: $startingIndex")

    val sortedMessages = _messages.sortedByDescending { it.timestamp }
    Log.d(LOG_TAG, "Total messages available: ${sortedMessages.size}")

    return if (startingIndex < sortedMessages.size) {
        val endIndex = minOf(startingIndex + pageSize, sortedMessages.size)
        Log.d(LOG_TAG, "Returning messages from index $startingIndex to $endIndex")

        val returnedMessages = sortedMessages.slice(startingIndex until endIndex)

        returnedMessages.forEachIndexed { index, message ->
            Log.d(LOG_TAG, "Message ${index + 1}: Caption - ${message.caption}")
        }
        Result.success(returnedMessages)
    } else {
        Log.e(LOG_TAG, "Starting index $startingIndex is out of bounds, returning empty list")
        Result.success(emptyList())
    }
}

override fun getLastMessageId(): Result<String> {
    return if (_messages.isNotEmpty()) {
        val lastMessage = _messages.maxByOrNull { it.timestamp }
        lastMessage?.let {
            Log.d(LOG_TAG, "Last message ID: ${it.id}, Caption: ${it.caption}")
            Result.success(it.id)
        } ?: run {
            Log.e(LOG_TAG, "Unable to find the last message")
            Result.failure(Exception("Unable to find the last message"))
        }
    } else {
        Log.e(LOG_TAG, "No messages available")
        Result.failure(Exception("No messages available"))
    }
}

override fun addMessage(message: Message) {
    _messages.add(message)
}

override fun deleteMessage(messageId: String) {
    _messages.removeAll { it.id == messageId }
}

override fun updateMessage(message: Message) {
    val index = _messages.indexOfFirst { it.id == message.id }
    if (index != -1) {
        _messages[index] = message
    }
}

override fun getCurrentUser(): User {
    return _currentUser
}

override fun getRecipient(): User {
    return _recipient
}

override fun getUserNameById(userId: String): String {
    if (userId == "user1") {
        return "Andrii Bobchuk"
    } else if (userId == "user2") {
        return "Tom Sawyer"
    }
    return "Unknown"
}


//override fun searchUsers(query: String): List<User> {
//    if (query.length < 3) {
//        return emptyList()
//    }
//    return _users.filter {
//        it.email.contains(query, ignoreCase = true) ||
//                it.name.contains(query, ignoreCase = true)
//    }
//}
    override fun getLikeCount(photoId: String): Int {
        return 11
    }

    // WALLPAPER MANAGEMENT
    override fun isLiked(wallpaperId: String): Boolean {
        return preferencesDataSource.isWallpaperLiked(wallpaperId)
    }

    override fun setLiked(wallpaperId: String, isLiked: Boolean) {
        preferencesDataSource.setWallpaperLiked(wallpaperId, isLiked)
    }

    override fun getWallpaperDestination(): WallpaperOption {
        return preferencesDataSource.getWallpaperDestination()
    }

    override fun setWallpaperDestination(option: WallpaperOption) {
        preferencesDataSource.setWallpaperDestination(option)
    }

    override fun getCurrentWallpaperId(): String? {
        return preferencesDataSource.getCurrentWallpaperId()
    }

    override fun setCurrentWallpaperId(id: String) {
        preferencesDataSource.setCurrentWallpaperId(id)
    }

    override fun getPreviousWallpaperId(): String? {
        return preferencesDataSource.getPreviousWallpaperId()
    }

    override fun setPreviousWallpaperId(id: String) {
        preferencesDataSource.setPreviousWallpaperId(id)
    }

    override fun shouldSaveIncomingWallpapers(): Boolean {
        return preferencesDataSource.isSavingIncomingWallpapersEnabled()
    }

    override fun setSaveIncomingWallpapers(shouldSave: Boolean) {
        preferencesDataSource.setSaveIncomingWallpapers(shouldSave)
    }
}




