package com.studios1299.vrwallpaper6.feature.auth.presentation.register

import com.studios1299.vrwallpaper6.core.presentation.UiText

sealed interface RegisterEvent {
    data object RegistrationSuccess: RegisterEvent
    data class Error(val error: UiText): RegisterEvent
}