package com.studios1299.playwall.auth.presentation.register

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.studios1299.playwall.R
import com.studios1299.playwall.core.domain.error_handling.DataError
import com.studios1299.playwall.core.presentation.UiText
import com.studios1299.playwall.core.presentation.asUiText
import com.studios1299.playwall.auth.domain.AuthRepository
import com.studios1299.playwall.auth.data.UserDataValidator
import com.studios1299.playwall.auth.data.UsernamePatternValidator
import com.studios1299.playwall.auth.presentation.getScreenRatio
import com.studios1299.playwall.auth.presentation.login.LoginEvent
import com.studios1299.playwall.core.data.networking.NetworkMonitor
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
class RegisterViewModel(
    private val repository: AuthRepository,
    private val emailDataValidator: UserDataValidator,
): ViewModel() {

    val usernameValidator = UserDataValidator(UsernamePatternValidator)

    var state by mutableStateOf(RegisterState())
        private set

    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                state = state.copy(isOnline = online)
            }
        }
        state.email.textAsFlow()
            .onEach { email ->
                val isValidEmail = emailDataValidator.isValidEmail(email.toString())
                state = state.copy(
                    isEmailValid = isValidEmail,
                    canRegister = isValidEmail && state.isUsernameValid && state.passwordValidationState.isValidPassword
                            && !state.isRegistering && state.isTermsAccepted
                )
            }
            .launchIn(viewModelScope)

        state.username.textAsFlow()
            .onEach { username ->
                val isValidUsername = usernameValidator.isValidUsername(username.toString())
                state = state.copy(
                    isUsernameValid = isValidUsername,
                    canRegister = isValidUsername && state.isEmailValid && state.passwordValidationState.isValidPassword
                            && !state.isRegistering && state.isTermsAccepted
                )
            }
            .launchIn(viewModelScope)

        state.password.textAsFlow()
            .onEach { password ->
                val passwordValidationState = emailDataValidator.validatePassword(password.toString())
                state = state.copy(
                    passwordValidationState = passwordValidationState,
                    canRegister = state.isEmailValid && state.isUsernameValid && passwordValidationState.isValidPassword
                            && !state.isRegistering && state.isTermsAccepted
                )
            }
            .launchIn(viewModelScope)

        snapshotFlow { state.isTermsAccepted }
            .onEach { isTermsAccepted ->
                state = state.copy(
                    canRegister = state.isEmailValid && state.isUsernameValid && state.passwordValidationState.isValidPassword
                            && !state.isRegistering && isTermsAccepted
                )
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: RegisterAction) {
        when(action) {
            is RegisterAction.OnRegisterClick -> register(action.context)
            RegisterAction.OnTogglePasswordVisibilityClick -> {
                state = state.copy(
                    isPasswordVisible = !state.isPasswordVisible
                )
            }
            RegisterAction.OnToggleTermsAcceptance -> {
                state = state.copy(isTermsAccepted = !state.isTermsAccepted)
            }
            else -> Unit
        }
    }

    private fun register(context: Context) {
        viewModelScope.launch {
            state = state.copy(isRegistering = true)

            val healthCheck = repository.checkHealth()
            if (healthCheck is SmartResult.Success) {
                val result = repository.register(
                    email = state.email.text.toString().trim(),
                    username = state.username.text.toString().trim(),
                    password = state.password.text.toString(),
                    screenRatio = getScreenRatio(context)
                )

                when(result) {
                    is SmartResult.Error -> {
                        Log.e("RegisterViewModel", "register(), error: $result")
                        eventChannel.send(
                            RegisterEvent.Error(
                                UiText.DynamicString(result.errorBody?:result.message?:"Error")
                            )
                        )
                    }
                    is SmartResult.Success -> {
                        Log.e("RegisterViewModel", "register(), success: $result")
                        loginAfterRegister()
                    }
                }
                state = state.copy(isRegistering = false)
            } else if (healthCheck is SmartResult.Error){
                eventChannel.send(
                    RegisterEvent.Error(
                        UiText.DynamicString(healthCheck.errorBody?:healthCheck.message?:"Server is down, try later")
                    )
                )
                state = state.copy(isRegistering = false)
            }
        }
    }

    private fun loginAfterRegister() {
        viewModelScope.launch {
            state = state.copy(isRegistering = true)
            val result = repository.login(
                email = state.email.text.toString().trim(),
                password = state.password.text.toString()
            )
            state = state.copy(isRegistering = false)

            when(result) {
                is SmartResult.Error -> {
                    Log.e("RegisterViewModel", "loginAfterRegister(), error: $result")
                    eventChannel.send(
                        RegisterEvent.Error(
                            UiText.DynamicString(result.errorBody?:"Error")
                        )
                    )
                }
                is SmartResult.Success -> {
                    Log.e("RegisterViewModel", "loginAfterRegister(), success: $result")
                    eventChannel.send(RegisterEvent.RegistrationSuccess)
                }
            }
        }
    }

    fun googleRegister(credential: AuthCredential, context: Context) {
        viewModelScope.launch {
            state = state.copy(isRegistering = true)
            val result = repository.googleRegister(
                credential = credential,
                screenRatio = getScreenRatio(context))
            state = state.copy(isRegistering = false)

            when(result) {
                is SmartResult.Error -> {
                    eventChannel.send(
                        RegisterEvent.Error(
                            UiText.DynamicString(result.errorBody?:"Error")
                        )
                    )
                }
                is SmartResult.Success -> {
                    eventChannel.send(RegisterEvent.RegistrationSuccess)
                }
            }
        }
    }
}