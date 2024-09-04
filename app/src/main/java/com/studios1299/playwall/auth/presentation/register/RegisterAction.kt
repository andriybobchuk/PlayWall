package com.studios1299.playwall.auth.presentation.register

sealed interface RegisterAction {
    data object OnTogglePasswordVisibilityClick: RegisterAction
    data object OnLoginClick: RegisterAction
    data object OnRegisterClick: RegisterAction
    data object OnToggleTermsAcceptance : RegisterAction
    data object OnTermsOfServiceClick : RegisterAction
    data object OnPrivacyPolicyClick : RegisterAction
}