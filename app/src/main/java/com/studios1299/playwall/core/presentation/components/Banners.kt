package com.studios1299.playwall.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

object Banners {

    @Composable
    fun Standard(
        text: String,
        backgroundColor: Color = Color.Gray,
        contentColor: Color = Color.White,
        button1Text: String? = null,
        button2Text: String? = null,
        button1OnClick: (() -> Unit)? = null,
        button2OnClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (button1Text == null && button2Text == null) Arrangement.Center else Arrangement.SpaceBetween // Center if no buttons
            ) {
                // Main banner text
                Text(
                    text = text,
                    color = contentColor,
                    modifier = Modifier.weight(1f),
                    textAlign = if (button1Text == null && button2Text == null) TextAlign.Center else TextAlign.Start // Align text center if no buttons
                )

                // Optional action buttons (only add if not null)
                if (button1Text != null) {
                    Button(
                        onClick = { button1OnClick?.invoke() },
                        colors = ButtonDefaults.buttonColors(contentColor = contentColor)
                    ) {
                        Text(button1Text, color = backgroundColor)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (button2Text != null) {
                    Button(
                        onClick = { button2OnClick?.invoke() },
                        colors = ButtonDefaults.buttonColors(contentColor = contentColor)
                    ) {
                        Text(button2Text, color = backgroundColor)
                    }
                }
            }
        }
    }


    @Composable
    fun OfflineStatus(
        modifier: Modifier = Modifier
    ) {
        Standard(
            modifier = modifier,
            text = "Offline, functionality restricted",
            backgroundColor = Color.Red,
            contentColor = Color.White,
            button1Text = null,  // No buttons
            button2Text = null,  // No buttons
        )
    }
}
