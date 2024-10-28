//package com.studios1299.playwall.feature.play.presentation
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.google.firebase.analytics.FirebaseAnalytics
//import com.studios1299.playwall.core.presentation.components.Toolbars
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DiamondsScreen(
//    viewModel: AppViewModel,
//    onNavigateToLuckySpin: () -> Unit,
//    onNavigateToPremiumPurchase: () -> Unit,
//    adManager: AdManager
//) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val noAdsMessage = stringResource(id = R.string.no_ads_available)
//    val analytics = FirebaseAnalytics.getInstance(context)
//
//    val colorStops = arrayOf(
//        0.7f to ColorGradient1,
//        1.0f to ColorGradient2
//    )
//
//    val spacing = 20.dp
//
//    var isLoading by remember {
//        mutableStateOf(false)
//    }
//
//    BackHandler(enabled = isLoading) {}
//
//    LaunchedEffect(Unit) {
//        analytics.logEvent("seen_diamonds_screen", null)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Brush.verticalGradient(colorStops = colorStops))
//    ) {
//        val diamondsCount = viewModel.diamondsCount.collectAsState()
//        DiamondsScreenTopBar(navController, isLoading)
//        Column(
//            modifier = Modifier
//                .padding(horizontal = 8.dp, vertical = 12.dp)
//
//        ) {
//            DiamondsCount(diamondsCount = diamondsCount.value)
//            Spacer(modifier = Modifier.size(spacing))
//            DailyCheckIn(viewModel)
//            Spacer(modifier = Modifier.size(spacing))
//            LuckySpinButton(navController)
//            Spacer(modifier = Modifier.size(spacing))
//            WatchVideoButton(isLoading) {
//                if (!isLoading) {
//                    analytics.logEvent("ds_watch_ads_to_get_diamonds_button",null)
//                    isLoading = true
//                    adManager.loadRewardedAd(
//                        onAdLoaded = { adLoaded ->
//                            if(adLoaded) {
//                                adManager.showRewardedAdIfLoaded(
//                                    onRewardEarned = {
//                                        viewModel.addDiamonds(1)
//                                    },
//                                    onAdClosed = {
//                                        viewModel.showNextDiamondSheet()
//                                    }
//                                )
//
//                                viewModel.hideNextDiamondSheet()
//                            } else {
//                                /*
//                               *
//                               * Case that ad is no loaded (no fill, etc)
//                               *
//                               * */
//                                viewModel.hideNextDiamondSheet()
//                                scope.launch {
//                                    SnackbarController.sendEvent(
//                                        SnackbarEvent(
//                                            message = noAdsMessage,
//                                            duration = SnackbarDuration.Long
//                                        )
//                                    )
//                                }
//
//
//                            }
//                            isLoading = false
//                        },
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.size(spacing))
//            UnlimitedDiamondsButton(viewModel)
////            Spacer(modifier = Modifier.size(spacing))
////            Button(onClick = { viewModel.addDiamonds(-1) }) {
////                Text(text = "Remove 1 diamond")
////            }
//
//
//        }
//
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DiamondsScreenTopBar(navController: NavController, isLoading: Boolean) {
//    TopAppBar(
//        title = { Text(text = "Get Free Diamonds", color = Color.White) },
//        navigationIcon = {
//            IconButton(onClick = {
//                if(!isLoading) {
//                    navController.navigateUp()
//                }
//            }) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "Go Back",
//                    tint = Color.White
//                )
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorCarbon)
//    )
//}
//
//@Composable
//fun DiamondsCount(diamondsCount: Int) {
//    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//        Box(modifier = Modifier
//            .clip(RoundedCornerShape(8.dp))
//            .background(ColorCarbon)
//            .padding(vertical = 16.dp, horizontal = 24.dp)) {
//            Text(
//                text = "\uD83D\uDC8E $diamondsCount",
//                fontSize = 40.sp,
//                textAlign = TextAlign.Center,
//                color = Color.White
//            )
//        }
//
//    }
//}
//
//@Composable
//fun DailyCheckIn(viewModel: AppViewModel) {
//
//    val dailyCheckinData by viewModel.dailyCheckinState.collectAsState()
//    val hasCheckedInToday by viewModel.hasCheckedInToday.collectAsState()
//
//    val listState = rememberLazyListState()
//    val lastCheckedIndex = dailyCheckinData.indexOfLast { it.checked }
//
//    LaunchedEffect(key1 = dailyCheckinData) {
//        if (lastCheckedIndex != -1) {
//            listState.animateScrollToItem(index = lastCheckedIndex)
//        }
//
//    }
//
//    Column(
//        modifier = Modifier
//            .clip(RoundedCornerShape(8.dp))
//            .background(color = Color.White)
//            .fillMaxWidth()
//            .padding(10.dp)
//    ) {
//        Text(
//            text = "Daily Check-In",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold,
//            color = ColorCarbon
//        )
//        Spacer(modifier = Modifier.size(12.dp))
//        LazyRow(
//            state = listState,
//            horizontalArrangement = Arrangement.spacedBy(6.dp)
//        ) {
//            items(dailyCheckinData) {
//                DailyCheckInCard(it)
//            }
//        }
//
//        if(!hasCheckedInToday) {
//            Spacer(modifier = Modifier.size(16.dp))
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = ColorCarbon),
//                shape = RoundedCornerShape(8.dp),
//                onClick = viewModel::checkIn
//            ) {
//                Text(
//                    modifier = Modifier.padding(12.dp),
//                    text = "CHECK IN \uD83D\uDC4B",
//                    fontSize = 20.sp
//                )
//            }
//        }
//
//    }
//}
//
//@Composable
//fun LuckySpinButton(navController: NavController) {
//
//    Button(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 70.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = ColorCarbon),
//        shape = RoundedCornerShape(8.dp),
//        onClick = {
//            val targetRoute = "lucky-spin"
//            navController.navigateOnce(targetRoute)
//        }
//    ) {
//        Text(
//            modifier = Modifier.padding(12.dp),
//            text = "LUCKY SPIN \uD83C\uDF40",
//            fontSize = 17.sp
//        )
//    }
//}
//
//@Composable
//fun UnlimitedDiamondsButton(viewModel: AppViewModel) {
//
//    Button(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 70.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = ColorGreen),
//        shape = RoundedCornerShape(8.dp),
//        onClick = {
//            viewModel.showBuyScreen = true
//        },
//        border = BorderStroke(width = 4.dp, color = ColorCarbon)
//    ) {
//        Text(
//            modifier = Modifier.padding(12.dp),
//            text = "GET UNLIMITED \uD83D\uDC8E",
//            fontSize = 17.sp,
//            color = ColorCarbon,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}
//
//@Composable
//fun WatchVideoButton(isLoading: Boolean, onClick: () -> Unit) {
//    Button(
//        modifier = Modifier
//            .fillMaxWidth()
//            .defaultMinSize(minHeight = 40.dp)
//            .heightIn(min = 70.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = ColorCarbon),
//        shape = RoundedCornerShape(8.dp),
//        onClick = onClick
//    ) {
//        if(isLoading) {
//            CircularProgressIndicator(
//                color = Color.White,
//                modifier = Modifier.size(20.dp),
//                strokeWidth = 3.dp
//            )
//        } else {
//            Image(
//                painter = painterResource(id = R.drawable.ad),
//                contentDescription = null,
//                modifier = Modifier.size(20.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(text = "WATCH ADS TO GET FREE \uD83D\uDC8E", color = Color.White, fontSize = 17.sp)
//        }
//    }
//}
//
//
//@Composable
//fun DailyCheckInCard(
//    data: DailyCheckinData
//) {
//    val textColor = if(data.checked) Color.White else ColorCarbon
//    val bgColor = if(data.checked) ColorCarbon else Color.White
//    Column(
//        modifier = Modifier
//            .clip(RoundedCornerShape(6.dp))
//            .background(color = bgColor)
//            .border(2.dp, color = ColorCarbon, shape = RoundedCornerShape(6.dp))
//            .padding(vertical = 4.dp, horizontal = 10.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = data.label,
//            color = textColor,
//            fontWeight = FontWeight.Bold
//        )
//        Text(text = "\uD83D\uDC8E", fontSize = 20.sp)
//        Text(
//            text = "+${data.diamonds}",
//            color = textColor,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}
