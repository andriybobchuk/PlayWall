package com.andriybobchuk.messenger.presentation.overlays

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.andriybobchuk.messenger.ui.theme.LightGrey

@Composable
fun FullscreenPopup(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    topBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    Popup(
        onDismissRequest = onDismiss,
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
                    color = Color(0xCC7D7D7D),
                    size = size,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(color = LightGrey)
                    .align(Alignment.TopCenter)
            ) {
                content()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(color = LightGrey)
                    .align(Alignment.Center)
            ) {
                content()
            }
        }
    }
}