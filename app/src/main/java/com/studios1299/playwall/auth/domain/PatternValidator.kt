package com.studios1299.playwall.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}