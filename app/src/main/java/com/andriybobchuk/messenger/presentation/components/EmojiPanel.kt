package com.andriybobchuk.messenger.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andriybobchuk.messenger.ui.theme.LightGrey

@Composable
fun EmojiPanel(
    selectedEmoji: String?,
    onEmojiClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(LightGrey)
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val emojis = listOf("❤️", "😂", "😍", "😢", "👍", "👎")
        emojis.forEach { emoji ->
            val isSelected = emoji == selectedEmoji
            Text(
                text = emoji,
                fontSize = 18.sp,
                color = if (isSelected) Color.Black else Color.Unspecified,
                modifier = Modifier
                    .background(
                        if (isSelected) Color.DarkGray.copy(alpha = 0.2f) else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onEmojiClick(emoji) }
                    .padding(6.dp)
            )
        }
    }
}