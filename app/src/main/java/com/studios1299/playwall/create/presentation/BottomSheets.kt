package com.studios1299.playwall.create.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.components.TextFields

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddTextBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onTextAdded: (inputText: String, textColor: Int) -> Unit,
    initialColor: Color? = null
) {
    val textFieldState = remember { TextFieldState("") }
    var selectedColor by remember { mutableStateOf(initialColor ?: Color.Blue) }
    val focusState = remember { mutableStateOf(false) }

    val availableColors = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Black
    )

    LaunchedEffect(focusState.value) {
        if (focusState.value) {
            sheetState.expand()
        } else {
            sheetState.partialExpand()
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            val selectedTextColor = android.graphics.Color.parseColor(
                "#" + Integer.toHexString(selectedColor.hashCode())
            )
            onTextAdded(textFieldState.text.toString(), selectedTextColor)
            onDismiss()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextFields.Primary(
                state = textFieldState,
                startIcon = null,
                endIcon = null,
                hint = stringResource(R.string.add_label),
                title = stringResource(R.string.add_label),
                modifier = Modifier.fillMaxWidth(),
                onFocusChanged = { isFocused -> focusState.value = isFocused }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.select_label_color))
            LazyRow(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(100f))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                items(availableColors) { color ->
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(100f))
                                .background(color)
                                .clickable { selectedColor = color }
                        )
                        if (color == selectedColor) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(100f))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
            if (focusState.value) {
                Spacer(modifier = Modifier.height(300.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerBottomSheet(
    stickers: List<Int> = listOf(
        R.drawable.heart,
        R.drawable.pw,
        R.drawable.heart,
        R.drawable.pw,
        R.drawable.heart,
        R.drawable.pw,
        R.drawable.heart,
        R.drawable.pw,
        R.drawable.heart,
        R.drawable.pw,
        R.drawable.heart,
        R.drawable.pw,
        R.drawable.heart,
        R.drawable.pw
    ),
    onDismiss: () -> Unit,
    onStickerSelected: (stickerResourceId: Int) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(stickers) { stickerResourceId ->
                Image(
                    painter = painterResource(id = stickerResourceId),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {
                            onStickerSelected(stickerResourceId)
                            onDismiss()
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawModeBottomSheet(
    availableColors: List<Color> = listOf(
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Black
    ),
    initialColor: Color? = null,
    initialBrushSize: Float? = null,
    onDismiss: () -> Unit,
    onDrawSettingsSelected: (selectedColor: Color, brushSize: Float) -> Unit
) {
    var selectedDrawColor by remember { mutableStateOf(initialColor ?: Color.Red) }
    var brushSize by remember { mutableStateOf(initialBrushSize ?: 10f) }

    ModalBottomSheet(
        onDismissRequest = {
            onDrawSettingsSelected(selectedDrawColor, brushSize)
            onDismiss()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.choose_brush_color))
            LazyRow(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(100f))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                items(availableColors) { color ->
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(100f))
                                .background(color)
                                .clickable { selectedDrawColor = color }
                        )
                        if (color == selectedDrawColor) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(100f))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
            Text("Brush Size: ${brushSize.toInt()}")
            Slider(
                value = brushSize,
                onValueChange = { brushSize = it },
                valueRange = 1f..50f, // Range for brush size
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}