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
import com.studios1299.playwall.auth.presentation.getScreenRatio
import com.studios1299.playwall.core.domain.error_handling.SmartResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
class RegisterViewModel(
    private val repository: AuthRepository,
    private val userDataValidator: UserDataValidator
): ViewModel() {

    var state by mutableStateOf(RegisterState())
        private set

    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        state.email.textAsFlow()
            .onEach { email ->
                val isValidEmail = userDataValidator.isValidEmail(email.toString())
                state = state.copy(
                    isEmailValid = isValidEmail,
                    canRegister = isValidEmail && state.passwordValidationState.isValidPassword
                            && !state.isRegistering && state.isTermsAccepted
                )
            }
            .launchIn(viewModelScope)

        state.password.textAsFlow()
            .onEach { password ->
                val passwordValidationState = userDataValidator.validatePassword(password.toString())
                state = state.copy(
                    passwordValidationState = passwordValidationState,
                    canRegister = state.isEmailValid && passwordValidationState.isValidPassword
                            && !state.isRegistering && state.isTermsAccepted
                )
            }
            .launchIn(viewModelScope)

        snapshotFlow { state.isTermsAccepted }
            .onEach { isTermsAccepted ->
                state = state.copy(
                    canRegister = state.isEmailValid && state.passwordValidationState.isValidPassword
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
            val result = repository.register(
                email = state.email.text.toString().trim(),
                password = state.password.text.toString(),
                screenRatio = getScreenRatio(context)
            )
            state = state.copy(isRegistering = false)

            when(result) {
                is SmartResult.Error -> {
                    if(result.error == DataError.Network.CONFLICT) {
                        eventChannel.send(
                            RegisterEvent.Error(
                                UiText.StringResource(R.string.error_email_exists)
                            )
                        )
                    } else {
                        eventChannel.send(RegisterEvent.Error(result.error.asUiText()))
                    }
                }
                is SmartResult.Success -> {
                    loginAfterRegister()
                }
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
                    if(result.error == DataError.Network.UNAUTHORIZED) {
                        eventChannel.send(
                            RegisterEvent.Error(
                                UiText.StringResource(R.string.error_email_password_incorrect)
                            )
                        )
                    } else {
                        eventChannel.send(RegisterEvent.Error(result.error.asUiText()))
                    }
                }
                is SmartResult.Success -> {
                    eventChannel.send(RegisterEvent.RegistrationSuccess)
                }
            }
        }
    }

    fun googleRegister(credential: AuthCredential, context: Context) {
        viewModelScope.launch {
            state = state.copy(isRegistering = true)
            val result = repository.googleRegister(credential, getScreenRatio(context))
            state = state.copy(isRegistering = false)

            when(result) {
                is SmartResult.Error -> {
                    if(result.error == DataError.Network.UNAUTHORIZED) {
                        eventChannel.send(
                            RegisterEvent.Error(
                                UiText.StringResource(R.string.error_email_password_incorrect)
                            )
                        )
                    } else {
                        eventChannel.send(RegisterEvent.Error(result.error.asUiText()))
                    }
                }
                is SmartResult.Success -> {
                    eventChannel.send(RegisterEvent.RegistrationSuccess)
                }
            }
        }
    }
}