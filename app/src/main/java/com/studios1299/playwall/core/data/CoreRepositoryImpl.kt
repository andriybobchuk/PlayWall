package com.studios1299.playwall.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.core.data.local.dao.ChatDao
import com.studios1299.playwall.core.data.local.dao.ExploreWallpaperDao
import com.studios1299.playwall.core.data.local.dao.FriendDao
import com.studios1299.playwall.core.data.local.dao.UserDao
import com.studios1299.playwall.core.data.local.entity.ExploreWallpaperEntity
import com.studios1299.playwall.core.data.local.toDomain
import com.studios1299.playwall.core.data.local.toEntity
import com.studios1299.playwall.core.data.local.toMessageEntity
import com.studios1299.playwall.core.data.local.toUserDataResponse
import com.studios1299.playwall.core.data.local.toUserEntity
import com.studios1299.playwall.core.data.networking.RetrofitClient
import com.studios1299.playwall.core.data.networking.RetrofitClientExt
import com.studios1299.playwall.core.data.networking.api.UpdateAppDataRequest
import com.studios1299.playwall.core.data.networking.request.friendships.AcceptRequest
import com.studios1299.playwall.core.data.networking.request.friendships.BlockRequest
import com.studios1299.playwall.core.data.networking.request.friendships.DeclineRequest
import com.studios1299.playwall.core.data.networking.request.friendships.InviteRequest
import com.studios1299.playwall.core.data.networking.request.friendships.LinkFriendshipRequest
import com.studios1299.playwall.core.data.networking.request.friendships.RemoveFriendRequest
import com.studios1299.playwall.core.data.networking.request.friendships.SendCodeRequest
import com.studios1299.playwall.core.data.networking.request.friendships.UnblockRequest
import com.studios1299.playwall.core.data.networking.request.user.UpdateProfileRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ChangeWallpaperRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.CommentRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.MarkMessagesAsReadRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ReactionRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.RemoveSavedWallpaperRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.ReportRequest
import com.studios1299.playwall.core.data.networking.request.wallpapers.SaveWallpaperRequest
import com.studios1299.playwall.core.data.networking.response.friendships.LinkRequestData
import com.studios1299.playwall.core.data.networking.response.wallpapers.ExploreWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.user.UserDataResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.ChangeWallpaperResponse
import com.studios1299.playwall.core.data.networking.response.wallpapers.WallpaperHistoryResponse
import com.studios1299.playwall.core.data.s3.S3Handler
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.play.data.model.Message
import com.studios1299.playwall.play.presentation.play.Friend
import com.studios1299.playwall.play.presentation.play.FriendshipStatus
import com.studios1299.playwall.monetization.presentation.AppState
import com.studios1299.playwall.monetization.presentation.screens.EVIL_EMOJI
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseCoreRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val exploreDao: ExploreWallpaperDao,
    private val friendsDao: FriendDao,
    private val chatDao: ChatDao,
    private val userDao: UserDao,
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

    private suspend fun refreshFirebaseToken(): SmartResult<String> {
        return try {
            val user = firebaseAuth.currentUser ?: return SmartResult.Error(604, null, "Current user could not be retrieved from firebaseAuth")

            val token = user.getIdToken(true).await().token
            if (token != null) {
                Preferences.setAuthToken(token)
                SmartResult.Success(token)
            } else {
                SmartResult.Error(604, "Forbidden", "Users ID token not found")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime Exception", "Originated from ${RetrofitClientExt.LOG_TAG}: \n${e.message}")
        }
    }

    /**
     * This function wraps network calls with the token management and safeCall logic.
     * It first tries with the current token, and if a 401 error occurs, it refreshes the token and retries once.
     * All calls are also wrapped in safeCall to handle exceptions and avoid crashes.
     */
    private suspend inline fun <reified T> performAuthRequest(
        request: (authHeader: String) -> Response<T>
    ): SmartResult<T> {
        val token = getFirebaseToken() ?: return SmartResult.Error(601, "Unauthorized", "Originated from ${RetrofitClientExt.LOG_TAG}")

        return RetrofitClientExt.safeCall {
            val result = request("Bearer $token")
            Log.e(LOG_TAG, "Token: $token")

            if (result.code() == 401 || result.code() == 403) {
                Log.e(LOG_TAG, "Retrying auth request with a refreshed token..")
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
                //Log.e(LOG_TAG, RetrofitClientExt.responseToSmartResult(result).toString())
                return RetrofitClientExt.responseToSmartResult(result)
            }
        }
    }

    override suspend fun inviteFriend(email: String): SmartResult<Unit> {

        return performAuthRequest { token ->
            if (getCurrentUserEmail() == email) {
                return SmartResult.Error(600, "Runtime Exception", "You cannot add yourself as friend!")
            }
            val inviteRequest = InviteRequest(email = email)
            RetrofitClient.friendsApi.inviteFriend(token, inviteRequest)
        }
    }

    override suspend fun getFriends(forceUpdate: Boolean): SmartResult<List<Friend>> {
        try {
            Log.v(LOG_TAG, "Starting repository's getFriends")
            Log.d(LOG_TAG, "Force refresh (loading from remote first) = $forceUpdate")

            // Check cache first if not forced to update
            if (!forceUpdate) {
                Log.d(LOG_TAG, "Trying to load friends from Room first..")
                val cachedFriends = friendsDao.getAllFriendsSortedByOrder()
                Log.d(LOG_TAG, "Cached friends were obtained from local: ${cachedFriends.size}")
                if (cachedFriends.isNotEmpty()) {
                    val result: List<Friend> = cachedFriends.map { it.toDomain() }
                    return SmartResult.Success(result)
                } else {
                    Log.i(LOG_TAG, "No cached data found, fetching from API")
                }
            }

            // Fetch from remote if forced or no cache
            val result = performAuthRequest { token ->
                Log.d(LOG_TAG, "Making API request to get friends")
                RetrofitClient.friendsApi.getFriends(token)
            }

            if (result is SmartResult.Success) {
                Log.d(LOG_TAG, "Successfully fetched friends from API: ${result.data}")
                val friendsWithAvatars = result.data?.mapIndexed { index, friend ->
                    val avatarUrl = friend.avatarId?.let {
                        val avatarUrlResult = pathToLink(it)
                        if (avatarUrlResult is SmartResult.Success) avatarUrlResult.data else ""
                    } ?: ""
                    friend.copy(
                        avatarId = avatarUrl,
                        lastMessageDate = friend.lastMessageDate
                    )
                }

                val friendsWithOrderIndex = friendsWithAvatars?.mapIndexed { index, friend ->
                    friend.toEntity().copy(orderIndex = index)
                }

                if (!friendsWithOrderIndex.isNullOrEmpty()) {
                    Log.d(LOG_TAG, "Inserting ${friendsWithOrderIndex.size} friends into Room")
                    friendsDao.deleteAllFriends()
                    friendsDao.insertFriends(friendsWithOrderIndex)
                }

                return SmartResult.Success(friendsWithAvatars?.filter { it.status == FriendshipStatus.accepted || it.status == FriendshipStatus.blocked })
            } else {
                return result
            }
        } catch (e: Exception) {
            return SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun getFriendRequests(forceUpdate: Boolean): SmartResult<List<Friend>> {
        try {
            Log.v(LOG_TAG, "Starting repository's getFriendRequests")
            Log.d(LOG_TAG, "Force refresh (loading from remote first) = $forceUpdate")
            // Check cache first if not forced to update
            if (!forceUpdate) {
                Log.d(LOG_TAG, "Trying to load requests from Room first..")
                val cachedFriendRequests = friendsDao.getAllFriendRequests() // Load requests with status = pending
                Log.d(LOG_TAG, "Cached requests were obtained from local: ${cachedFriendRequests.size}")
                if (cachedFriendRequests.isNotEmpty()) {
                    return SmartResult.Success(cachedFriendRequests.map { it.toDomain() })
                } else {
                    Log.i(LOG_TAG, "No cached data found, fetching from API")
                }
            }

            // Fetch from remote if forced or no cache
            val result = performAuthRequest { token ->
                Log.d(LOG_TAG, "Making API request to get requests")
                RetrofitClient.friendsApi.getFriendRequests(token)
            }

            if (result is SmartResult.Success) {
                Log.d(LOG_TAG, "Successfully fetched requests from API: ${result.data?.size}")
                val friendsWithAvatars = result.data!!.map { friend ->
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

                if (friendsWithAvatars.isNotEmpty()) {
                    Log.d(LOG_TAG, "Inserting ${friendsWithAvatars.size} requests into Room")
                    friendsDao.deleteAllFriends() // Clear old cache
                    friendsDao.insertFriends(friendsWithAvatars.map { it.toEntity() })
                } else {
                    Log.d(LOG_TAG, "No friend requests to insert into Room")
                }


                return SmartResult.Success(friendsWithAvatars.filter { it.status == FriendshipStatus.pending })
            } else {
                return SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "Could not get friend requests")
            }
        } catch (e: Exception) {
            return SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun acceptFriendRequest(acceptRequest: AcceptRequest): SmartResult<Unit> {
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.acceptFriendRequest(token, acceptRequest)
        }
    }

    override suspend fun declineFriendRequest(declineRequest: DeclineRequest): SmartResult<Unit> {
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.declineFriendRequest(token, declineRequest)
        }
    }

    override suspend fun createFriendshipWithLink(linkFriendshipRequest: LinkFriendshipRequest): SmartResult<Unit> {
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.createFriendshipWithLink(token, linkFriendshipRequest)
        }
    }

    override suspend fun getLinkRequestData(linkFriendshipRequest: LinkFriendshipRequest): SmartResult<LinkRequestData> {
        Log.e(RetrofitClientExt.LOG_TAG, "Request: ${linkFriendshipRequest}")
        return performAuthRequest { token ->
            RetrofitClient.friendsApi.getLinkRequestData(token, linkFriendshipRequest)
        }
    }

    override suspend fun sendOneTimeCode(oneTimeCode: Int): SmartResult<Unit> {
        return try {
            performAuthRequest { token ->
                RetrofitClient.friendsApi.sendOneTimeCode(token, SendCodeRequest(oneTimeCode))
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun removeUser(friendshipId: Int): SmartResult<Unit> {
        return try {
            Log.d("removeUser", "Removing friend with ID: $friendshipId")
            performAuthRequest { token ->
                Log.d("removeUser", "Using token: $token")
                RetrofitClient.friendsApi.removeFriend(token, RemoveFriendRequest(friendshipId))
            }
        } catch (e: Exception) {
            Log.e("removeUser", "Exception in removeUser(): " + e.message)
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun blockUser(friendshipId: Int, userId: Int): SmartResult<Unit> {
        return try {
            Log.e("repo", "Blocking user with friendship ID: $friendshipId by user $userId")
            performAuthRequest { token ->
                Log.d("blockUser", "Using token: $token")
                RetrofitClient.friendsApi.blockUser(token, BlockRequest(friendshipId, userId))
            }
        } catch (e: Exception) {
            Log.e("blockUser", "Exception in blockUser(): " + e.message)
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun unblockUser(friendshipId: Int, userId: Int): SmartResult<Unit> {
        return try {
            Log.d("unblockUser", "Unblocking user with friendship ID: $friendshipId by user $userId")
            performAuthRequest { token ->
                Log.d("unblockUser", "Using token: $token")
                RetrofitClient.friendsApi.unblockUser(token, UnblockRequest(friendshipId, userId))
            }
        } catch (e: Exception) {
            Log.e("unblockUser", "Exception in unblockUser(): " + e.message)
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun getUserData(): SmartResult<UserDataResponse> {
        return try {
            // If no local data, make a remote request
            val result = performAuthRequest { token ->
                RetrofitClient.userApi.getUserData(token)
            }

            if (result is SmartResult.Success) {
                Log.e(LOG_TAG, "getUserData, no cached data, so calling remote..")
                val userData = result.data ?: return SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "User data is null")
                val avatarUrlResult = pathToLink(userData.avatarId)
                val avatarUrl = if (avatarUrlResult is SmartResult.Success) avatarUrlResult.data ?: "" else ""

                // Convert to UserDataResponse and cache in Room
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
                SmartResult.Success(userDataResponse)
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "Could not get user data")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun getUserDataById(recipientId: String, forceUpdate: Boolean): SmartResult<UserDataResponse> {
        return try {
            if(!forceUpdate) {
                // Check if the user data is available in the local database
                val cachedUser = userDao.getUserById(recipientId.toInt())
                Log.e(LOG_TAG, "getUserDataById, cached data: $cachedUser")
                if (cachedUser != null) {
                    Log.e(LOG_TAG, "getUserDataById, cached data: ${cachedUser.toUserDataResponse()}")
                    return SmartResult.Success(cachedUser.toUserDataResponse())
                }
            }

            // If no local data, make a remote request
            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.getRecipientData(token, recipientId.toInt())
            }

            if (result is SmartResult.Success) {
                Log.e(LOG_TAG, "getUserDataById, no cached data, so calling remote..")
                val userData = result.data ?: return SmartResult.Error(600, "Runtime exception in $LOG_TAG.getUserDataById():", "Data is null")
                val avatarUrlResult = pathToLink(userData.avatarId)
                val avatarUrl = if (avatarUrlResult is SmartResult.Success) avatarUrlResult.data ?: "" else ""

                // Convert to UserDataResponse and cache in Room
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

                // Cache the result in Room
                Log.e(LOG_TAG, "getUserDataById, remote data obtained and inserting into room: ${userDataResponse.toUserEntity()}")
                userDao.insertUser(userDataResponse.toUserEntity())
                SmartResult.Success(userDataResponse)
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG.getUserDataById():", "Data is null")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG.getUserDataById():", "Data is null: ${e.message}")
        }
    }

    override suspend fun updateProfile(avatarId: String?, nick: String?): SmartResult<Unit> {
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
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    // S3:
    override suspend fun uploadFile(file: File, folder: S3Handler.Folder): SmartResult<String> {
        return try {
            val avatarId = S3Handler.uploadToS3(file, folder)
            if (avatarId != null) {
                SmartResult.Success(avatarId)
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "Couldnt upload to s3")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun pathToLink(path: String): SmartResult<String> {
        return try {
            val avatarUrl = S3Handler.pathToDownloadableLink(path)
            if (avatarUrl != null) {
                SmartResult.Success(avatarUrl)
            } else {
                SmartResult.Error(604, null, "Could not convert path to downloadable link")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, null, "${LOG_TAG} in ${e.message}")
        }
    }

    override suspend fun changeWallpaper(request: ChangeWallpaperRequest): SmartResult<ChangeWallpaperResponse?> {
        return try {
            Log.v(LOG_TAG, "changeWallpaper, start")

            if (!AppState.isPremium.value && AppState.devilCount.value <= 0) {
                return SmartResult.Error(600, "Not enough $EVIL_EMOJI to send wallpaper, get more by watching ads", "Not enough $EVIL_EMOJI to send wallpaper")
            }

            val response = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.changeWallpaper(token, request)
            }
            if (response is SmartResult.Success) {
                if (response.data?.data != null) {
                    Log.v(LOG_TAG, "changeWallpaper, success! Inserting data to ROOM: response.data?.data = ${response.data.data}")
                    val wallpaperUrlResult = pathToLink(response.data.data.fileName)
                    val wallpaperUrl = if (wallpaperUrlResult is SmartResult.Success) {
                        wallpaperUrlResult.data
                    } else {
                        ""
                    }
                    chatDao.insertMessages(listOf(response.data.data.toMessageEntity().copy(fileName = wallpaperUrl?:"")))
                }
                if (!AppState.isPremium.value) addDevils(-1)

                SmartResult.Success(response.data?.data)
            } else if (response is SmartResult.Error) {
                if (response.code == 404) {
                    Log.e(LOG_TAG, "This user has no push token, probably uninstalled the app")
                    val editedResponse = response.copy(errorBody = "This user restricted sending wallpapers", message = "This user restricted sending wallpapers, probably uninstalled the app")
                    editedResponse
                } else {
                    Log.e(LOG_TAG, "changeWallpaper result: $response")
                    response
                }
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG.changeWallpaper():", "wallpaperApi could not change wallpaper")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG.changeWallpaper():", "wallpaperApi could not change wallpaper")
        }
    }

    override suspend fun getWallpaperHistory(
        userId: String,
        page: Int,
        pageSize: Int,
        forceUpdate: Boolean
    ): SmartResult<List<WallpaperHistoryResponse>> {
        Log.e(LOG_TAG, "getWallpaperHistory(), start. forceUpdate = $forceUpdate")

        val userIdInt = userId.toInt()
        val messageCount = chatDao.getMessageCountForUser(userIdInt)
        Log.e(LOG_TAG, "getWallpaperHistory(), cached data size: $messageCount")

        if (!forceUpdate) {
            if (messageCount != 0) {
                val cachedMessages = chatDao.getMessagesForUser(userIdInt, page, pageSize)
                Log.e(LOG_TAG, "getWallpaperHistory(), returning cached messages from ROOM..")
                return SmartResult.Success(cachedMessages.map { it.toDomain() })
            } else {
                return getWallpaperHistoryFromRemote(userId, page, pageSize)
            }
        } else {
            return getWallpaperHistoryFromRemote(userId, page, pageSize)
        }
    }

    override suspend fun saveMessageToLocal(message: Message) {
        Log.v(LOG_TAG, "saveMessageToLocal, start. Inserting data to ROOM: message = ${message}")
        chatDao.insertMessages(listOf(message.toMessageEntity()))
    }

    private suspend fun getWallpaperHistoryFromRemote(
        userId: String,
        page: Int,
        pageSize: Int
    ): SmartResult<List<WallpaperHistoryResponse>> {
        return try {
            Log.e(
                LOG_TAG,
                "getWallpaperHistory(), no cached data found, fetching stuff from remote API.."
            )
            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.getWallpaperHistory(
                    token,
                    userId.toInt(),
                    page,
                    pageSize
                )
            }
            if (result is SmartResult.Success) {
                val mappedWallpapers = result.data?.data?.map { wallpaper ->
                    val wallpaperUrlResult = pathToLink(wallpaper.fileName)
                    val wallpaperUrl = if (wallpaperUrlResult is SmartResult.Success) {
                        wallpaperUrlResult.data
                    } else {
                        ""
                    }
                    wallpaper.copy(fileName = wallpaperUrl ?: "")
                } ?: emptyList()

                if (page == 0) {
                    chatDao.insertMessages(mappedWallpapers.map { it.toEntity() })
                }
                SmartResult.Success(mappedWallpapers)
            } else {
                return SmartResult.Error(
                    600,
                    "Runtime exception in $LOG_TAG.getWallpaperHistory():",
                    "Data is null"
                )
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception in getWallpaperHistory(): ${e.message}")
            return SmartResult.Error(
                600,
                "Runtime exception in $LOG_TAG.getWallpaperHistory():",
                "Data is null"
            )
        }
    }

    override suspend fun react(wallpaperId: Int, reaction: String?): SmartResult<Unit> {
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
            SmartResult.Error(600, "Runtime exception in $LOG_TAG.react():", "Could not react: ${e.message}")
        }
    }

    override suspend fun comment(wallpaperId: Int, comment: String?): SmartResult<Unit> {
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
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "Could not comment: ${e.message}")
        }
    }

    override suspend fun markMessagesAsRead(friendshipId: Int, lastMessageId: Int): SmartResult<Unit> {
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
                Log.e("ChatRepository", "Failed to mark messages as read. Error: ${result.errorBody}")
            } else {
                Log.e("ChatRepository", "Unexpected result: $result")
            }

            result
        } catch (e: Exception) {
            Log.e("ChatRepository", "Exception while marking messages as read: ${e.localizedMessage}")
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "Could not mar as read: ${e.message}")
        }
    }

    override suspend fun loadExploreWallpapers(
        page: Int,
        pageSize: Int,
        forceRefresh: Boolean
    ): SmartResult<List<ExploreWallpaperResponse>> {
        Log.v("Repository", "Starting repo loadExploreWallpapers with page: $page, pageSize: $pageSize")
        Log.d(LOG_TAG, "Force refresh (loading from remote first) = $forceRefresh")

        if (!forceRefresh) {
            Log.d(LOG_TAG, "Trying to load explore wallpapers from Room first..")
            val cachedWallpapers = exploreDao.getAllWallpapersSortedByOrder()
            Log.d(LOG_TAG, "Cached wallpapers were obtained from local: ${cachedWallpapers.size}")
            if (cachedWallpapers.isNotEmpty()) {
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
                return SmartResult.Success(modifiedCachedWallpapers) // Return the modified list
            } else {
                Log.i(LOG_TAG, "No cached data found, fetching from API")
            }
        }

        // If no cached data, fetch from the API
        return try {
            val result = performAuthRequest { token ->
                Log.d(LOG_TAG, "Making API request")
                RetrofitClient.wallpaperApi.loadExploreWallpapers(token, page, pageSize)
            }

            if (result is SmartResult.Success) {
                Log.d(LOG_TAG, "Successfully fetched wallpapers from API: ${result.data?.size}")

                // Cache the fetched data, preserving the order by assigning a sequential "order" field
                val entitiesToInsert = result.data?.mapIndexed { index, wallpaper ->
                    val avatarUrlResult = pathToLink(wallpaper.fileName)
                    ExploreWallpaperEntity(
                        id = wallpaper.id,
                        fileName = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data?:""
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
                if (entitiesToInsert != null) {
                    Log.d(LOG_TAG, "Inserting ${entitiesToInsert.size} wallpapers into Room")
                }
                if (entitiesToInsert != null) {
                    exploreDao.insertWallpapers(entitiesToInsert)
                }

                // Also, return the modified list from the API result in the original order
                val modifiedApiWallpapers = result.data?.map { wallpaper ->
                    val avatarUrlResult = pathToLink(wallpaper.fileName)
                    ExploreWallpaperResponse(
                        id = wallpaper.id,
                        fileName = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data?:""
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
                return SmartResult.Success(modifiedApiWallpapers) // Return modified API data
            } else {
                Log.e(LOG_TAG, "API request failed: ${result}")
            }
            result // Return the result (Success/Error)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Exception occurred: ${e.message}", e)
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun saveWallpaper(wallpaperId: Int): SmartResult<Unit> {
        return try {
            performAuthRequest { token ->
                RetrofitClient.wallpaperApi.saveWallpaper(token, SaveWallpaperRequest(wallpaperId))
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun removeSavedWallpaper(wallpaperId: Int): SmartResult<Unit> {
        return try {
            performAuthRequest { token ->
                RetrofitClient.wallpaperApi.removeSavedWallpaper(token, RemoveSavedWallpaperRequest(wallpaperId))
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun loadSavedWallpapers(page: Int, pageSize: Int): SmartResult<List<ExploreWallpaperResponse>> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.loadSavedWallpapers(token, page, pageSize)
            }
            Log.e("loadSavedWallpapers in repo", "result: $result")
            if (result is SmartResult.Success) {
                val withLoadedImages = result.data?.map {
                    if (it.fileName == null) {
                        it.copy(fileName = "")
                    } else {
                        val avatarUrlResult = pathToLink(it.fileName)
                        val avatarUrl = if (avatarUrlResult is SmartResult.Success) {
                            avatarUrlResult.data
                        } else {
                            ""
                        }
                        it.copy(fileName = avatarUrl?:"")
                    }
                }
                SmartResult.Success(withLoadedImages)
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "Could not load saved wallpapers")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun reportWallpaper(wallpaperId: Int): SmartResult<Unit> {
        return try {
            val result = performAuthRequest { token ->
                RetrofitClient.wallpaperApi.reportWallpaper(token, ReportRequest(wallpaperId))
            }
            if (result is SmartResult.Success) {
                SmartResult.Success(Unit)
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "Could not report")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }

    override suspend fun getFriendScreenRatio(friendId: Int): SmartResult<Float> {
        return try {
            val result =  performAuthRequest { token ->
                RetrofitClient.userApi.getFriendScreenRatio(token, friendId)
            }
            if (result is SmartResult.Success) {
                val screenRatio = result.data?.screenRatio
                SmartResult.Success(screenRatio)
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG:", "friends screen ratio not found")
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG:", e.message)
        }
    }


    override suspend fun addDevils(count: Int): SmartResult<Unit> {
        return try {
            val currentCount = Preferences.getDevilsCount()
            val newCount = currentCount + count
            Preferences.setDevilsCount(newCount)
            AppState.updateDevilCount(AppState.devilCount.value + count)
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.updateAppData(token, UpdateAppDataRequest(devilCount = newCount))
            }
            Log.e(LOG_TAG, "addDevils: Current count: $currentCount, Added: $count, New count: $newCount")
            if (result is SmartResult.Success) {
                SmartResult.Success(Unit)
            } else {
                SmartResult.Error(600, "Runtime exception in $LOG_TAG: ", result.toString())
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG: ", e.message)
        }
    }

    override suspend fun getDevilCount(): Int {
        Log.e(LOG_TAG, "getDevilCount() start")
        var count = Preferences.getDevilsCount()
        if (count != -1) {
            Log.e(LOG_TAG, "getDevilCount from local: $count")
            return count
        } else {
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.getAppData(token)
            }
            if (result is SmartResult.Success) {
                count = result.data?.devilCount?:0
                Log.e(LOG_TAG, "getDevilCount from remote: $count, result.data?.devilCount: ${result.data?.devilCount}")
            } else {
                count = 10
                Log.e(LOG_TAG, "getDevilCount from remote (error): $count")
            }
        }
        Log.e(LOG_TAG, "Final devils count $count")
        Preferences.setDevilsCount(count)
        AppState.updateDevilCount(count)
        return count
    }

    override suspend fun updatePremiumStatus(isPremium: Boolean): SmartResult<Unit> {
        return try {
            Preferences.setPremiumStatus(isPremium)
            AppState.updatePremiumStatus(isPremium)
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.updateAppData(token, UpdateAppDataRequest(isPremium = isPremium))
            }
            Log.e(LOG_TAG, "updatePremiumStatus: Set to $isPremium")
            if (result is SmartResult.Success) {
                SmartResult.Success(Unit)
            } else {
                SmartResult.Error(600, "Failed to update premium status on server", result.toString())
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG: ", e.message)
        }
    }

    override suspend fun isPremium(forceUpdate: Boolean): Boolean {
        var isPremium = Preferences.isPremium()
        val checkValue = Preferences.getDevilsCount() // This approach does not work
        Log.e(LOG_TAG, "isPremium from prefs: $isPremium")
        if (checkValue == -1 || forceUpdate) {
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.getAppData(token)
            }
            if (result is SmartResult.Success) {
                isPremium = result.data?.isPremium == 1
                Log.e(LOG_TAG, "isPremium from remote: $isPremium")
                Preferences.setPremiumStatus(isPremium)
                AppState.updatePremiumStatus(isPremium)
            } else {
                // If there's an error and no local data, default to premium status
                isPremium = true
                Preferences.setPremiumStatus(isPremium)
                AppState.updatePremiumStatus(isPremium)
            }
        }
        Log.e(LOG_TAG, "isPremium: $isPremium")
        AppState.updatePremiumStatus(isPremium)
        return isPremium
    }

    override suspend fun getLastCheckInDate(): String? {
        var lastDate = Preferences.getLastCheckInDate()
        Log.e(LOG_TAG, "getLastCheckInDate from prefs: $lastDate")
        if (lastDate.isNullOrEmpty()) {
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.getAppData(token)
            }
            if (result is SmartResult.Success) {
                lastDate = result.data?.lastCheckInDate
                Log.e(LOG_TAG, "getLastCheckInDate from remote: $lastDate")
                if (lastDate != null) {
                    Preferences.setLastCheckInDate(lastDate)
                }
            }
        }
        Log.e(LOG_TAG, "final getLastCheckInDate: $lastDate")
        return lastDate
    }

    override suspend fun setLastCheckInDate(date: String): SmartResult<Unit> {
        return try {
            Preferences.setLastCheckInDate(date)
            AppState.updateLastCheckinDate(date)
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.updateAppData(token, UpdateAppDataRequest(lastCheckInDate = date))
            }
            Log.e(LOG_TAG, "setLastCheckInDate: Date set to $date")
            if (result is SmartResult.Success) {
                SmartResult.Success(Unit)
            } else {
                SmartResult.Error(600, "Failed to update last check-in date on server", result.toString())
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG: ", e.message)
        }
    }

    override suspend fun hasCheckedInToday(): Boolean {
        val lastCheckInDate = getLastCheckInDate()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hasCheckedIn = lastCheckInDate == today
        Log.e(LOG_TAG, "hasCheckedInToday: Checked in today? $hasCheckedIn")
        return hasCheckedIn
    }

    override suspend fun getConsecutiveDays(): Int {
        var consecutiveDays = Preferences.getConsecutiveDays()
        Log.e(LOG_TAG, "getConsecutiveDays from prefs: $consecutiveDays")
        if (consecutiveDays == -1) {  // Assuming -1 indicates no local data available
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.getAppData(token)
            }
            if (result is SmartResult.Success) {
                consecutiveDays = result.data?.consecutiveDays ?: 0
                Log.e(LOG_TAG, "getConsecutiveDays from remote: $consecutiveDays")
                Preferences.setConsecutiveDays(consecutiveDays)
                AppState.updateConsecutiveDays(consecutiveDays)
            }
        }
        Log.e(LOG_TAG, "getConsecutiveDays: $consecutiveDays")
        return consecutiveDays
    }

    override suspend fun setConsecutiveDays(days: Int): SmartResult<Unit> {
        return try {
            Preferences.setConsecutiveDays(days)
            AppState.updateConsecutiveDays(days)
            val result = performAuthRequest { token ->
                RetrofitClient.userAppDataApi.updateAppData(token, UpdateAppDataRequest(consecutiveDays = days))
            }
            Log.e(LOG_TAG, "setConsecutiveDays: Consecutive days set to $days")
            if (result is SmartResult.Success) {
                SmartResult.Success(Unit)
            } else {
                SmartResult.Error(600, "Failed to update consecutive days on server", result.toString())
            }
        } catch (e: Exception) {
            SmartResult.Error(600, "Runtime exception in $LOG_TAG: ", e.message)
        }
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




