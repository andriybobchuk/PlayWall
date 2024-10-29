package com.studios1299.playwall.monetization.presentation.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import com.studios1299.playwall.R
import com.studios1299.playwall.monetization.data.AdManager
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UseOfNonLambdaOffsetOverload")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextDiamondSheet(
    //viewModel: AppViewModel,
    adManager: AdManager
) {

    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            if(isLoading) {
                newState != SheetValue.Hidden
            } else {
                true
            }
        },
    )

    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    val noAdsMessage = stringResource(id = R.string.no_ads_available)

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(100)

        launch {
            val offsetAnimation = async {
                offsetY.animateTo(
                    targetValue = -15f,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
            }

            val scaleAnimation = async {
                scale.animateTo(
                    targetValue = 1.3f,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
            }

            offsetAnimation.await()
            scaleAnimation.await()

            val offsetReturn = async {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                )
            }

            val scaleReturn = async {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                )
            }
            offsetReturn.await()
            scaleReturn.await()
        }
    }

    ModalBottomSheet(
        //onDismissRequest = viewModel::hideNextDiamondSheet,
        onDismissRequest = {},
        sheetState = sheetState,
    ) {
        BackHandler(enabled = true, onBack = {
            if (!isLoading) {
                Log.d("PrePremiumSheet", "Back clicked")
                //viewModel.hidePrePremiumSheet()
            } else {
                Log.d("PrePremiumSheet", "Back blocked, loading in progress")
            }
        })

        Column(
            modifier = Modifier.padding(top = 10.dp, start = 16.dp, end= 16.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You earned 1 \uD83D\uDC8E",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(y = offsetY.value.dp)
                    .scale(scale.value)

            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Keep watching to receive more diamonds.",
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (!isLoading) {
                        analytics.logEvent("watch_ads_to_get_more_diamonds_button",null)
                        isLoading = true
                        adManager.loadRewardedAd(
                            onAdLoaded = { adLoaded ->
                                if(adLoaded) {
                                    adManager.showRewardedAdIfLoaded(
                                        onRewardEarned = {
                                           // viewModel.addDiamonds(1)
                                        },
                                        onAdClosed = {
                                            //viewModel.showNextDiamondSheet()
                                        }
                                    )

                                   // viewModel.hideNextDiamondSheet()
                                } else {
                                    /*
                                   *
                                   * Case that ad is no loaded (no fill, etc)
                                   *
                                   * */
                                    //viewModel.hideNextDiamondSheet()
                                    scope.launch {
//                                        SnackbarController.sendEvent(
//                                            SnackbarEvent(
//                                                message = noAdsMessage,
//                                                duration = SnackbarDuration.Long
//                                            )
//                                        )
                                    }
                                }
                                isLoading = false
                            },
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
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
                    Text(text = "Watch ads to get more diamonds \uD83D\uDC8E", color = Color.White)
                }

            }

            OutlinedButton(
                //onClick = viewModel::hideNextDiamondSheet,
                onClick = {},
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                Text(text = "No, thanks", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}


@SuppressLint("UseOfNonLambdaOffsetOverload")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextSpinSheet(
    //viewModel: AppViewModel,
    isLoading: Boolean,
    diamonds: Int,
    onDismiss: () -> Unit,
    onNextAdButtonClick: () -> Unit
) {

    val context = LocalContext.current
    val analytics = FirebaseAnalytics.getInstance(context)
    var isLoadingInside by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            if(isLoading) {
                newState != SheetValue.Hidden
            } else {
                true
            }
        },
    )

    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }


    LaunchedEffect(Unit) {
        delay(100)

        launch {
            val offsetAnimation = async {
                offsetY.animateTo(
                    targetValue = -15f,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
            }

            val scaleAnimation = async {
                scale.animateTo(
                    targetValue = 1.3f,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
            }

            offsetAnimation.await()
            scaleAnimation.await()

            val offsetReturn = async {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                )
            }

            val scaleReturn = async {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                )
            }
            offsetReturn.await()
            scaleReturn.await()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        BackHandler(enabled = true, onBack = {
            if (!isLoading) {
                Log.d("PrePremiumSheet", "Back clicked")
                //viewModel.hideNextSpinSheet()
            } else {
                Log.d("PrePremiumSheet", "Back blocked, loading in progress")
            }
        })

        Column(
            modifier = Modifier.padding(top = 10.dp, start = 16.dp, end= 16.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You earned $diamonds \uD83D\uDC8E",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset(y = offsetY.value.dp)
                    .scale(scale.value)

            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Keep watching to receive more diamonds.",
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    analytics.logEvent("watched_ad_to_get_next_spin",null)
                    isLoadingInside = true
                    onNextAdButtonClick.invoke()
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
            ) {
                if(isLoadingInside) {
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
                    Text(text = "Watch ad to spin again \uD83C\uDF40", color = Color.White)
                }

            }

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                Text(text = "No, thanks", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSheet(
   // viewModel: AppViewModel
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        //onDismissRequest = viewModel::hidePrePremiumSheet,
        onDismissRequest = {},
        sheetState = sheetState,

        ) {

        Column(
            modifier = Modifier.padding(top = 10.dp, start = 16.dp, end= 16.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
           // StarsRow(viewModel = viewModel)
            Spacer(modifier = Modifier.height(20.dp))
            //FeedBackTextField(viewModel = viewModel)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                //onClick = viewModel::sendFeedback,
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send feedback")
            }
        }
    }
}

//@Composable
//fun StarsRow(viewModel: AppViewModel) {
//    val feedbackState by viewModel.rateFeedbackState.collectAsState()
//    Row(
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        FeedbackStar(filled = feedbackState.starsCount > 0) {
//            viewModel.setRateAppCount(1)
//        }
//        FeedbackStar(filled = feedbackState.starsCount > 1) {
//            viewModel.setRateAppCount(2)
//        }
//        FeedbackStar(filled = feedbackState.starsCount > 2) {
//            viewModel.setRateAppCount(3)
//        }
//        FeedbackStar(filled = feedbackState.starsCount > 3) {
//            viewModel.setRateAppCount(4)
//        }
//        FeedbackStar(filled = feedbackState.starsCount > 4) {
//            viewModel.setRateAppCount(5)
//        }
//    }
//}

@Composable
fun FeedbackStar(
    filled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Icon(
        imageVector = if(filled) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = null,
        tint = if(filled) MaterialTheme.colorScheme.primary else Color.Gray,
        modifier = Modifier
            .size(45.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    )
}


//@Composable
//fun FeedBackTextField(
//    viewModel: AppViewModel,
//    modifier: Modifier = Modifier
//) {
//    val feedbackState by viewModel.rateFeedbackState.collectAsState()
//    TextField(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(0.dp),
//        value = feedbackState.feedback,
//        placeholder = { Text(text = "Your feedback (optional)", color = Color.Gray)},
//        colors = TextFieldDefaults.colors(
//            focusedContainerColor = Color.Transparent,
//            unfocusedContainerColor = Color.Transparent,
//            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
//            unfocusedIndicatorColor = Color.Gray
//        ),
//        trailingIcon = { Icon(imageVector = Icons.Default.Create, contentDescription = null)},
//        onValueChange = {
//            viewModel.setFeedbackOpinion(it)
//        }
//    )
//}
