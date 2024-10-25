package com.studios1299.playwall.auth.presentation.login

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.R
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.core.presentation.asUiText
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.data.UserDataValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.AuthCredential
import com.studios1299.playwall.auth.presentation.getScreenRatio
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.domain.error_handling.SmartResult

@OptIn(ExperimentalFoundationApi::class)
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userDataValidator: UserDataValidator
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                state = state.copy(isOnline = online)
            }
        }
        combine(state.email.textAsFlow(), state.password.textAsFlow()) { email, password ->
            state = state.copy(
                canLogin = userDataValidator.isValidEmail(
                    email = email.toString().trim()
                ) && password.isNotEmpty()
            )
        }.launchIn(viewModelScope)
    }

    fun onAction(action: LoginAction) {
        when(action) {
            LoginAction.OnLoginClick -> login()
            LoginAction.OnTogglePasswordVisibility -> {
                state = state.copy(
                    isPasswordVisible = !state.isPasswordVisible
                )
            }
            LoginAction.OnPasswordResetClick -> sendPasswordResetEmail()
            else -> Unit
        }
    }

    private fun login() {
        viewModelScope.launch {
            state = state.copy(isLoggingIn = true)
            val result = authRepository.login(
                email = state.email.text.toString().trim(),
                password = state.password.text.toString()
            )
            state = state.copy(isLoggingIn = false)

            when(result) {
                is SmartResult.Error -> {
                    eventChannel.send(
                        LoginEvent.Error(
                            UiText.DynamicString(result.errorBody?:"Error")
                        )
                    )
                }
                is SmartResult.Success -> {
                    eventChannel.send(LoginEvent.LoginSuccess)
                }
            }
        }
    }

    fun googleLogin(credential: AuthCredential) {
        viewModelScope.launch {
            state = state.copy(isLoggingIn = true)
            val result = authRepository.googleLogin(credential)
            state = state.copy(isLoggingIn = false)

            when(result) {
                is SmartResult.Error -> {
                    eventChannel.send(
                        LoginEvent.Error(
                            UiText.DynamicString(result.errorBody?:"Error")
                        )
                    )
                }
                is SmartResult.Success -> {
                    eventChannel.send(LoginEvent.LoginSuccess)
                }
            }
        }
    }

    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(state.email.text.toString().trim())
            when (result) {
                is SmartResult.Success -> {
                    eventChannel.send(LoginEvent.PasswordResetEmailSent)
                }
                is SmartResult.Error -> {
                    eventChannel.send(
                        LoginEvent.Error(
                            UiText.DynamicString(result.errorBody?:"Error")
                        )
                    )
                }
            }
        }
    }

}

