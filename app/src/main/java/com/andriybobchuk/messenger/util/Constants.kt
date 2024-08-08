package com.andriybobchuk.messenger.util

import androidx.compose.ui.graphics.Color


object Constants {

    // Message bubble dimensions:
    const val MESSAGE_CORNER_RADIUS = 20
    const val HORIZONTAL_SCREEN_PERCENTAGE = 0.65f
    const val VERTICAL_SCREEN_PERCENTAGE = 0.35f

    // Reactions
    val EMOJI_LIST = listOf("❤️", "😂", "😍", "😢", "👍", "👎")

    // Fullscreen ImageViewer
    const val DRAG_DISMISS_THRESHOLD = 500f
    const val BACKGROUND_ALPHA = 1f
    const val DRAG_ALPHA_FACTOR = 3
    val TOP_BAR_BACKGROUND_COLOR = Color.Black.copy(alpha = 0.5f)
    val BACKGROUND_COLOR = Color.Black
}