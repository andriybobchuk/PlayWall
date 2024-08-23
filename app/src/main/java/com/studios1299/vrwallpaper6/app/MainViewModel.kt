package com.studios1299.vrwallpaper6.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.vrwallpaper6.feature.auth.domain.AuthRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository = MyApp.appModule.authRepository
): ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(isCheckingAuth = true)
            state = state.copy(
                isLoggedIn = authRepository.getCurrentUserId() != null
            )
            state = state.copy(isCheckingAuth = false)
        }
    }
}