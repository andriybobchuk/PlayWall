package com.studios1299.playwall.auth.presentation.intro

sealed interface IntroAction {
    data object OnSignInClick: IntroAction
    data object OnSignUpClick: IntroAction
    data object onTermsClick: IntroAction
    data object onPrivacyClick: IntroAction
    data object onContentPolicyClick: IntroAction
}