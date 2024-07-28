package com.andriybobchuk.messenger.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andriybobchuk.messenger.ui.theme.MyMessage

@Composable
fun EmojiPanel(onEmojiClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            //.fill()
            .clip(RoundedCornerShape(32.dp))
            .background(MyMessage)
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val emojis = listOf("❤️", "😂", "😍", "😢", "👍", "👎")
        emojis.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 18.sp,
                modifier = Modifier
                    .clickable { onEmojiClick(emoji) }
                    .padding(6.dp)
            )
        }
    }
}