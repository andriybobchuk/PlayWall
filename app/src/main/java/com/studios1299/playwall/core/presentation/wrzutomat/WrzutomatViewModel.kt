package com.studios1299.playwall.core.presentation.wrzutomat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WrzutomatViewModel : ViewModel() {

    private val _subscriptionOptions = MutableStateFlow<List<SubscriptionOption>>(emptyList())
    val subscriptionOptions: StateFlow<List<SubscriptionOption>> = _subscriptionOptions.asStateFlow()

    private val _subscriptionDetails = MutableStateFlow(SubscriptionOption(name = "Weekly", price = 4.99, currency = "USD"))
    val subscriptionDetails: StateFlow<SubscriptionOption> = _subscriptionDetails.asStateFlow()

    init {
        _subscriptionOptions.value = listOf(
            SubscriptionOption(name = "WEEKLY", price = 4.99, currency = "PLN"),
            SubscriptionOption(name = "MONTHLY", price = 9.99, currency = "PLN"),
            SubscriptionOption(name = "YEARLY", price = 99.99, currency = "PLN")
        )

        _subscriptionDetails.value = SubscriptionOption(name = "Weekly", price = 4.99, currency = "CAD")
    }

    fun subscribe(option: SubscriptionOption) {
    }
}