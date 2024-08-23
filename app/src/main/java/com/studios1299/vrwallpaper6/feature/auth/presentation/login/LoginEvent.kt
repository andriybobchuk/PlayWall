package com.studios1299.vrwallpaper6.feature.auth.presentation.login

import com.studios1299.vrwallpaper6.core.presentation.UiText

sealed interface LoginEvent {
    data class Error(val error: UiText): LoginEvent
    data object LoginSuccess: LoginEvent
}