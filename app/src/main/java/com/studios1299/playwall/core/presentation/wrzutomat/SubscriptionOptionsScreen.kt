package com.studios1299.playwall.core.presentation.wrzutomat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_WHITE

@Composable
fun SubscriptionOptionsScreen(
    subscriptionOptions: List<SubscriptionOption>,
    onSubscribe: (SubscriptionOption) -> Unit,
    onClose: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(subscriptionOptions[0]) }

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

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(48.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 32.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.clip(RoundedCornerShape(36.dp))
                    ) {
                        subscriptionOptions.forEachIndexed { index, option ->
                            val shape = when (index) {
                                0 -> RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                                subscriptionOptions.size - 1 -> RoundedCornerShape(
                                    bottomStart = 36.dp,
                                    bottomEnd = 36.dp)
                                else -> RoundedCornerShape(0.dp)
                            }

                            val isSelected = option == selectedOption
                            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                            val borderColor = if (isSelected) ZEDGE_WHITE else ZEDGE_WHITE.copy(alpha = 0.2f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor, shape = shape)
                                    .border(BorderStroke(1.dp, borderColor), shape = shape)
                                    .clickable { selectedOption = option }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.name,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = buildAnnotatedString {
                            if (selectedOption.name == "WEEKLY") {
                                append("FREE FOR 3 DAYS\nLATER ")
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                    append("${selectedOption.price} ${selectedOption.currency}")
                                }
                                append(" WEEKLY")
                            } else {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                    append("${selectedOption.price} ${selectedOption.currency} ")
                                }
                                append(selectedOption.name)
                            }
                        },
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Cancel anytime",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    CartoonStyledButton(
                        text = "SUBSCRIBE",
                        onClick = { onSubscribe(selectedOption) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PoliciesButtons(
                        onPrivacyPolicyClick = onPrivacyPolicyClick,
                        onTermsOfServiceClick = onTermsOfServiceClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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

