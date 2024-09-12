package com.studios1299.playwall.create.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun DrawingToolbar(
    onChooseImage: () -> Unit,
    onAddText: () -> Unit,
    onAddSticker: () -> Unit,
    onDraw: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = onChooseImage
            ) {
                Icon(Icons.Outlined.Image, contentDescription = "Choose Image")
            }
            IconButton(
                onClick = onAddText,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.TextFields, contentDescription = "Add Text")
            }
            IconButton(
                onClick = onAddSticker,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.EmojiEmotions, contentDescription = "Add Sticker")
            }
            IconButton(
                onClick = onDraw,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.Brush, contentDescription = "Draw")
            }
            IconButton(
                onClick = onUndo,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.Undo, contentDescription = "Undo")
            }
            IconButton(
                onClick = onRedo,
                enabled = enabled
            ) {
                Icon(Icons.Outlined.Redo, contentDescription = "Redo")
            }
        }
    }
}