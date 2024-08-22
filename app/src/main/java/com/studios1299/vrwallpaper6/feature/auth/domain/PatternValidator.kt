package com.studios1299.vrwallpaper6.feature.auth.domain

interface PatternValidator {
    fun matches(value: String): Boolean
}