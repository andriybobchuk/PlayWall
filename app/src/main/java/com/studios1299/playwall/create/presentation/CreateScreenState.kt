package com.studios1299.playwall.create.presentation

import android.net.Uri
import androidx.compose.ui.geometry.Offset

data class CreateScreenState(
    val selectedImageUri: String = "https://i.pinimg.com/736x/68/8d/d3/688dd325dbbdc238f4b70caffe77a5af.jpg",
    val addedTexts: List<TextItem> = emptyList(),
    val addedStickers: List<StickerItem> = emptyList(),
    val isDrawingModeEnabled: Boolean = false
)

data class TextItem(val text: String, val color: Int, val position: Offset)
data class StickerItem(val stickerUri: Uri, val position: Offset)
