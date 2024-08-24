package com.studios1299.playwall.auth.presentation.register

import com.studios1299.playwall.core.presentation.UiText

sealed interface RegisterEvent {
    data object RegistrationSuccess: RegisterEvent
    data class Error(val error: UiText): RegisterEvent
}