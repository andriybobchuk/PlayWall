package com.studios1299.playwall.monetization.presentation.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.android.billingclient.api.BillingClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.studios1299.playwall.R
import com.studios1299.playwall.app.config.AppConfigManager
import com.studios1299.playwall.core.presentation.components.Toolbars
import com.studios1299.playwall.monetization.data.AdManager
import com.studios1299.playwall.monetization.data.BillingManager
import com.studios1299.playwall.monetization.presentation.AppState
import com.studios1299.playwall.monetization.presentation.DiamondsViewModel
import com.studios1299.playwall.monetization.presentation.components.NextDiamondSheet
import kotlinx.coroutines.launch

const val EVIL_EMOJI = "\uD83D\uDE08"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiamondsScreen(
    viewModel: DiamondsViewModel,
    onNavigateToLuckySpin: () -> Unit,
    onNavigateToPremiumPurchase: () -> Unit,
    onBackClick: () -> Unit,
    adManager: AdManager,
    onOpenWrzutomatDuzy: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val noAdsMessage = "No ads available"
    val analytics = FirebaseAnalytics.getInstance(context)
    val billingManager = BillingManager(context)

    val colorStops = arrayOf(
        //0.3f to Color(0xFF8A2BE2), // Purple, like BlueViolet
        0.4f to MaterialTheme.colorScheme.background, // Purple, like BlueViolet
        //1.0f to Color(0xFFFF00FF)  // Magenta
        1.0f to Color(0xFFFF00FF)  // Magenta
    )

    val spacing = 20.dp

    var isLoading by remember {
        mutableStateOf(false)
    }

    BackHandler(enabled = isLoading) {}

    LaunchedEffect(Unit) {
        analytics.logEvent("seen_diamonds_screen", null)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.diamonds_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
            //.background(Brush.verticalGradient(colorStops = colorStops))
        ) {
            //val diamondsCount = viewModel.diamondsCount.collectAsState()
            val diamondsCount = AppState.devilCount.collectAsState()
            val showNextDiamondSheet = AppState.nextDiamondSheetShow.collectAsState()

            DiamondsScreenTopBar(onBackClick, isLoading)
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 12.dp)

            ) {
                DiamondsCount(diamondsCount = diamondsCount.value)
                Spacer(modifier = Modifier.size(spacing))
                DailyCheckIn(viewModel)
                Spacer(modifier = Modifier.size(spacing))
                LuckySpinButton(onNavigateToLuckySpin)
                Spacer(modifier = Modifier.size(spacing))
                WatchVideoButton(isLoading) {
                    if (!isLoading) {
                        analytics.logEvent("ds_watch_ads_to_get_diamonds_button", null)
                        isLoading = true
                        adManager.loadRewardedAd(
                            onAdLoaded = { adLoaded ->
                                if (adLoaded) {
                                    adManager.showRewardedAdIfLoaded(
                                        onRewardEarned = {
                                            viewModel.addDevils(1)
                                        },
                                        onAdClosed = {
                                            // viewModel.showNextDiamondSheet()
                                            AppState.updateNextDiamondSheetShow(true)
                                        }
                                    )
                                    //viewModel.hideNextDiamondSheet()
                                    AppState.updateNextDiamondSheetShow(false)
                                } else {
                                    /*
                                   *
                                   * Case that ad is no loaded (no fill, etc)
                                   *
                                   * */
                                    //viewModel.hideNextDiamondSheet()
                                    AppState.updateNextDiamondSheetShow(false)
                                    scope.launch {
//                                    SnackbarController.sendEvent(
//                                        SnackbarEvent(
//                                            message = noAdsMessage,
//                                            duration = SnackbarDuration.Long
//                                        )
//                                    )
                                    }


                                }
                                isLoading = false
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.size(spacing))

                val showDialog = remember { mutableStateOf(false) }
                UnlimitedDiamondsButton(
                    onClick = {
                        showDialog.value = true
                    }
                )
                if (showDialog.value) {

                    onOpenWrzutomatDuzy()
//                    BuyDialog(
//                        billingManager = billingManager,
//                        onDismissRequest = { showDialog.value = false }
//                    )
                }
                Spacer(modifier = Modifier.size(spacing))
//            Button(onClick = {
//                viewModel.addDevils(-1)
//            }) {
//                Text(text = "Remove 1 diamond")
//            }
//            Button(onClick = {
//                viewModel.reset()
//            }) {
//                Text(text = "reset checkin")
//            }

                if (showNextDiamondSheet.value) {
                    NextDiamondSheet(
                        viewModel = viewModel,
                        adManager = adManager
                    )
                }

            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiamondsScreenTopBar(onBackClick: () -> Unit, isLoading: Boolean) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Toolbars.Primary(
        title = "Get Devils",
        showBackButton = true,
        onBackClick = {
            if (!isLoading) {
                onBackClick()
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun DiamondsCount(diamondsCount: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                //.clip(RoundedCornerShape(8.dp))
                //.background(DIAMONDS_SCREEN_PANEL.copy(alpha = 0.5f))
                .padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "$EVIL_EMOJI $diamondsCount",
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }

    }
}

@Composable
fun DailyCheckIn(
    viewModel: DiamondsViewModel
) {

//    val dailyCheckinData by viewModel.dailyCheckinState.collectAsState()
//    val hasCheckedInToday by viewModel.hasCheckedInToday.collectAsState()
//
    val dailyCheckinData = AppState.dailyCheckinState.collectAsState()
    val hasCheckedInToday = AppState.hasCheckedInToday.collectAsState()

    val listState = rememberLazyListState()
    val lastCheckedIndex = dailyCheckinData.value.indexOfLast { it.checked }

    LaunchedEffect(key1 = dailyCheckinData) {
        //TODO
        if (lastCheckedIndex != -1 && lastCheckedIndex != 1) {
            listState.animateScrollToItem(index = lastCheckedIndex)
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 20.dp)
    ) {
        Text(
            text = "Daily Check-In",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.size(20.dp))
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(dailyCheckinData.value) {
                DailyCheckInCard(it)
            }
        }

        // if(!hasCheckedInToday) {
        if (!hasCheckedInToday.value) {
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    viewModel.checkIn()
                }
                //viewModel::checkIn
            ) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "CHECK IN \uD83D\uDC4B",
                    fontSize = 20.sp
                )
            }
        }

    }
}

