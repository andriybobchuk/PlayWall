package com.studios1299.playwall.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Deprecated("I think this is not that necessary as I am anyways handling it for all Android versions")
@Composable
fun KeyboardAware(content: @Composable () -> Unit) {
    Box(modifier = Modifier.imePadding()) {
        content()
    }
}