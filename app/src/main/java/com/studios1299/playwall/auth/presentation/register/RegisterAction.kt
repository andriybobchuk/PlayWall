package com.studios1299.playwall.auth.presentation.register

import android.content.Context

sealed interface RegisterAction {
    data object OnTogglePasswordVisibilityClick: RegisterAction
    data object OnLoginClick: RegisterAction
    data class OnRegisterClick(val context: Context): RegisterAction
    data object OnToggleTermsAcceptance : RegisterAction
    data object OnTermsOfServiceClick : RegisterAction
    data object OnPrivacyPolicyClick : RegisterAction
}