package com.studios1299.playwall.core.presentation.wrzutomat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.studios1299.playwall.monetization.data.BillingManager

enum class WrzutomatType { SMALL, BIG }

@Composable
fun Wrzutomat(
    viewModel: WrzutomatViewModel,
    type: WrzutomatType = WrzutomatType.SMALL,
    onClose: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsOfServiceClick: () -> Unit,
    billingManager: BillingManager
) {
    val subscriptionDetails = viewModel.subscriptionDetails.collectAsState()
    val subscriptionOptions = viewModel.subscriptionOptions.collectAsState()

    if (type == WrzutomatType.SMALL) {
        SubscriptionWeeklyScreen(
            //subscriptionDetails = subscriptionDetails.value,
            //onSubscribe = { viewModel.subscribe(it) },
            onClose = onClose,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onTermsOfServiceClick = onTermsOfServiceClick,
            billingManager = billingManager
        )
    } else {
       SubscriptionOptionsScreen(
           //subscriptionOptions = subscriptionOptions.value,
           //onSubscribe = { viewModel.subscribe(it) },
           onClose = onClose,
           onPrivacyPolicyClick = onPrivacyPolicyClick,
           onTermsOfServiceClick = onTermsOfServiceClick,
           billingManager = billingManager
       )
    }
}