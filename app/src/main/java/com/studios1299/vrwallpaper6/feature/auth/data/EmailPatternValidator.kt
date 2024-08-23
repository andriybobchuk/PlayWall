package com.studios1299.vrwallpaper6.feature.auth.data

import android.util.Patterns
import com.studios1299.vrwallpaper6.feature.auth.domain.PatternValidator

object EmailPatternValidator: PatternValidator {

    override fun matches(value: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(value).matches()
    }
}