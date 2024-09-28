package com.studios1299.playwall.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.networking.RetrofitClient
import com.studios1299.playwall.core.data.networking.RetrofitClientExt
import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.friendships.InviteRequest
import com.studios1299.playwall.core.data.networking.request.user.UpdateProfileRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.CommentRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ReactionRequest
import com.studios1299.playwall.core.data.networking.response.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.logSmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.explore.presentation.explore.Photo
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.play.Friend
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.io.File

class FirebaseCoreRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
) : CoreRepository {
    companion object {
        private const val LOG_TAG = "FirebaseCoreRepositoryImpl"
    }

    override suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    private suspend fun getFirebaseToken(): String? {
        return Preferences.getAuthToken() ?: refreshFirebaseToken().let {
            if (it is SmartResult.Success) it.data else null
        }
    }

    private suspend fun refreshFirebaseToken(): SmartResult<String, DataError.Network> {
        return try {
            val user = firebaseAuth.currentUser ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)

            val token = user.getIdToken(true).await().token
            if (token != null) {
                Preferences.setAuthToken(token)
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
//    private suspend inline fun <reified T> performAuthRequest(
//        request: (authHeader: String) -> Response<T>
//    ): SmartResult<T, DataError.Network> {
//        val token = getFirebaseToken() ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)
//
//        return RetrofitClientExt.safeCall {
//            val result = request("Bearer $token")
//            Log.e(LOG_TAG, "Token: ${token}")
//            if (result.code() == 401 || result.code() == 403) {
//                val refreshedToken = refreshFirebaseToken()
//                if (refreshedToken is SmartResult.Success) {
//                    Log.e(LOG_TAG, "Refreshed token: ${refreshedToken.data}")
//                    request("Bearer ${refreshedToken.data}")
//                } else {
//                    return@safeCall result // Return the original unauthorized result
//                }
//            } else {
//                result
//            }
//        }
//    }
    private suspend inline fun <reified T> performAuthRequest(
        request: (authHeader: String) -> Response<T>
    ): SmartResult<T, DataError.Network> {
        val token = getFirebaseToken() ?: return SmartResult.Error(DataError.Network.UNAUTHORIZED)

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

    override suspend fun inviteFriend(email: String): SmartResult<Unit, DataError.Network> {
        return performAuthRequest { token ->
            if (getCurrentUserEmail() == email) {
                return SmartResult.Error(DataError.Network.BAD_REQUEST)
            }
            val inviteRequest = InviteRequest(email = email)
            RetrofitClient.friendsApi.inviteFriend(token, inviteRequest)
        }
    }

    override suspend fun getFriends(): SmartResult<List<Friend>, DataError.Network> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.friendsApi.getFriends(token)
            }
            if (result is SmartResult.Success) {
                val friendsWithAvatars = result.data.map { friend ->
                    if (friend.avatarId == null) {
                        friend.copy(avatarId = "")
                    } else {
                        val avatarUrlResult = loadAvatar(friend.avatarId)
                        val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data
                        } else {
                            ""
                        }
                        friend.copy(avatarId = avatarUrl)
                    }
                }

                SmartResult.Success(friendsWithAvatars)
            } else {
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun getFriendRequests(): SmartResult<List<Friend>, DataError.Network> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.friendsApi.getFriendRequests(token)
            }

            if (result is SmartResult.Success) {
                val friendsWithAvatars = result.data.map { friend ->

                    if (friend.avatarId == null) {
                        friend.copy(avatarId = "")
                    } else {
                        val avatarUrlResult = loadAvatar(friend.avatarId)
                        val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data
                        } else {
                            ""
                        }
                        friend.copy(avatarId = avatarUrl)
                    }
                }
                SmartResult.Success(friendsWithAvatars)
            } else {
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception: " + e.message)
            SmartResult.Error(DataError.Network.UNKNOWN)
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
                        id = userData.id,
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
    override suspend fun uploadFile(file: File, folder: S3Handler.Folder): SmartResult<String, DataError.Network> {
        return try {
            val avatarId = S3Handler.uploadToS3(file, folder)
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










    // Wallpapers
    override suspend fun changeWallpaper(request: ChangeWallpaperRequest): SmartResult<Unit, DataError.Network> {
        return try {
            Log.e("DEBUGG", "changewallpaper start")
            val response = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.changeWallpaper(token, request)
            }
            if (response is SmartResult.Success) {
                Log.e("DEBUGG", "resposnse" + response.data)
                SmartResult.Success(Unit)
            } else {
                Log.e("changeWallpaper", "Error in response: ${logSmartResult(response)}")
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }
//    suspend fun sendWallpaper(filename: String, recipientId: String, comment: String?, reaction: String?): SmartResult<Unit, CustomError> {
//
//        // Convert the avatar URI to a File
//        val avatarFile = uriToFile(context, avatarUri)
//        if (avatarFile == null || !avatarFile.exists()) {
//            return SmartResult.Error(CustomError("Failed to convert Uri to File or file does not exist."))
//        }
//
//        // Upload the wallpaper to S3 and get the filename
//        val fileNameResult = uploadFile(avatarFile, S3Handler.Folder.WALLPAPERS) // Implement this method to upload to S3
//        if (fileNameResult is SmartResult.Error) {
//            return fileNameResult // Return the error if upload fails
//        }
//
//        val fileName = (fileNameResult as SmartResult.Success).data
//
//        // Send the wallpaper using the API
//        val changeWallpaperRequest = ChangeWallpaperRequest(fileName, recipientId, comment, reaction)
//        val response = wallpaperApi.changeWallpaper("Bearer YOUR_AUTH_TOKEN", changeWallpaperRequest)
//
//        return if (response.isSuccessful) {
//            SmartResult.Success(Unit) // Success
//        } else {
//            SmartResult.Error(CustomError("Failed to send wallpaper."))
//        }
//    }


    override suspend fun getUserDataById(recipientId: String): SmartResult<UserDataResponse, DataError.Network> {
        return try {
            Log.e(LOG_TAG, "Friend ID: " + recipientId)
            val result = performAuthRequest { token ->
                Log.e(LOG_TAG, "Friend ID: " + recipientId.toInt())
                RetrofitClient.wallpaperApi.getRecipientData(token, recipientId.toInt())
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
                        id = userData.id,
                        name = userData.name,
                        email = userData.email,
                        avatarId = avatarUrl
                    )
                )
            } else {
                Log.e(LOG_TAG, "Error in getRecipientData(): " + result)
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception in getRecipientData(): " + e.message)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun getWallpaperHistory(userId: String, page: Int, pageSize: Int): SmartResult<List<WallpaperHistoryResponse>, DataError.Network> {
        return try {

            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.getWallpaperHistory(token, userId.toInt(), page, pageSize)
            }
            Log.e(LOG_TAG, "getWallpaperHistory: "+result)

            if (result is SmartResult.Success) {
                SmartResult.Success(result.data.map { wallpaper ->

                    if (wallpaper.fileName == null) {
                        wallpaper.copy(fileName = "")
                    } else {
                        val wallpaperUrlResult = loadAvatar(wallpaper.fileName)
                        val wallpaperUrl = if (wallpaperUrlResult is SmartResult.Success) {
                            wallpaperUrlResult.data
                        } else {
                            ""
                        }
                        wallpaper.copy(fileName = wallpaperUrl)
                    }
                })
            } else {
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception in getWallpaperHistory(): " + e.message)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

//    override suspend fun getWallpaperHistory(userId: String, page: Int, pageSize: Int): SmartResult<List<WallpaperHistoryResponse>, DataError.Network> {
//        return try {
//            val result = performAuthRequest { token ->
//                RetrofitClient.wallpaperApi.getWallpaperHistory(token, userId.toInt(), page, pageSize)
//            }
//
//            if (result is SmartResult.Success) {
//                SmartResult.Success(result.data.map { wallpaper ->
//                    val wallpaperUrl = wallpaper.fileName?.let { loadAvatar(it).data } ?: ""
//                    wallpaper.copy(fileName = wallpaperUrl)
//                })
//            } else {
//                SmartResult.Error(DataError.Network.UNKNOWN)
//            }
//        } catch (e: Exception) {
//            Log.e(LOG_TAG, "Exception in getWallpaperHistory(): ${e.message}")
//            SmartResult.Error(DataError.Network.UNKNOWN)
//        }
//    }

//    override suspend fun retrieveMessages(page: Int, pageSize: Int): Result<List<Message>> {
//
//        val startingIndex = page * pageSize
//
//        val sortedMessages = _messages.sortedByDescending { it.timestamp }
//
//        return if (startingIndex < sortedMessages.size) {
//            val endIndex = minOf(startingIndex + pageSize, sortedMessages.size)
//            val returnedMessages = sortedMessages.slice(startingIndex until endIndex)
//
//            Result.success(returnedMessages)
//        } else {
//            Result.success(emptyList())
//        }
//    }

    override suspend fun react(wallpaperId: Int, reaction: String?): SmartResult<Unit, DataError.Network> {
        return try {
            performAuthRequest { token ->
                if (reaction.isNullOrEmpty()) {
                    RetrofitClient.wallpaperApi.removeReaction(token, ReactionRequest(wallpaperId, null))
                } else {
                    RetrofitClient.wallpaperApi.addReaction(token, ReactionRequest(wallpaperId, reaction))
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception in react(): " + e.message)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun comment(wallpaperId: Int, comment: String?): SmartResult<Unit, DataError.Network> {
        return try {
            performAuthRequest { token ->
                if (comment.isNullOrEmpty()) {
                    RetrofitClient.wallpaperApi.addComment(token, CommentRequest(wallpaperId, null))
                } else {
                    RetrofitClient.wallpaperApi.addComment(token, CommentRequest(wallpaperId, comment))
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception in comment(): " + e.message)
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



private val _recipient = User(
    id = -1,
    name = "Tom Sawyer",
    profilePictureUrl = "https://lithelper.com/wp-content/uploads/2020/05/tom1.jpeg",
    lastOnline = System.currentTimeMillis(),
    email = "andrii.bobchuk@gmail.com"
)


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

//override fun getLastMessageId(): Result<String> {
//    return if (_messages.isNotEmpty()) {
//        val lastMessage = _messages.maxByOrNull { it.timestamp }
//        lastMessage?.let {
//            Log.d(LOG_TAG, "Last message ID: ${it.id}, Caption: ${it.caption}")
//            Result.success(it.id)
//        } ?: run {
//            Log.e(LOG_TAG, "Unable to find the last message")
//            Result.failure(Exception("Unable to find the last message"))
//        }
//    } else {
//        Log.e(LOG_TAG, "No messages available")
//        Result.failure(Exception("No messages available"))
//    }
//}

override fun addMessage(message: Message) {
    _messages.add(message)
}

//override fun deleteMessage(messageId: String) {
//    _messages.removeAll { it.id == messageId }
//}

override fun updateMessage(message: Message) {
    val index = _messages.indexOfFirst { it.id == message.id }
    if (index != -1) {
        _messages[index] = message
    }
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
        return Preferences.isWallpaperLiked(wallpaperId)
    }

    override fun setLiked(wallpaperId: String, isLiked: Boolean) {
        Preferences.setWallpaperLiked(wallpaperId, isLiked)
    }

    override fun getWallpaperDestination(): WallpaperOption {
        return Preferences.getWallpaperDestination()
    }

    override fun setWallpaperDestination(option: WallpaperOption) {
        Preferences.setWallpaperDestination(option)
    }

    override fun getCurrentWallpaperId(): String? {
        return Preferences.getCurrentWallpaperId()
    }

    override fun setCurrentWallpaperId(id: String) {
        Preferences.setCurrentWallpaperId(id)
    }

    override fun getPreviousWallpaperId(): String? {
        return Preferences.getPreviousWallpaperId()
    }

    override fun setPreviousWallpaperId(id: String) {
        Preferences.setPreviousWallpaperId(id)
    }

    override fun shouldSaveIncomingWallpapers(): Boolean {
        return Preferences.isSavingIncomingWallpapersEnabled()
    }

    override fun setSaveIncomingWallpapers(shouldSave: Boolean) {
        Preferences.setSaveIncomingWallpapers(shouldSave)
    }
}




