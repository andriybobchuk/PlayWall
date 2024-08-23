package com.studios1299.vrwallpaper6.feature.play.data

import android.util.Log
import com.studios1299.vrwallpaper6.feature.play.data.model.Message
import com.studios1299.vrwallpaper6.feature.play.data.model.MessageStatus
import com.studios1299.vrwallpaper6.feature.play.data.model.Reaction
import com.studios1299.vrwallpaper6.feature.play.data.model.User
import com.studios1299.vrwallpaper6.feature.play.domain.ChatRepository
import com.studios1299.vrwallpaper6.feature.play.presentation.screens.play.Friend
import com.studios1299.vrwallpaper6.feature.play.presentation.screens.play.FriendRequest
import kotlinx.coroutines.delay
import java.util.UUID

class FakeChatRepository : ChatRepository {
    companion object {
        private const val LOG_TAG = "FakeChatRepository"
    }

    private val _messages = mutableListOf<Message>()
    private val _friends = mutableListOf<Friend>()
    private val _requests = mutableListOf<FriendRequest>()

    private val _currentUser = User(
        id = "user1",
        name = "Andrii Bobchuk",
        profilePictureUrl = "https://media.licdn.com/dms/image/D4D03AQG510ilgQaD_g/profile-displayphoto-shrink_200_200/0/1709116748493?e=2147483647&v=beta&t=rfehlo_FlkkyBXfptFpsVWBUcNnQbID_dR0Ght21TTw",
        lastOnline = System.currentTimeMillis()
    )
    private val _recipient = User(
        id = "user2",
        name = "Tom Sawyer",
        profilePictureUrl = "https://lithelper.com/wp-content/uploads/2020/05/tom1.jpeg",
        lastOnline = System.currentTimeMillis()
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
                    lastMessage = "See you soon!",
                    unreadMessages = 2
                ),
                Friend(
                    id = "2",
                    name = "Bob",
                    avatar = "https://www.dell.com/wp-uploads/2022/11/Human-like-Avatar-2-640x480.jpg",
                    lastMessage = "Thanks for the help!",
                    unreadMessages = 0
                ),
                Friend(
                    id = "3",
                    name = "Charlie",
                    avatar = "https://www.researchgate.net/profile/Kai-Riemer/publication/313794667/figure/fig4/AS:462513214103554@1487283151275/The-same-image-as-Figure-3-rotated-180-degrees-note-how-crude-the-composite-seems_Q320.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "4",
                    name = "Derek",
                    avatar = "https://www.cgw.com/images/Media/PublicationsArticle/pg30b.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "5",
                    name = "John",
                    avatar = "-3-rotated-180-degrees-note-how-crude-the-composite-seems_Q320.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "6",
                    name = "Tim",
                    avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "7",
                    name = "Chuck",
                    avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "8",
                    name = "Tristan",
                    avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "9",
                    name = "Andrew",
                    avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "10",
                    name = "Sam",
                    avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                ),
                Friend(
                    id = "11",
                    name = "Bob",
                    avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3639665-temp4521.jpg",
                    lastMessage = null,
                    unreadMessages = 0
                )

            )
        )
        _requests.addAll(
            listOf(
                FriendRequest(
                    id = "7",
                    name = "David",
                    avatar = "https://comicvine.gamespot.com/a/uploads/scale_small/3/39768/3633678-600full-ian-mckellen.jpg"
                ),
                FriendRequest(
                    id = "8",
                    name = "Eva",
                    avatar = "https://comicvine.gamespot.com/a/uploads/original/3/39768/3633865-magneto-daniel%20craig.jpg"
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
                    unreadMessages = 0
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
}
