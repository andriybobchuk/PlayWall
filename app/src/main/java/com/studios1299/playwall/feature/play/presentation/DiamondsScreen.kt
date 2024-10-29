package com.studios1299.playwall.feature.play.presentation

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import com.studios1299.playwall.R
import com.studios1299.playwall.core.data.AdManager
import com.studios1299.playwall.core.presentation.components.Toolbars
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiamondsScreen(
   // viewModel: AppViewModel,
    onNavigateToLuckySpin: () -> Unit,
    onNavigateToPremiumPurchase: () -> Unit,
    onBackClick: () -> Unit,
    adManager: AdManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val noAdsMessage = "No ads available"
    val analytics = FirebaseAnalytics.getInstance(context)

    val ColorGradient1 = Color(0xFFFFF176) // Light yellow shade
    val ColorGradient2 = Color(0xFFFFC107) // Golden yellow shade
    val colorStops = arrayOf(
        0.3f to ColorGradient1,
        1.0f to ColorGradient2
    )

    val spacing = 20.dp

    var isLoading by remember {
        mutableStateOf(false)
    }

    BackHandler(enabled = isLoading) {}

    LaunchedEffect(Unit) {
        analytics.logEvent("seen_diamonds_screen", null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colorStops = colorStops))
    ) {
        //val diamondsCount = viewModel.diamondsCount.collectAsState()
        val diamondsCount = 2
        DiamondsScreenTopBar(onBackClick, isLoading)
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)

        ) {
            //DiamondsCount(diamondsCount = diamondsCount.value)
            DiamondsCount(diamondsCount = 4)
            Spacer(modifier = Modifier.size(spacing))
            DailyCheckIn(
            //    viewModel
            )
            Spacer(modifier = Modifier.size(spacing))
            LuckySpinButton(onNavigateToLuckySpin)
            Spacer(modifier = Modifier.size(spacing))
            WatchVideoButton(isLoading) {
                if (!isLoading) {
                    analytics.logEvent("ds_watch_ads_to_get_diamonds_button",null)
                    isLoading = true
                    adManager.loadRewardedAd(
                        onAdLoaded = { adLoaded ->
                            if(adLoaded) {
                                adManager.showRewardedAdIfLoaded(
                                    onRewardEarned = {
                                        //viewModel.addDiamonds(1)
                                    },
                                    onAdClosed = {
                                       // viewModel.showNextDiamondSheet()
                                    }
                                )

                                ///viewModel.hideNextDiamondSheet()
                            } else {
                                /*
                               *
                               * Case that ad is no loaded (no fill, etc)
                               *
                               * */
                                //viewModel.hideNextDiamondSheet()
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
            UnlimitedDiamondsButton(
            //    viewModel
            )
//            Spacer(modifier = Modifier.size(spacing))
//            Button(onClick = { viewModel.addDiamonds(-1) }) {
//                Text(text = "Remove 1 diamond")
//            }


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
            if(!isLoading) {
                onBackClick()
            }
        },
        scrollBehavior = scrollBehavior,
        customContent = {
        }
    )
}

@Composable
fun DiamondsCount(diamondsCount: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Box(modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 16.dp, horizontal = 24.dp)) {
            Text(
                text = "\uD83D\uDC8E $diamondsCount",
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }

    }
}

@Composable
fun DailyCheckIn(
    //viewModel: AppViewModel
) {

//    val dailyCheckinData by viewModel.dailyCheckinState.collectAsState()
//    val hasCheckedInToday by viewModel.hasCheckedInToday.collectAsState()
//
    val listState = rememberLazyListState()
//    val lastCheckedIndex = dailyCheckinData.indexOfLast { it.checked }
//
//    LaunchedEffect(key1 = dailyCheckinData) {
//        if (lastCheckedIndex != -1) {
//            listState.animateScrollToItem(index = lastCheckedIndex)
//        }
//
//    }

    val dailyCheckinData = listOf(
        DailyCheckinData(label = "Day 1", diamonds = 1, checked = true),
        DailyCheckinData(label = "Day 2", diamonds = 1, checked = true),
        DailyCheckinData(label = "Day 3", diamonds = 1, checked = true),
        DailyCheckinData(label = "Day 4", diamonds = 2, checked = false),
        DailyCheckinData(label = "Day 5", diamonds = 2, checked = false),
        DailyCheckinData(label = "Day 6", diamonds = 2, checked = false),
        DailyCheckinData(label = "Day 7", diamonds = 3, checked = false),
        DailyCheckinData(label = "Day 8", diamonds = 4, checked = false),
        DailyCheckinData(label = "Day 9", diamonds = 5, checked = false)
    )


    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            text = "Daily Check-In",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.size(12.dp))
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(dailyCheckinData) {
                DailyCheckInCard(it)
            }
        }

       // if(!hasCheckedInToday) {
        if(!false) {
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                onClick = {}
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
        shape = RoundedCornerShape(8.dp),
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
fun UnlimitedDiamondsButton() {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            //viewModel.showBuyScreen = true
        },
        //border = BorderStroke(width = 4.dp, color = ColorCarbon)
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = "GET UNLIMITED \uD83D\uDC8E",
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
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        if(isLoading) {
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
            Text(text = "WATCH ADS TO GET FREE \uD83D\uDC8E", color = Color.White, fontSize = 17.sp)
        }
    }
}


@Composable
fun DailyCheckInCard(
    data: DailyCheckinData
) {
    val textColor = if(data.checked) Color.White else MaterialTheme.colorScheme.primary
    val bgColor = if(data.checked) MaterialTheme.colorScheme.primary else Color.White
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = bgColor)
            .border(2.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(6.dp))
            .padding(vertical = 4.dp, horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = data.label,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(text = "\uD83D\uDC8E", fontSize = 20.sp)
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
