package com.andriybobchuk.messenger.presentation.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullscreenImageViewer(
    imageUrl: String,
    caption: String,
    sender: String,
    dateTime: String,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val topBarVisible = remember { mutableStateOf(true) }
    val alpha = remember { Animatable(1f) }
    val dragAmount = remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha.value))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        topBarVisible.value = false
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            offsetY.animateTo(0f)
                            alpha.animateTo(1f)
                        }
                        topBarVisible.value = true
                    }
                ) { change, dragAmountDelta ->
                    dragAmount.value += dragAmountDelta
                    coroutineScope.launch {
                        offsetY.snapTo(offsetY.value + dragAmountDelta)
                        val clampedAlpha = (1 - abs(offsetY.value) / (500 * 3)).coerceIn(0f, 1f)
                        alpha.snapTo(clampedAlpha)
                        if (abs(dragAmount.value) > 500f) {
                            onDismiss()
                        }
                    }
                }
            }
    ) {
        GlideImage(
            model = imageUrl,
            contentDescription = "Fullscreen Image",
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            topBarVisible.value = !topBarVisible.value
                        }
                    )
                },
            contentScale = ContentScale.Fit
        )

        AnimatedVisibility(
            visible = topBarVisible.value,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(text = sender, style = MaterialTheme.typography.titleMedium)
                        Text(text = dateTime, style = MaterialTheme.typography.bodyMedium.copy(Color.Gray))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = "Download",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }

        AnimatedVisibility(
            visible = topBarVisible.value,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = caption,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}



