package com.andriybobchuk.messenger.core.domain.error_handling

enum class PasswordError: Error {
    TOO_SHORT,
    NO_UPPERCASE,
    NO_DIGIT
}