package com.studios1299.playwall.core.presentation.wrzutomat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studios1299.playwall.R
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_MAGENTA
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_PURPLE
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_WHITE

data class SubscriptionOption(
    val name: String,
    val price: Double,
    val currency: String
)

@Composable
fun Background(content: @Composable () -> Unit) {
    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Image(
                painter = painterResource(id = R.drawable.primary_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            content()
        }
    }
}

@Composable
fun PoliciesButtons(
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Privacy Policy",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.clickable { onPrivacyPolicyClick() }
        )
        Text(
            text = "Terms of Service",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.clickable { onTermsOfServiceClick() }
        )
    }
}

@Composable
fun CartoonStyledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .padding(8.dp)
            .height(70.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .drawBehind {
                val cornerRadius = 24.dp.toPx()
                val buttonColor = ZEDGE_PURPLE
                val shadowColor = ZEDGE_MAGENTA

                drawRoundRect(
                    color = shadowColor,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    size = size.copy(height = size.height + 7.dp.toPx()),
                    topLeft = Offset(0f, 6.dp.toPx())
                )
                drawRoundRect(
                    color = buttonColor,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = ZEDGE_WHITE,
            fontSize = (21.sp * animatedScale),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun Logo(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.pw),
            contentDescription = null,
            modifier = Modifier.size(160.dp)
        )
    }
}
