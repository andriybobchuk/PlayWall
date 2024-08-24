package com.studios1299.playwall.auth.data

import com.studios1299.playwall.auth.domain.PasswordValidationState
import com.studios1299.playwall.auth.domain.PatternValidator

class UserDataValidator(
    private val patternValidator: PatternValidator
) {

    fun isValidEmail(email: String): Boolean {
        return patternValidator.matches(email.trim())
    }

    fun validatePassword(password: String): PasswordValidationState {
        val hasMinLength = password.length >= MIN_PASSWORD_LENGTH
        val hasDigit = password.any { it.isDigit() }
        val hasLowerCaseCharacter = password.any { it.isLowerCase() }
        val hasUpperCaseCharacter = password.any { it.isUpperCase() }

        return PasswordValidationState(true, true, true, true)
//        return PasswordValidationState(
//            hasMinLength = hasMinLength,
//            hasNumber = hasDigit,
//            hasLowerCaseCharacter = hasLowerCaseCharacter,
//            hasUpperCaseCharacter = hasUpperCaseCharacter
//        )
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 9
    }
}