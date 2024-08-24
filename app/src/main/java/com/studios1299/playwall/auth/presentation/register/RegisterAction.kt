package com.studios1299.playwall.auth.presentation.register

sealed interface RegisterAction {
    data object OnTogglePasswordVisibilityClick: RegisterAction
    data object OnLoginClick: RegisterAction
    data object OnRegisterClick: RegisterAction
}