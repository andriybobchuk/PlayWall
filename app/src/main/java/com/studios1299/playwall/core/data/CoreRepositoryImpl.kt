package com.studios1299.playwall.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.local.dao.ExploreWallpaperDao
import com.studios1299.playwall.core.data.local.dao.FriendDao
import com.studios1299.playwall.core.data.local.entity.ExploreWallpaperEntity
import com.studios1299.playwall.core.data.local.toDomain
import com.studios1299.playwall.core.data.local.toEntity
import com.studios1299.playwall.core.data.networking.RetrofitClient
import com.studios1299.playwall.core.data.networking.RetrofitClientExt
import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.BlockRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.friendships.InviteRequest
import com.studios1299.playwall.core.data.networking.request.friendships.RemoveFriendRequest
import com.studios1299.playwall.core.data.networking.request.friendships.UnblockRequest
import com.studios1299.playwall.core.data.networking.request.user.UpdateProfileRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.CommentRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.MarkMessagesAsReadRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ReactionRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.RemoveSavedWallpaperRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ReportRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.SaveWallpaperRequest
import com.studios1299.playwall.core.data.networking.response.wallpapers.ExploreWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.user.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.ChangeWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.error_handling.logSmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.feature.play.presentation.play.Friend
import com.studios1299.playwall.feature.play.presentation.play.FriendshipStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File

class FirebaseCoreRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val exploreDao: ExploreWallpaperDao,
    private val friendsDao: FriendDao,
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

    override suspend fun getFriends(forceUpdate: Boolean): SmartResult<List<Friend>, DataError.Network> {
        try {
            // Check cache first if not forced to update
            if (!forceUpdate) {
                val cachedFriends = friendsDao.getAllFriends() // Load friends with status = accepted
                if (cachedFriends.isNotEmpty()) {
                    val result: List<Friend> = cachedFriends.map { it.toDomain() }
                    return SmartResult.Success(result) // Convert entities to domain
                }
            }

            // Fetch from remote if forced or no cache
            val result = performAuthRequest { token ->
                RetrofitClient.friendsApi.getFriends(token)
            }

            if (result is SmartResult.Success) {
                val friendsWithAvatars = result.data.map { friend ->
                    if (friend.avatarId == null) {
                        friend.copy(avatarId = "")
                    } else {
                        val avatarUrlResult = pathToLink(friend.avatarId)
                        if (avatarUrlResult is SmartResult.Success) {
                            friend.copy(avatarId = avatarUrlResult.data)
                        } else {
                            friend.copy(avatarId = "")
                        }
                    }
                }

                // Cache the result
                friendsDao.deleteAllFriends() // Clear old cache
                friendsDao.insertFriends(friendsWithAvatars.map { it.toEntity() }) // Insert new cache

                return SmartResult.Success(friendsWithAvatars.filter { it.status == FriendshipStatus.accepted })
            } else {
                return result
            }
        } catch (e: Exception) {
            return SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun getFriendRequests(forceUpdate: Boolean): SmartResult<List<Friend>, DataError.Network> {
        try {
            // Check cache first if not forced to update
            if (!forceUpdate) {
                val cachedFriendRequests = friendsDao.getAllFriendRequests() // Load requests with status = pending
                if (cachedFriendRequests.isNotEmpty()) {
                    return SmartResult.Success(cachedFriendRequests.map { it.toDomain() })
                }
            }

            // Fetch from remote if forced or no cache
            val result = performAuthRequest { token ->
                RetrofitClient.friendsApi.getFriendRequests(token)
            }

            if (result is SmartResult.Success) {
                val friendsWithAvatars = result.data.map { friend ->
                    if (friend.avatarId == null) {
                        friend.copy(avatarId = "")
                    } else {
                        val avatarUrlResult = pathToLink(friend.avatarId)
                        val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data
                        } else {
                            ""
                        }
                        friend.copy(avatarId = avatarUrl)
                    }
                }

                // Cache the result
                friendsDao.deleteAllFriends() // Clear old cache
                friendsDao.insertFriends(friendsWithAvatars.map { it.toEntity() })

                return SmartResult.Success(friendsWithAvatars.filter { it.status == FriendshipStatus.pending })
            } else {
                return SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            return SmartResult.Error(DataError.Network.UNKNOWN)
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

    override suspend fun removeUser(friendshipId: Int): SmartResult<Unit, DataError.Network> {
        return try {
            Log.d("removeUser", "Removing friend with ID: $friendshipId")
            performAuthRequest { token ->
                Log.d("removeUser", "Using token: $token")
                RetrofitClient.friendsApi.removeFriend(token, RemoveFriendRequest(friendshipId))
            }
        } catch (e: Exception) {
            Log.e("removeUser", "Exception in removeUser(): " + e.message)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun blockUser(friendshipId: Int, userId: Int): SmartResult<Unit, DataError.Network> {
        return try {
            Log.e("repo", "Blocking user with friendship ID: $friendshipId by user $userId")
            performAuthRequest { token ->
                Log.d("blockUser", "Using token: $token")
                RetrofitClient.friendsApi.blockUser(token, BlockRequest(friendshipId, userId))
            }
        } catch (e: Exception) {
            Log.e("blockUser", "Exception in blockUser(): " + e.message)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun unblockUser(friendshipId: Int, userId: Int): SmartResult<Unit, DataError.Network> {
        return try {
            Log.d("unblockUser", "Unblocking user with friendship ID: $friendshipId by user $userId")
            performAuthRequest { token ->
                Log.d("unblockUser", "Using token: $token")
                RetrofitClient.friendsApi.unblockUser(token, UnblockRequest(friendshipId, userId))
            }
        } catch (e: Exception) {
            Log.e("unblockUser", "Exception in unblockUser(): " + e.message)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }



    override suspend fun getUserData(): SmartResult<UserDataResponse, DataError.Network> {
        return try {
            Log.e("getUserData", "Starting getUserData request")

            val result = performAuthRequest { token ->
                Log.e("getUserData", "Token retrieved: $token")
                RetrofitClient.userApi.getUserData(token)
            }

            if (result is SmartResult.Success) {
                val userData = result.data
                Log.e("getUserData", "UserData retrieved successfully: $userData")

                val avatarUrlResult = pathToLink(userData.avatarId)
                Log.e("getUserData", "Avatar ID: ${userData.avatarId}, Avatar URL result: $avatarUrlResult")

                val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                    avatarUrlResult.data
                } else {
                    Log.e("getUserData", "Failed to retrieve avatar URL, using empty string.")
                    ""
                }

                val userDataResponse = UserDataResponse(
                    id = userData.id,
                    name = userData.name,
                    email = userData.email,
                    avatarId = avatarUrl,
                    since = userData.since,
                    status = userData.status,
                    requesterId = userData.requesterId,
                    friendshipId = userData.friendshipId,
                    screenRatio = userData.screenRatio
                )

                Log.e("getUserData", "Returning UserDataResponse: $userDataResponse")
                SmartResult.Success(userDataResponse)
            } else {
                Log.e("getUserData", "Failed to retrieve user data: $result")
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Log.e("getUserData", "Exception in getUserData: ${e.message}", e)
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

    override suspend fun pathToLink(path: String): SmartResult<String, DataError.Network> {
        return try {
            val avatarUrl = S3Handler.pathToDownloadableLink(path)
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
    override suspend fun changeWallpaper(request: ChangeWallpaperRequest): SmartResult<ChangeWallpaperResponse?, DataError.Network> {
        return try {
            Log.e("changeWallpaper", "changewallpaper start with $request")
            val response = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.changeWallpaper(token, request)
            }
            if (response is SmartResult.Success) {
                Log.e("changeWallpaper", "resposnse" + response.data.data)
                SmartResult.Success(response.data.data)
            } else {
                Log.e("changeWallpaper", "Error in response: ${logSmartResult(response)}")
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun getUserDataById(recipientId: String): SmartResult<UserDataResponse, DataError.Network> {
        return try {
            Log.e(LOG_TAG, "Friend ID: " + recipientId)
            val result = performAuthRequest { token ->
                Log.e(LOG_TAG, "Friend ID: " + recipientId.toInt())
                RetrofitClient.wallpaperApi.getRecipientData(token, recipientId.toInt())
            }
            Log.e(LOG_TAG, "ImagePicker: raw ${result}")
            if (result is SmartResult.Success) {
                val userData = result.data
                val avatarUrlResult = pathToLink(userData.avatarId)
                val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                    avatarUrlResult.data
                } else {
                    ""
                }
                Log.e(LOG_TAG, "ImagePicker: rat ${userData.screenRatio}")
                SmartResult.Success(
                    UserDataResponse(
                        id = userData.id,
                        name = userData.name,
                        email = userData.email,
                        avatarId = avatarUrl,
                        since = userData.since,
                        status = userData.status,
                        requesterId = userData.requesterId,
                        friendshipId = userData.friendshipId,
                        screenRatio = userData.screenRatio
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

    override suspend fun getWallpaperHistory(
        userId: String,
        page: Int,
        pageSize: Int
    ): SmartResult<List<WallpaperHistoryResponse>, DataError.Network> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.getWallpaperHistory(token, userId.toInt(), page, pageSize)
            }

            Log.e(LOG_TAG, "getWallpaperHistory: $result")

            if (result is SmartResult.Success) {
                SmartResult.Success(result.data.data.map { wallpaper ->

                    if (wallpaper.fileName == null) {
                        wallpaper.copy(fileName = "")
                    } else {
                        val wallpaperUrlResult = pathToLink(wallpaper.fileName)
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
            Log.e(LOG_TAG, "Exception in getWallpaperHistory(): ${e.message}")
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun react(wallpaperId: Int, reaction: String?): SmartResult<Unit, DataError.Network> {
        return try {
            Log.e("react", "Reacting...")
            val result = performAuthRequest { token ->
                if (reaction.isNullOrEmpty()) {
                    RetrofitClient.wallpaperApi.removeReaction(token, ReactionRequest(wallpaperId, null))
                } else {
                    RetrofitClient.wallpaperApi.addReaction(token, ReactionRequest(wallpaperId, reaction))
                }
            }
            if ( result is SmartResult.Success) {
                Log.e("react", "success")
                result
            } else {
                Log.e("react", "fucked...")
                result
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

    override suspend fun markMessagesAsRead(friendshipId: Int, lastMessageId: Int): SmartResult<Unit, DataError.Network> {
        Log.e("ChatRepository", "Attempting to mark messages as read for friendshipId: $friendshipId, lastMessageId: $lastMessageId")
        return try {
            val result = performAuthRequest { token ->
                Log.e("ChatRepository", "Got auth token: $token")
                RetrofitClient.wallpaperApi.markMessagesAsRead(
                    token,
                    MarkMessagesAsReadRequest(friendshipId, lastMessageId)
                )
            }

            if (result is SmartResult.Success) {
                Log.e("ChatRepository", "Successfully marked messages as read for friendshipId: $friendshipId")
            } else if (result is SmartResult.Error) {
                Log.e("ChatRepository", "Failed to mark messages as read. Error: ${result.error}")
            } else {
                Log.e("ChatRepository", "Unexpected result: $result")
            }

            result
        } catch (e: Exception) {
            Log.e("ChatRepository", "Exception while marking messages as read: ${e.localizedMessage}")
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun loadExploreWallpapers(
        page: Int,
        pageSize: Int,
        forceRefresh: Boolean
    ): SmartResult<List<ExploreWallpaperResponse>, DataError.Network> = withContext(Dispatchers.IO) {
        Log.e("Repository", "Starting loadExploreWallpapers with page: $page, pageSize: $pageSize")

        // Try to load from Room first
        if (!forceRefresh) {
            val cachedWallpapers = exploreDao.getAllWallpapersSortedByOrder() // Fetch in correct order
            Log.e("Repository", "Cached wallpapers count: ${cachedWallpapers.size}")

            if (cachedWallpapers.isNotEmpty()) {
                // Return modified cached data
                Log.e("Repository", "Returning cached data")
                val modifiedCachedWallpapers = cachedWallpapers.map {
                    ExploreWallpaperResponse(
                        id = it.id,
                        fileName = it.fileName,
                        type = it.type,
                        sentCount = it.sentCount,
                        savedCount = it.savedCount,
                        dateUpdated = it.dateCreated,
                        dateCreated = it.dateCreated
                    )
                }
                return@withContext SmartResult.Success(modifiedCachedWallpapers) // Return the modified list
            } else {
                Log.e("Repository", "No cached data found, fetching from API")
            }
        }

        // If no cached data, fetch from the API
        return@withContext try {
            val result = performAuthRequest { token ->
                Log.e("Repository", "Making API request with token: $token")
                RetrofitClient.wallpaperApi.loadExploreWallpapers(token, page, pageSize)
            }

            if (result is SmartResult.Success) {
                Log.e("Repository", "Successfully fetched wallpapers from API: ${result.data.size}")

                // Cache the fetched data, preserving the order by assigning a sequential "order" field
                val entitiesToInsert = result.data.mapIndexed { index, wallpaper ->
                    val avatarUrlResult = pathToLink(wallpaper.fileName)
                    ExploreWallpaperEntity(
                        id = wallpaper.id,
                        fileName = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data
                        } else {
                            ""
                        },
                        type = wallpaper.type,
                        sentCount = wallpaper.sentCount,
                        savedCount = wallpaper.savedCount,
                        isLiked = false, // Adjust as necessary
                        dateCreated = wallpaper.dateCreated,
                        order = index // Store the order in which the wallpaper was fetched
                    )
                }

                // Log the data being inserted
                Log.e("Repository", "Inserting ${entitiesToInsert.size} wallpapers into Room")
                exploreDao.insertWallpapers(entitiesToInsert)

                // Also, return the modified list from the API result in the original order
                val modifiedApiWallpapers = result.data.map { wallpaper ->
                    val avatarUrlResult = pathToLink(wallpaper.fileName)
                    ExploreWallpaperResponse(
                        id = wallpaper.id,
                        fileName = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data
                        } else {
                            ""
                        },
                        type = wallpaper.type,
                        sentCount = wallpaper.sentCount,
                        savedCount = wallpaper.savedCount,
                        dateUpdated = wallpaper.dateCreated,
                        dateCreated = wallpaper.dateCreated
                    )
                }

                return@withContext SmartResult.Success(modifiedApiWallpapers) // Return modified API data
            } else {
                Log.e("Repository", "API request failed: ${result}")
            }

            result // Return the result (Success/Error)
        } catch (e: Exception) {
            Log.e("Repository", "Exception occurred: ${e.message}", e)
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }


    override suspend fun saveWallpaper(wallpaperId: Int): SmartResult<Unit, DataError.Network> {
        return try {
            performAuthRequest { token ->
                RetrofitClient.wallpaperApi.saveWallpaper(token, SaveWallpaperRequest(wallpaperId))
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun removeSavedWallpaper(wallpaperId: Int): SmartResult<Unit, DataError.Network> {
        return try {
            performAuthRequest { token ->
                RetrofitClient.wallpaperApi.removeSavedWallpaper(token, RemoveSavedWallpaperRequest(wallpaperId))
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun loadSavedWallpapers(page: Int, pageSize: Int): SmartResult<List<ExploreWallpaperResponse>, DataError.Network> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.loadSavedWallpapers(token, page, pageSize)
            }
            Log.e("loadSavedWallpapers in repo", "result: $result")
            if (result is SmartResult.Success) {
                val withLoadedImages = result.data.map {
                    if (it.fileName == null) {
                        it.copy(fileName = "")
                    } else {
                        val avatarUrlResult = pathToLink(it.fileName)
                        val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data
                        } else {
                            ""
                        }
                        it.copy(fileName = avatarUrl)
                    }
                }
                SmartResult.Success(withLoadedImages)
            } else {
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }



    override suspend fun reportWallpaper(wallpaperId: Int): SmartResult<Unit, DataError.Network> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.reportWallpaper(token, ReportRequest(wallpaperId))
            }
            if (result is SmartResult.Success) {
                SmartResult.Success(Unit)
            } else {
                SmartResult.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun getFriendScreenRatio(friendId: Int): SmartResult<Float, DataError.Network> {
        return try {
            val result =  performAuthRequest { token ->
                RetrofitClient.userApi.getFriendScreenRatio(token, friendId)
            }
            if (result is SmartResult.Success) {
                val screenRatio = result.data.screenRatio
                SmartResult.Success(screenRatio)
            } else {
                SmartResult.Error(DataError.Network.NOT_FOUND)
            }
        } catch (e: Exception) {
            SmartResult.Error(DataError.Network.UNKNOWN)
        }
    }





    // WALLPAPER MANAGEMENT
//    override fun isLiked(wallpaperId: String): Boolean {
//        return Preferences.isWallpaperLiked(wallpaperId)
//    }
//
//    override fun setLiked(wallpaperId: String, isLiked: Boolean) {
//        Preferences.setWallpaperLiked(wallpaperId, isLiked)
//    }

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




