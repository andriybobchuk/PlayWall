package com.studios1299.playwall.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.core.domain.CoreRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val coreRepository: CoreRepository = MyApp.appModule.coreRepository
): ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(isCheckingAuth = true)
            state = state.copy(
                isLoggedIn = coreRepository.getCurrentUserId() != null
            )
            state = state.copy(isCheckingAuth = false)
        }
    }
}