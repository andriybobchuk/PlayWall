package com.andriybobchuk.messenger
//
//import android.net.Uri
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.andriybobchuk.messenger.data.ChatRepository
//import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.TestCoroutineDispatcher
//import kotlinx.coroutines.test.runBlockingTest
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class)
//class ChatViewModelTest {
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var chatViewModel: ChatViewModel
//    private val testDispatcher = TestCoroutineDispatcher()
//    private lateinit var mockRepository: ChatRepository
//
//    @Before
//    fun setup() {
//        // Create a mock ChatRepository
//        mockRepository = mockk()
//
//        // Initialize ChatViewModel with the mock repository
//        chatViewModel = ChatViewModel(mockRepository)
//
//        Dispatchers.setMain(testDispatcher) // Set the Main dispatcher to the test dispatcher
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain() // Reset the Main dispatcher
//        testDispatcher.cleanupTestCoroutines()
//    }
//
//    @Test
//    fun testLoadMessages() = testDispatcher.runBlockingTest {
//        val testMessages = List(5) { index -> Message(id = "$index", imageUrl = "", caption = "", timestamp = 0, status = MessageStatus.SENT, reactions = listOf(), senderId = "", recipientId = "") }
//        coEvery { mockRepository.retrieveMessages(any(), any()) } returns testMessages
//
//        chatViewModel.loadMessages()
//
//        advanceUntilIdle()
//
//        val uiState = chatViewModel.uiState.value
//        assertEquals(5, uiState.messages.size) // Assuming PAGE_SIZE = 5
//    }
//
//    @Test
//    fun testSendImage() = testDispatcher.runBlockingTest {
//        val testUri = Uri.parse("https://test.com/image.jpg")
//        val testCaption = "Test Image"
//        val testUserId = "user123"
//        val testRecipientId = "recipient456"
//        val testMessageId = UUID.randomUUID().toString()
//        val timestamp = System.currentTimeMillis()
//
//        // Mock the repository behavior
//        coEvery { mockRepository.addMessage(any()) } just Runs
//        every { mockRepository.getCurrentUser() } returns User(id = testUserId, name = "Test User")
//        every { mockRepository.getRecipient() } returns User(id = testRecipientId, name = "Test Recipient")
//
//        chatViewModel.setCurrentUser(User(id = testUserId, name = "Test User"))
//        chatViewModel.setRecipient(User(id = testRecipientId, name = "Test Recipient"))
//
//        chatViewModel.sendImage(testUri, testCaption)
//
//        advanceUntilIdle()
//
//        val uiState = chatViewModel.uiState.value
//        assertEquals(testUri.toString(), uiState.messages[0].imageUrl)
//        assertEquals(testCaption, uiState.messages[0].caption)
//    }
//
//    @Test
//    fun testDeleteMessage() = testDispatcher.runBlockingTest {
//        val testMessageId = "testMessageId"
//        val existingMessages = listOf(Message(id = testMessageId, imageUrl = "", caption = "", timestamp = 0, status = MessageStatus.SENT, reactions = listOf(), senderId = "", recipientId = ""))
//
//        // Setup initial state
//        chatViewModel._uiState.value = MessengerUiState(messages = existingMessages)
//
//        // Mock repository
//        coEvery { mockRepository.deleteMessage(any()) } just Runs
//
//        chatViewModel.deleteMessage(testMessageId)
//
//        advanceUntilIdle()
//
//        val uiState = chatViewModel.uiState.value
//        assertTrue(uiState.messages.none { it.id == testMessageId })
//    }
//
//    @Test
//    fun testAddOrUpdateReaction() = testDispatcher.runBlockingTest {
//        val testMessageId = "testMessageId"
//        val initialMessage = Message(id = testMessageId, imageUrl = "", caption = "", timestamp = 0, status = MessageStatus.SENT, reactions = listOf(), senderId = "", recipientId = "")
//        val updatedReaction = Reaction(userName = "user1", emoji = "👍")
//
//        // Setup initial state
//        chatViewModel._uiState.value = MessengerUiState(messages = listOf(initialMessage))
//
//        chatViewModel.addOrUpdateReaction(testMessageId, updatedReaction)
//
//        advanceUntilIdle()
//
//        val uiState = chatViewModel.uiState.value
//        val updatedMessage = uiState.messages.find { it.id == testMessageId }
//        assertTrue(updatedMessage?.reactions?.contains(updatedReaction) == true)
//    }
//
//    @Test
//    fun testRemoveReaction() = testDispatcher.runBlockingTest {
//        val testMessageId = "testMessageId"
//        val initialReaction = Reaction(userName = "user1", emoji = "👍")
//        val initialMessage = Message(id = testMessageId, imageUrl = "", caption = "", timestamp = 0, status = MessageStatus.SENT, reactions = listOf(initialReaction), senderId = "", recipientId = "")
//
//        // Setup initial state
//        chatViewModel._uiState.value = MessengerUiState(messages = listOf(initialMessage))
//
//        chatViewModel.removeReaction(testMessageId, "user1")
//
//        advanceUntilIdle()
//
//        val uiState = chatViewModel.uiState.value
//        val updatedMessage = uiState.messages.find { it.id == testMessageId }
//        assertTrue(updatedMessage?.reactions?.none { it.userName == "user1" } == true)
//    }
//
//    @Test
//    fun testUpdateMessageCaption() = testDispatcher.runBlockingTest {
//        val testMessageId = "testMessageId"
//        val initialMessage = Message(id = testMessageId, imageUrl = "", caption = "Old Caption", timestamp = 0, status = MessageStatus.SENT, reactions = listOf(), senderId = "", recipientId = "")
//        val updatedCaption = "New Caption"
//        val updatedMessage = initialMessage.copy(caption = updatedCaption)
//
//        // Setup initial state
//        chatViewModel._uiState.value = MessengerUiState(messages = listOf(initialMessage))
//
//        // Mock repository
//        coEvery { mockRepository.updateMessage(any()) } just Runs
//
//        chatViewModel.updateMessageCaption(initialMessage, updatedCaption)
//
//        advanceUntilIdle()
//
//        val uiState = chatViewModel.uiState.value
//        assertEquals(updatedCaption, uiState.messages.find { it.id == testMessageId }?.caption)
//    }
//
//    @Test
//    fun testGetUserNameById() {
//        val testUserId = "userId"
//        val expectedUserName = "Test User"
//        every { mockRepository.getUserNameById(testUserId) } returns expectedUserName
//
//        val userName = chatViewModel.getUserNameById(testUserId)
//
//        assertEquals(expectedUserName, userName)
//    }
//
//    @Test
//    fun testGetLastMessageId() = testDispatcher.runBlockingTest {
//        val testLastMessageId = "lastMessageId"
//        val result = Result.success(testLastMessageId)
//        every { mockRepository.getLastMessageId() } returns result
//
//        val lastMessageId = chatViewModel.getLastMessageId()
//
//        assertEquals(testLastMessageId, lastMessageId)
//    }
//}
