package com.studios1299.playwall.core.domain.error_handling

enum class PasswordError: Error {
    TOO_SHORT,
    NO_UPPERCASE,
    NO_DIGIT
}