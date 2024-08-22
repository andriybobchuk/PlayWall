package com.studios1299.vrwallpaper6.core.domain.error_handling

enum class PasswordError: Error {
    TOO_SHORT,
    NO_UPPERCASE,
    NO_DIGIT
}