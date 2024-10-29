package com.studios1299.playwall.monetization.presentation.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.studios1299.playwall.R

@Composable
fun RateDialog(
    //viewModel: AppViewModel,
    onSuccess: () -> Unit
) {

    val context = LocalContext.current
    val manager = ReviewManagerFactory.create(context)

    fun showRatePrompt() {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val activity = context as Activity
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    Log.d("RateAppDialog", "Review flow completed")
                }
            } else {
                val reviewErrorCode = (task.exception as ReviewException).errorCode
                Log.e("RateAppDialog", "Error: $reviewErrorCode")
            }
        }
    }

    Dialog(
        onDismissRequest = { /* No action on dismiss */ },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 40.dp)
            ) {
                Text(text = "\uD83D\uDE42", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = stringResource(id = R.string.we_work_hard), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    painter = painterResource(id = R.drawable.winbg),
                    contentDescription = stringResource(id = R.string.rate_the_app),
                    Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val imageWidth = size.width
                            if (offset.x < imageWidth * 0.2f) {
//                                viewModel.setRateAppCount(1)
//                                viewModel.setFeedbackSheet(true)
                                Log.d("RateApp", "1 start")
                            } else if (offset.x < imageWidth * 0.4f){
//                                viewModel.setRateAppCount(2)
//                                viewModel.setFeedbackSheet(true)
                                Log.d("RateApp", "2 stars")
                            } else if (offset.x < imageWidth * 0.6f) {
//                                viewModel.setRateAppCount(3)
//                                viewModel.setFeedbackSheet(true)
                                Log.d("RateApp", "3 stars")
                            } else if (offset.x < imageWidth * 0.8f) {
//                                viewModel.setRateAppCount(4)
//                                viewModel.setFeedbackSheet(true)
                                Log.d("RateApp", "4 stars")
                            } else {
                                Log.d("RateApp", "5 stars")
                                showRatePrompt()
                            }
                            onSuccess()
                        }
                    }
//                        Modifier.clickable {
//                        showRatePrompt()
//                        onSuccess()
//                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = stringResource(id = R.string.click_here_if_you_are_happy), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        showRatePrompt()
                        onSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(text = stringResource(id = R.string.rate_the_app), color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(text = stringResource(id = R.string.no_thanks), color = Color.White)
                }
            }
        }
    }
}
