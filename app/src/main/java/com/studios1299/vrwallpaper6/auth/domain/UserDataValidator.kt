package com.studios1299.vrwallpaper6.auth.domain

class UserDataValidator(
    private val patternValidator: com.studios1299.vrwallpaper6.auth.domain.PatternValidator
) {

    fun isValidEmail(email: String): Boolean {
        return patternValidator.matches(email.trim())
    }

    fun validatePassword(password: String): com.studios1299.vrwallpaper6.auth.domain.PasswordValidationState {
        val hasMinLength = password.length >= com.studios1299.vrwallpaper6.auth.domain.UserDataValidator.Companion.MIN_PASSWORD_LENGTH
        val hasDigit = password.any { it.isDigit() }
        val hasLowerCaseCharacter = password.any { it.isLowerCase() }
        val hasUpperCaseCharacter = password.any { it.isUpperCase() }

        return com.studios1299.vrwallpaper6.auth.domain.PasswordValidationState(
            hasMinLength = hasMinLength,
            hasNumber = hasDigit,
            hasLowerCaseCharacter = hasLowerCaseCharacter,
            hasUpperCaseCharacter = hasUpperCaseCharacter
        )
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 9
    }
}