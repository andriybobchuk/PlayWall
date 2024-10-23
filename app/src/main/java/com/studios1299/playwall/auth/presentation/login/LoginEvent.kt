package com.studios1299.playwall.auth.presentation.login

import com.studios1299.playwall.core.presentation.UiText

sealed interface LoginEvent {
    data class Error(val error: UiText): LoginEvent
    data object LoginSuccess: LoginEvent
    data object PasswordResetEmailSent: LoginEvent
}