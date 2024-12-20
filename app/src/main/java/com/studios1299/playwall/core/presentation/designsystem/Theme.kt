package com.studios1299.playwall.core.presentation.designsystem

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

//private val LightColorScheme = lightColorScheme(
//    primary = GRAY800, // Black texts
//    primaryContainer = GRAY200, // Background
//    secondaryContainer = BLUE200, // their message
//    secondary = GRAY600, // Grey texts
//    background = Color.White, // Backgrounds
//    outline = Color.LightGray, // Shimmer for loading images
//    surface = Color.White// For light container elements like textfields backgrounds
//)

//private val DarkColorScheme = darkColorScheme(
//    primary = NAVY200, // Light text for primary elements
//    primaryContainer = NAVY600, // Background
//    secondaryContainer = NAVY500, // their message
//    secondary = NAVY400, // Light text for secondary elements
//    background = NAVY800, // Background
//    outline = NAVY300, // for loading images backgrounds
//    surface = Color(0x0a0a0a) // For light container elements like textfields backgrounds
//)

private val ZedgeScheme = darkColorScheme(
    primary = ZEDGE_PURPLE,
    onPrimary = ZEDGE_WHITE,

    secondary = ZEDGE_MAGENTA,

    background = ZEDGE_BLACK, // Background
    onBackground = ZEDGE_WHITE,


    primaryContainer = ZEDGE_DARK,
    onPrimaryContainer = ZEDGE_WHITE,


    secondaryContainer = ZEDGE_LIGHT_PURPLE, // their message
    onSecondaryContainer = ZEDGE_GRAY,
    //secondary = NAVY400, // Light text for secondary elements


    error = ZEDGE_MAGENTA,

    outline = NAVY300, // for loading images backgrounds
    //surface = Color(0x0a0a0a) // For light container elements like textfields backgrounds
    surface = ZEDGE_BLACK // For light container elements like textfields backgrounds
)

//private val LightAndroidBackgroundTheme = BackgroundTheme(color = Color.White)
//private val DarkAndroidBackgroundTheme = BackgroundTheme(color = DARK_GREEN300)

@Composable
fun PlayWallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
    val colorScheme = ZedgeScheme



    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoppinsTypography,
        content = content
    )
}