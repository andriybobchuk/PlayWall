package com.studios1299.playwall.monetization.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studios1299.playwall.R

@Composable
fun PlusIcon(modifier: Modifier = Modifier) {
    Image(
        imageVector = Icons.Default.Add,
        contentDescription = stringResource(id = R.string.app_name),
        modifier = modifier.height(14.dp)
    )
}

@Composable
fun DiamondsDisplay(diamondsCount: Int, isPremium: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\uD83D\uDE08 " + if (isPremium) "\u221e" else diamondsCount,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            //color = ColorCarbon
        )
        if (!isPremium) {
            Spacer(modifier = Modifier.width(2.dp))
            PlusIcon()
        }
    }
}