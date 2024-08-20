package com.andriybobchuk.messenger.feature.auth.domain

import com.andriybobchuk.messenger.core.domain.error_handling.Error
import com.andriybobchuk.messenger.core.domain.error_handling.PasswordError
import com.andriybobchuk.messenger.core.domain.error_handling.Result

class UserDataValidator {

    companion object {
        const val MIN_PASSWORD_LENGTH = 9
    }

    fun validatePassword(password: String): Result<Unit, PasswordError> {
        if(password.length < 9) {
            return Result.Error(PasswordError.TOO_SHORT)
        }

        val hasUppercaseChar = password.any { it.isUpperCase() }
        if(!hasUppercaseChar) {
            return Result.Error(PasswordError.NO_UPPERCASE)
        }

        val hasDigit = password.any { it.isDigit() }
        if(!hasDigit) {
            return Result.Error(PasswordError.NO_DIGIT)
        }

        return Result.Success(Unit)
    }

}