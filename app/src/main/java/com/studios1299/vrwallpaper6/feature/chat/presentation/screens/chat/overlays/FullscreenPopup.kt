package com.studios1299.vrwallpaper6.feature.chat.presentation.screens.chat.overlays

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun FullscreenPopup(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Popup(
        onDismissRequest = {
            onDismiss
        },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier.fillMaxSize(),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    color = Color(0x007D7D7D),
                    size = size,
                )
            }
            content()
        }
    }
}