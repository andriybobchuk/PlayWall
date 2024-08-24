package com.studios1299.vrwallpaper6.auth.presentation.register

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import com.studios1299.vrwallpaper6.auth.domain.PasswordValidationState

data class RegisterState @OptIn(ExperimentalFoundationApi::class) constructor(
    val email: TextFieldState = TextFieldState(),
    val isEmailValid: Boolean = false,
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val passwordValidationState: com.studios1299.vrwallpaper6.auth.domain.PasswordValidationState = com.studios1299.vrwallpaper6.auth.domain.PasswordValidationState(),
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false
)