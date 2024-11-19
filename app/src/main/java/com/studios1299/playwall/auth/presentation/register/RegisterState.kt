package com.studios1299.playwall.auth.presentation.register

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.studios1299.playwall.auth.domain.PasswordValidationState

data class RegisterState @OptIn(ExperimentalFoundationApi::class) constructor(
    val email: TextFieldState = TextFieldState(),
    val isEmailValid: Boolean = false,
    val username: TextFieldState = TextFieldState(),
    val isUsernameValid: Boolean = false,
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val passwordValidationState: PasswordValidationState = PasswordValidationState(),
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val isOnline: Boolean = true
)