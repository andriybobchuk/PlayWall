package com.andriybobchuk.messenger

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ChatViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var chatViewModel: ChatViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        chatViewModel = ChatViewModel()
    }

    @Test
    fun testLoadMessages() = testDispatcher.runBlockingTest {
        chatViewModel.loadMessages()

        val uiState = chatViewModel.uiState.value
        assertEquals(5, uiState.messages.size) // Assuming PAGE_SIZE = 5
    }

    @Test
    fun testSendImage() = testDispatcher.runBlockingTest {
        val testUri = Uri.parse("https://test.com/image.jpg")
        val testCaption = "Test Image"

        chatViewModel.sendImage(testUri, testCaption)

        val uiState = chatViewModel.uiState.value
        assertEquals(testUri.toString(), uiState.messages[0].imageUrl)
        assertEquals(testCaption, uiState.messages[0].caption)
    }

    // Add more tests for other functions...
}
