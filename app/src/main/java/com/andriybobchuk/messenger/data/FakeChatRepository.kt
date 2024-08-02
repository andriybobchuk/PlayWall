package com.andriybobchuk.messenger.data

import com.andriybobchuk.messenger.model.Message
import com.andriybobchuk.messenger.model.MessageStatus
import com.andriybobchuk.messenger.model.Reaction
import com.andriybobchuk.messenger.model.User
import kotlinx.coroutines.delay
import java.util.UUID

class FakeChatRepository {

    // Fake in-memory storage
    private val _messages = mutableListOf<Message>()
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
        // Pre-populate with example messages
        _messages.addAll(
            listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://www.joiipetcare.com/wp-content/uploads/2023/05/BOAS-cat-1-compressed.jpg",
                    caption = "Look at this cutie!",
                    timestamp = System.currentTimeMillis() - 86400000 * 5,
                    status = MessageStatus.SENT,
                    reactions = listOf(Reaction("user2", "❤️")),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcJVO3i3NJtecXei14edIi7shKj2F2fpRcjg&s",
                    caption = "Funny cat!",
                    timestamp = System.currentTimeMillis() - 86400000 * 4,
                    status = MessageStatus.DELIVERED,
                    reactions = listOf(Reaction("user3", "😂")),
                    senderId = _recipient.id,
                    recipientId = _currentUser.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://st3.depositphotos.com/30152186/37144/i/450/depositphotos_371445966-stock-photo-funny-kitten-grass-summer.jpg",
                    caption = "Amazing!",
                    timestamp = System.currentTimeMillis() - 86400000 * 2,
                    status = MessageStatus.READ,
                    reactions = listOf(Reaction("user1", "😮")),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://www.purina.co.nz/sites/default/files/2020-11/6-Small-Cat-BreedsHERO.jpg",
                    caption = "So cute!",
                    timestamp = System.currentTimeMillis() - 86400000 * 1,
                    status = MessageStatus.SENT,
                    reactions = listOf(Reaction("user4", "😍")),
                    senderId = _recipient.id,
                    recipientId = _currentUser.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://www.purina.co.nz/sites/default/files/styles/ttt_image_510/public/2020-11/6-Small-Cat-Breeds1.jpg?itok=vRRyOFAB",
                    caption = "",
                    timestamp = System.currentTimeMillis() - 86400000 * 3,
                    status = MessageStatus.DELIVERED,
                    reactions = listOf(),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://www.omlet.us/images/originals/Cat-Cat_Guide-A_Devon_Rex_cat_showing_off_its_wonderful_pointed_ear_tips.jpg",
                    caption = "Relaxing day",
                    timestamp = System.currentTimeMillis() - 86400000 * 6,
                    status = MessageStatus.READ,
                    reactions = listOf(Reaction("user2", "👍")),
                    senderId = _recipient.id,
                    recipientId = _currentUser.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://i.pinimg.com/736x/c3/f1/ab/c3f1ab06a04e326d71c8c8f835ab0f11.jpg",
                    caption = "Lovely dog!",
                    timestamp = System.currentTimeMillis() - 86400000 * 1,
                    status = MessageStatus.SENT,
                    reactions = listOf(Reaction("user3", "❤️")),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://www.catster.com/wp-content/uploads/2023/12/rsz_shutterstock_147812990-1.jpg",
                    caption = "Beautiful scenery",
                    timestamp = System.currentTimeMillis() - 86400000 * 7,
                    status = MessageStatus.DELIVERED,
                    reactions = listOf(Reaction("user4", "👍")),
                    senderId = _recipient.id,
                    recipientId = _currentUser.id
                ),



                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://cdn.britannica.com/34/235834-050-C5843610/two-different-breeds-of-cats-side-by-side-outdoors-in-the-garden.jpg",
                    caption = "Nice view!",
                    timestamp = System.currentTimeMillis() - 86400000 * 3,
                    status = MessageStatus.SENT,
                    reactions = listOf(Reaction("user1", "👍")),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://static.scientificamerican.com/sciam/cache/file/32665E6F-8D90-4567-9769D59E11DB7F26_source.jpg?w=1200",
                    caption = "",
                    timestamp = System.currentTimeMillis() - 86400000 * 3,
                    status = MessageStatus.DELIVERED,
                    reactions = listOf(),
                    senderId = _recipient.id,
                    recipientId = _currentUser.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://ichef.bbci.co.uk/news/976/cpsprodpb/114BC/production/_127844807_henry_daniella_hutchinson.jpg",
                    caption = "Hello, how are you? this is an example of a long message",
                    timestamp = System.currentTimeMillis() - 86400000 * 2,
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
                    caption = "Here's a new photo.",
                    timestamp = System.currentTimeMillis() - 86400000 * 2,
                    status = MessageStatus.SENT,
                    reactions = listOf(
                        Reaction("user1", "👍"),
                        Reaction("user2", "🔥")
                    ),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://i.pinimg.com/736x/87/4c/9a/874c9a34512567d6b370e66c74bb8b28.jpg",
                    caption = "Did you see this?",
                    timestamp = System.currentTimeMillis() - 86400000,
                    status = MessageStatus.DELIVERED,
                    reactions = listOf(
                        Reaction("user2", "👏")
                    ),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://static.scientificamerican.com/sciam/cache/file/2AE14CDD-1265-470C-9B15F49024186C10_source.jpg?w=1200",
                    caption = "",
                    timestamp = System.currentTimeMillis() - 86400000,
                    status = MessageStatus.SENT,
                    reactions = listOf(
                        Reaction("user1", "😍"),
                        Reaction("user2", "👌")
                    ),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://www.petethevet.com/wp-content/uploads/2019/02/cat-1739091_1920.jpg",
                    caption = "Look at this!",
                    timestamp = System.currentTimeMillis() - 86400000,
                    status = MessageStatus.SENT,
                    reactions = listOf(
                        Reaction("user2", "😎")
                    ),
                    senderId = _recipient.id,
                    recipientId = _currentUser.id
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://www.thesprucepets.com/thmb/OoMBiCxD3B02Jx-WO9dmY0DAaaI=/4000x0/filters:no_upscale():strip_icc()/cats-recirc3_2-1f5de201af94447a9063f83249260aff.jpg",
                    caption = "",
                    timestamp = System.currentTimeMillis() - 5000000,
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
                    caption = "Another amazing photo.",
                    timestamp = System.currentTimeMillis() - 4500000,
                    status = MessageStatus.SENT,
                    reactions = listOf(
                        Reaction("user1", "🎉"),
                        Reaction("user2", "👍")
                    ),
                    senderId = _currentUser.id,
                    recipientId = _recipient.id
                ),
            )
        )
    }

    suspend fun retrieveMessages(page: Int, pageSize: Int): Result<List<Message>> {
        delay(3000L)
        val startingIndex = page * pageSize
        val sortedMessages = _messages.sortedByDescending { it.timestamp }
        return if (startingIndex + pageSize <= _messages.size) {
            Result.success(
                sortedMessages.slice(startingIndex until startingIndex + pageSize)
            )
        } else Result.success(emptyList())
    }

    fun addMessage(message: Message) {
        _messages.add(message)
    }

    fun deleteMessage(messageId: String) {
        _messages.removeAll { it.id == messageId }
    }

    fun updateMessage(message: Message) {
        val index = _messages.indexOfFirst { it.id == message.id }
        if (index != -1) {
            _messages[index] = message
        }
    }

    fun getCurrentUser(): User {
        return _currentUser
    }

    fun getRecipient(): User {
        return _recipient
    }

    fun getUserNameById(userId: String): String {
        if (userId == "user1") {
            return "Andrii Bobchuk"
        } else if (userId == "user2") {
            return "Tom Sawyer"
        }
        return "Unknown"
    }

    fun getMessageById(messageId: String): Message? {
        return _messages.find { it.id == messageId }
    }
}
