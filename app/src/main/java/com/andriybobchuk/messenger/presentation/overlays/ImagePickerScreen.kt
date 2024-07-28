package com.andriybobchuk.messenger.presentation.overlays

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImagePickerScreen(
    imageUri: Uri?,
    caption: String,
    onSendClick: (Uri?, String) -> Unit,
    onDismiss: () -> Unit,
    onCaptionChange: (String) -> Unit
) {
    var captionState by remember { mutableStateOf(caption) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
    ) {
        imageUri?.let {
            GlideImage(
                model = it,
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = captionState,
                onValueChange = { newCaption ->
                    captionState = newCaption
                    onCaptionChange(newCaption)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray, shape = RoundedCornerShape(20.dp))
                    .padding(12.dp),
                textStyle = TextStyle(color = Color.White),
                decorationBox = { innerTextField ->
                    Box(
                    ) {
                        if (captionState.isEmpty()) {
                            Text(
                                text = "Add a caption...",
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { onSendClick(imageUri, captionState) }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}