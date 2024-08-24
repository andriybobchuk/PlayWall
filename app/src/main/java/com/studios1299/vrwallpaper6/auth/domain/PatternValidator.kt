package com.studios1299.vrwallpaper6.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}