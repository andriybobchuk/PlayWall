package com.andriybobchuk.messenger.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = GRAY800, // Black texts
    primaryContainer = GRAY200, // Background
    secondaryContainer = BLUE200, // their message
    secondary = GRAY600, // Grey texts
    background = Color.White, // Backgrounds
    outline = Color.LightGray // Shimmer for loading images
)

private val DarkColorScheme = darkColorScheme(
    primary = NAVY200, // Light text for primary elements
    primaryContainer = NAVY600, // Background
    secondaryContainer = NAVY500, // their message
    secondary = NAVY400, // Light text for secondary elements
    background = NAVY800, // Background
    outline = NAVY300 // for loading images backgrounds
)

//private val LightAndroidBackgroundTheme = BackgroundTheme(color = Color.White)
//private val DarkAndroidBackgroundTheme = BackgroundTheme(color = DARK_GREEN300)

@Composable
fun MessengerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoppinsTypography,
        content = content
    )
}