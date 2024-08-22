package com.studios1299.vrwallpaper6.feature.auth.presentation.register

//import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class RegisterViewModel(
    //private val userDataValidator: UserDataValidator
): ViewModel() {

    var state by mutableStateOf(RegisterState())
        private set

    init {
//        state.email.textAsFlow()
//            .onEach { email ->
//                state = state.copy(
//                    isEmailValid = userDataValidator.isValidEmail(email.toString())
//                )
//            }
//            .launchIn(viewModelScope)
//
//        state.password.textAsFlow()
//            .onEach { password ->
//                state = state.copy(
//                    passwordValidationState = userDataValidator.validatePassword(password.toString())
//                )
//            }
//            .launchIn(viewModelScope)
    }

    fun onAction(action: RegisterAction) {

    }
}