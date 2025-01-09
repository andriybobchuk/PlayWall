package com.studios1299.playwall.core.presentation.wrzutomat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class Feature(
    val emoji: String,
    val label: String
)
@Composable
fun SubscriptionWeeklyScreen(
    subscriptionDetails: SubscriptionOption,
    onSubscribe: (SubscriptionOption) -> Unit,
    onClose: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit
) {
    val features = listOf(
        Feature("\uD83D\uDCE3", "No ads"),
        Feature("\uD83D\uDE08", "Unlimited devils"),
        Feature("\uD83D\uDC51", "Premium wallpapers"),
    )

    // Track visibility for each feature
    val featureVisibility = remember { mutableStateOf(List(features.size) { false }) }
    val captionVisible = remember { mutableStateOf(false) }

    // Animate the features one by one
    LaunchedEffect(Unit) {
        features.forEachIndexed { index, _ ->
            delay((index + 1) * 150L)  // Delay for each feature to appear sequentially
            featureVisibility.value = featureVisibility.value.toMutableList().apply { this[index] = true }
        }
        delay(800L)  // Wait before showing the caption
        captionVisible.value = true
    }

    Background {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Logo()
            }

            Column(modifier = Modifier
                .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier
                       // .align(Alignment.Center)
                        .height(250.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(48.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 32.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(36.dp))
                            .align(Alignment.Start)
                    ) {
                        // Animate each feature item
                        features.forEachIndexed { index, feature ->
                            AnimatedVisibility(
                                visible = featureVisibility.value[index],
                                enter = fadeIn(animationSpec = tween(durationMillis = 350)) + slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(durationMillis = 350)
                                )
                            ) {
                                Row(modifier = Modifier.align(Alignment.Start)) {
                                    Text(
                                        text = feature.emoji,
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(28.dp))
                                    Text(
                                        text = feature.label,
                                        color = Color.White.copy(0.8f),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animate the caption last
                    AnimatedVisibility(
                        visible = captionVisible.value,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(durationMillis = 300)
                        )
                    ) {
                        Text(
                            text = "This is a cool caption that can explain some stuff",
                            color = Color.White,
                            fontSize = 15.5.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    text = buildAnnotatedString {
                        append("FREE FOR 3 DAYS\nLATER ")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append("${subscriptionDetails.price} ${subscriptionDetails.currency}")
                        }
                        append(" WEEKLY")
                    },
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    text = "Cancel anytime",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }


            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                CartoonStyledButton(
                    text = "SUBSCRIBE",
                    onClick = { onSubscribe(subscriptionDetails) },
                )
                Spacer(modifier = Modifier.height(10.dp))
                PoliciesButtons(
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                    onTermsOfServiceClick = onTermsOfServiceClick
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}
