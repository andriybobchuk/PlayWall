package com.studios1299.playwall.core.presentation.wrzutomat

import android.app.Activity
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.BillingClient
import com.studios1299.playwall.app.config.AppConfigManager
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_WHITE
import com.studios1299.playwall.monetization.data.BillingManager


enum class priceOption {
    WEEKLY,
    MONTHLY,
    YEARLY
}

@Composable
fun SubscriptionOptionsScreen(
//    subscriptionOptions: List<SubscriptionOption>,
//    onSubscribe: (SubscriptionOption) -> Unit,
    onClose: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit,
    billingManager: BillingManager
) {
    var selectedOption by remember { mutableStateOf(priceOption.WEEKLY) }

    val priceData = billingManager.priceData.collectAsState()

    val useV2WeeklySubscription = AppConfigManager.useV2WeeklySubscription



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
                        priceOption.entries.forEachIndexed { index, option ->
                            val shape = when (index) {
                                0 -> RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                                priceOption.entries.size - 1 -> RoundedCornerShape(
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
                            when (selectedOption) {
                                priceOption.WEEKLY -> {
                                    append("FREE FOR 3 DAYS\nLATER ")
                                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                        val weeklyPrice = if (useV2WeeklySubscription)
                                            priceData.value.weeklyWithTrialV2.price
                                        else
                                            priceData.value.weeklyWithTrial.price

                                        val weeklyCurrency = if (useV2WeeklySubscription)
                                            priceData.value.weeklyWithTrialV2.currency
                                        else
                                            priceData.value.weeklyWithTrial.currency

                                        append("${weeklyPrice} ${weeklyCurrency}")
                                    }
                                    append(" WEEKLY")
                                }
                                priceOption.MONTHLY -> {
                                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                        append("${priceData.value.monthly.price} ${priceData.value.monthly.currency} ")
                                    }
                                    append(selectedOption.name)
                                }
                                priceOption.YEARLY -> {
                                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                        append("${priceData.value.yearly.price} ${priceData.value.yearly.currency} ")
                                    }
                                    append(selectedOption.name)
                                }
                            }
//
//
//                            if (selectedOption.name == "WEEKLY") {
//                                append("FREE FOR 3 DAYS\nLATER ")
//                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
//                                    append("${selectedOption.price} ${selectedOption.currency}")
//                                }
//                                append(" WEEKLY")
//                            } else {
//                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
//                                    append("${selectedOption.price} ${selectedOption.currency} ")
//                                }
//                                append(selectedOption.name)
//                            }
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
                    val context = LocalContext.current
                    CartoonStyledButton(
                        text = "SUBSCRIBE",
                        onClick = {
                            try {
                                val productId = when (selectedOption) {
                                    priceOption.WEEKLY -> {
                                        if (useV2WeeklySubscription) {
                                            "weekly_subscription_with_trial_v2"
                                        } else {
                                            "weekly_subscription_with_trial"
                                        }
                                    }
                                    priceOption.MONTHLY -> "subscription_monthly"
                                    priceOption.YEARLY -> "yearly_subscription"
                                }
                                billingManager.startPurchaseFlow(context as Activity, productId, BillingClient.ProductType.SUBS)

                            } catch (e: Exception) {
                                Log.e(LOG_TAG, "Subscribing failed: ${e.message}")
                                Toast.makeText(context, "Purchase Failed", Toast.LENGTH_LONG).show()
                            }

                                  },
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

