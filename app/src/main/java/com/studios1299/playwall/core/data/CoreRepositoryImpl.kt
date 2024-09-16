package com.studios1299.playwall.core.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.studios1299.playwall.core.data.local.PreferencesDataSource
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.core.domain.model.WallpaperOption
import com.studios1299.playwall.explore.presentation.explore.Photo
import com.studios1299.playwall.feature.play.data.model.Message
import com.studios1299.playwall.feature.play.data.model.MessageStatus
import com.studios1299.playwall.feature.play.data.model.Reaction
import com.studios1299.playwall.feature.play.data.model.User
import com.studios1299.playwall.feature.play.presentation.play.Friend
import com.studios1299.playwall.feature.play.presentation.play.FriendRequest
import kotlinx.coroutines.delay
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

    override suspend fun getUserProfile(): UserProfile {
        return UserProfile(
            name = "John Doe",
            email = "johndoe@example.com",
            avatarUrl = "https://media.licdn.com/dms/image/D4D03AQG510ilgQaD_g/profile-displayphoto-shrink_200_200/0/1709116748493?e=2147483647&v=beta&t=rfehlo_FlkkyBXfptFpsVWBUcNnQbID_dR0Ght21TTw"
        )
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
private val _friends = mutableListOf<Friend>()
private val _requests = mutableListOf<FriendRequest>()

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
    _friends.addAll(
        listOf(
            Friend(
                id = "1",
                name = "Alice",
                avatar = "https://www.shutterstock.com/image-photo/adult-female-avatar-image-on-260nw-2420293027.jpg",
                lastMessage = "Sent you a wallpaper · 1m",
                unreadMessages = 2,
                muted = false,
            ),
            Friend(
                id = "2",
                name = "Bob",
                avatar = "https://www.dell.com/wp-uploads/2022/11/Human-like-Avatar-2-640x480.jpg",
                lastMessage = "Sent you a wallpaper · 5h",
                unreadMessages = 99,
                muted = false,
            ),
            Friend(
                id = "3",
                name = "Charlie",
                avatar = "https://www.researchgate.net/profile/Kai-Riemer/publication/313794667/figure/fig4/AS:462513214103554@1487283151275/The-same-image-as-Figure-3-rotated-180-degrees-note-how-crude-the-composite-seems_Q320.jpg",
                lastMessage = "Sent you a wallpaper · 1m",
                unreadMessages = 0,
                muted = true,
            ),
            Friend(
                id = "4",
                name = "Derek",
                avatar = "https://www.cgw.com/images/Media/PublicationsArticle/pg30b.jpg",
                lastMessage = "You sent a wallpaper · 1d",
                unreadMessages = 0,
                muted = false,
            ),
            Friend(
                id = "5",
                name = "John",
                avatar = "-3-rotated-180-degrees-note-how-crude-the-composite-seems_Q320.jpg",
                lastMessage = "Sent you a wallpaper · 5mo",
                unreadMessages = 0,
                muted = false,
            ),
            Friend(
                id = "6",
                name = "Tim",
                avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                lastMessage = "You sent a wallpaper · 5d",
                unreadMessages = 0,
                muted = true,
            ),
            Friend(
                id = "7",
                name = "Chuck",
                avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                lastMessage = null,
                unreadMessages = 0,
                muted = false,
            ),
            Friend(
                id = "8",
                name = "Tristan",
                avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                lastMessage = null,
                unreadMessages = 0,
                muted = false,
            ),
            Friend(
                id = "9",
                name = "Andrew",
                avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                lastMessage = null,
                unreadMessages = 0,
                muted = false,
            ),
            Friend(
                id = "10",
                name = "Sam",
                avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                lastMessage = null,
                unreadMessages = 0,
                muted = false,
            ),
            Friend(
                id = "11",
                name = "Bob",
                avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                lastMessage = null,
                unreadMessages = 0,
                muted = false,
            )

        )
    )
    _requests.addAll(
        listOf(
            FriendRequest(
                id = "12",
                name = "David",
                avatar = "https://comicvine.gamespot.com/a/uploads/scale_small/3/39768/3633678-600full-ian-mckellen.jpg"
            ),
            FriendRequest(
                id = "13",
                name = "Eva",
                avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3633865-magneto-daniel%20craig.jpg"
            )
        )
    )
}
private val _users = listOf(
    User(
        id = "user1",
        name = "Viktor Didyk",
        email = "vi.didyk@gmail.com",
        profilePictureUrl = "https://media.licdn.com/dms/image/D4D03AQG510ilgQaD_g/profile-displayphoto-shrink_200_200/0/1709116748493?e=2147483647&v=beta&t=rfehlo_FlkkyBXfptFpsVWBUcNnQbID_dR0Ght21TTw"
    ),
    User(
        id = "user2",
        name = "Andrii White",
        email = "ww@gmail.com",
        profilePictureUrl = "https://lithelper.com/wp-content/uploads/2020/05/tom1.jpeg"
    ),
    // Add more users as needed
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




override fun getFriends(): List<Friend> {
    return _friends
}

override fun getFriendRequests(): List<FriendRequest> {
    return _requests
}

override fun acceptFriendRequest(requestId: String): Boolean {
    val request = _requests.find { it.id == requestId }
    return if (request != null) {
        _requests.remove(request)
        _friends.add(
            Friend(
                id = request.id,
                name = request.name,
                avatar = request.avatar,
                lastMessage = null,
                unreadMessages = 0,
                muted = false
            )
        )
        true
    } else {
        false
    }
}

override fun rejectFriendRequest(requestId: String): Boolean {
    val request = _requests.find { it.id == requestId }
    return if (request != null) {
        _requests.remove(request)
        true
    } else {
        false
    }
}

override fun searchUsers(query: String): List<User> {
    if (query.length < 3) {
        return emptyList()
    }
    return _users.filter {
        it.email.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true)
    }
}
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




data class UserProfile(
    val name: String,
    val email: String,
    val avatarUrl: String
)
