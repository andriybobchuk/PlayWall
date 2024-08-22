package com.studios1299.vrwallpaper6.feature.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import com.studios1299.vrwallpaper6.feature.auth.domain.PasswordValidationState

data class RegisterState constructor(
    val email: TextFieldState = TextFieldState(),
    val isEmailValid: Boolean = false,
    val password: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val passwordValidationState: PasswordValidationState = PasswordValidationState(),
    val isRegistering: Boolean = false,
    val canRegister: Boolean = passwordValidationState.isValidPassword && !isRegistering
)