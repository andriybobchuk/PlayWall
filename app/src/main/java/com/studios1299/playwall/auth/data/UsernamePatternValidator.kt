package com.studios1299.playwall.auth.data

import com.studios1299.playwall.auth.domain.PatternValidator

object UsernamePatternValidator: PatternValidator {
    private val usernamePattern = "^[a-zA-Z0-9_\\-]{5,}$".toRegex()

    override fun matches(value: String): Boolean {
        return usernamePattern.matches(value)
    }
}