@Composable
fun LuckySpinButton(onLuckySpinClick: () -> Unit) {

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 8.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp,
            disabledElevation = 8.dp
        ),
        onClick = onLuckySpinClick
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = "LUCKY SPIN \uD83C\uDF40",
            fontSize = 17.sp
        )
    }
}

@Composable
fun UnlimitedDiamondsButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp),
        onClick = onClick,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 8.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp,
            disabledElevation = 8.dp
        ),
        //border = BorderStroke(width = 4.dp, color = ColorCarbon)
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = "GET UNLIMITED $EVIL_EMOJI",
            fontSize = 17.sp,
            //color = ColorCarbon,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WatchVideoButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp)
            .heightIn(min = 70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 8.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp,
            disabledElevation = 8.dp
        ),
        onClick = onClick
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 3.dp
            )
        } else {
            Image(
                imageVector = Icons.Default.AdUnits,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "WATCH ADS TO GET FREE $EVIL_EMOJI", color = Color.White, fontSize = 17.sp)
        }
    }
}


@Composable
fun DailyCheckInCard(
    data: DailyCheckinData
) {
    val bgColor =
        if (data.checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val textColor =
        if (data.checked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = bgColor)
            .border(
                2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(vertical = 4.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = data.label,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(text = "$EVIL_EMOJI", fontSize = 20.sp)
        Text(
            text = "+${data.diamonds}",
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

data class DailyCheckinData(
    val label: String,
    val diamonds: Int,
    val checked: Boolean
)

//
//@Composable
//fun BuyDialog(
//    billingManager: BillingManager,
//    onDismissRequest: () -> Unit
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val priceData by billingManager.priceData.collectAsState()
//
//    Dialog(onDismissRequest = onDismissRequest) {
//        Surface(
//            shape = MaterialTheme.shapes.medium,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column(
//                modifier = Modifier.padding(all = 24.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Upgrade to Premium",
//                    style = MaterialTheme.typography.titleLarge,
//                    textAlign = TextAlign.Center
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(
//                    text = "Unlock all premium features including additional wallpapers, no ads, and exclusive content!",
//                    style = MaterialTheme.typography.titleSmall,
//                    textAlign = TextAlign.Center
//                )
//                Spacer(modifier = Modifier.height(24.dp))
//
//                if (priceData.weeklyWithTri.price != "") {
//                    Button(
//                        onClick = {
//                            coroutineScope.launch {
//                                billingManager.startPurchaseFlow(
//                                    context as Activity,
//                                    if (AppConfigManager.useV2WeeklySubscription)
//                                        "weekly_subscription_with_trial_v2" else "weekly_subscription_with_trial",
//                                    BillingClient.ProductType.SUBS
//                                )
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("Subscribe Weekly - ${priceData.weekly.price}")
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                if (priceData.lifetime.price != "") {
//                    Button(
//                        onClick = {
//                            coroutineScope.launch {
//                                billingManager.startPurchaseFlow(
//                                    context as Activity,
//                                    "premium_lifetime",
//                                    BillingClient.ProductType.INAPP
//                                )
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("Buy Lifetime - ${priceData.lifetime.price}")
//                    }
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//
//                if (priceData.lifetime.price == "" && priceData.weekly.price == "") {
//                    Text(
//                        text = "Unfortunately, prices are not available at the moment",
//                        style = MaterialTheme.typography.bodyMedium,
//                        textAlign = TextAlign.Center
//                    )
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(
//                        onClick = onDismissRequest
//                    ) {
//                        Text("Cancel")
//                    }
//                }
//            }
//        }
//    }
//}