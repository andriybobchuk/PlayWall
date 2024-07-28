package com.andriybobchuk.messenger

import com.andriybobchuk.messenger.presentation.viewmodel.ChatViewModel
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.andriybobchuk.messenger.presentation.MessengerScreen
import com.andriybobchuk.messenger.ui.theme.MessengerTheme

class MainActivity : ComponentActivity() {

    private lateinit var chatViewModel: ChatViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        setContent {
            MessengerTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    MessengerScreen(
                        onBackClick = {
                            // Handle back navigation
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}