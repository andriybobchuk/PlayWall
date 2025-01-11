package com.studios1299.playwall.monetization.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.studios1299.playwall.app.MyApp
import com.studios1299.playwall.app.di.AppModule
import com.studios1299.playwall.core.data.local.Preferences
import com.studios1299.playwall.monetization.presentation.AppState
import com.studios1299.playwall.monetization.presentation.DiamondsViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class PriceData(
    //val weekly: SinglePriceData = SinglePriceData(),
    val weeklyWithTrial: SinglePriceData = SinglePriceData(),
    val weeklyWithTrialV2: SinglePriceData = SinglePriceData(),
    val monthly: SinglePriceData = SinglePriceData(),
    val yearly: SinglePriceData = SinglePriceData(),
    //val lifetime: SinglePriceData = SinglePriceData()
)

data class SinglePriceData(
    val price: String = "",
    val currency: String = ""
)

class BillingManager(
    private val applicationContext: Context
) : PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private var isProcessingPurchase = false

    private val _priceData: MutableStateFlow<PriceData> = MutableStateFlow(PriceData())
    val priceData = _priceData.asStateFlow()

    //private val preferenceManager = PreferenceManager(applicationContext)

    init {
        startBillingConnection()
        checkPremiumStatus()
    }

    var isPremiumUser by mutableStateOf(false)
        private set

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            isProcessingPurchase = false
        } else {
            Log.e("BillingManager", "Error in purchase flow with response code: ${billingResult.responseCode}")
            isProcessingPurchase = false
        }
    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                CoroutineScope(Dispatchers.IO).launch {
                    val billingResult = acknowledgePurchaseWithRetry(purchase.purchaseToken)
                    delay(500)
                    withContext(Dispatchers.Main) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            //val preferenceManager = PreferenceManager(applicationContext)
                            //preferenceManager.setPremiumUser(true)
                            Preferences.setPremiumStatus(true)
                            AppState.updatePremiumStatus(true)
                            checkPremiumStatus()
                            restartApp(applicationContext)
                        } else {
                            Log.e("BillingManager", "Failed to acknowledge purchase with response code: ${billingResult.responseCode}")
                        }
                        isProcessingPurchase = false
                    }
                }
            } else {
                isProcessingPurchase = false
            }
        } else {
            isProcessingPurchase = false
        }
    }


    fun startPurchaseFlow(activity: Activity,productId: String, billingType: String) {
        Log.d("BillingManager", "Starting purchase flow for productId: $productId and billingType: $billingType")


        if (isProcessingPurchase) {
            Log.w("BillingManager", "Purchase flow already in process")
            return  // Prevent multiple purchase flows
        }
        isProcessingPurchase = true

        val productDetailsParamsList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(
                    if (billingType == BillingClient.ProductType.SUBS)
                        BillingClient.ProductType.SUBS
                    else
                        BillingClient.ProductType.INAPP
                )
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productDetailsParamsList)

        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                for (productDetails in productDetailsList) {

                    val flowParams = if (billingType == BillingClient.ProductType.SUBS) {
                        // For subscription
                        if (productDetails.subscriptionOfferDetails != null) {
                            val offerToken = productDetails.subscriptionOfferDetails!![0].offerToken
                            BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    listOf(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .setOfferToken(offerToken)
                                            .build()
                                    )
                                )
                                .build()
                        } else {
                            isProcessingPurchase = false
                            return@queryProductDetailsAsync
                        }
                    } else {
                        BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build()
                                )
                            )
                            .build()
                    }

                    val billingResultLaunch = billingClient.launchBillingFlow(activity, flowParams)
                    if (billingResultLaunch.responseCode != BillingClient.BillingResponseCode.OK) {
                        isProcessingPurchase = false
                    }
                }
            } else {
                isProcessingPurchase = false // Reset flag if query fails
            }
        }
    }

    fun checkPremiumStatus() {
        Log.d("BillingManager", "Checking premium status")

        val subscriptionDeferred = CompletableDeferred<Boolean>()
        val inAppPurchaseDeferred = CompletableDeferred<Boolean>()

        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasSubscription = purchases.any { (it.products.contains("weekly_subscription") || it.products.contains("weekly_subscription_with_trial") || it.products.contains("weekly_subscription_with_trial_v2") || it.products.contains("subscription_monthly") || it.products.contains("yearly_subscription")) && it.purchaseState == Purchase.PurchaseState.PURCHASED }

                purchases.forEach { purchase ->
                    Log.d("BillingManager", "Purchase: $purchase")
                    if(!purchase.isAcknowledged) {
                        CoroutineScope(Dispatchers.IO).launch {
                            acknowledgePurchase(purchase.purchaseToken)
                        }
                    }
                }

                Log.d("BillingManager", "Subscription status checked: $hasSubscription")

                CoroutineScope(Dispatchers.IO).launch {
                    MyApp.appModule.coreRepository.updatePremiumStatus(hasSubscription)
                    Preferences.setPremiumStatus(hasSubscription)
                    AppState.updatePremiumStatus(hasSubscription)
                }

                subscriptionDeferred.complete(hasSubscription)
            } else {
                Log.e("BillingManager", "Failed to query subscriptions with response code: ${billingResult.responseCode}")
                subscriptionDeferred.completeExceptionally(Exception("Failed to query subscriptions"))
            }
        }

        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasInAppPurchase = purchases.any { it.products.contains("premium_lifetime") && it.purchaseState == Purchase.PurchaseState.PURCHASED }

                purchases.forEach { purchase ->
                    if(!purchase.isAcknowledged) {
                        CoroutineScope(Dispatchers.IO).launch {
                            acknowledgePurchase(purchase.purchaseToken)
                        }
                    }
                }

                Log.d("BillingManager", "In-app purchase status checked: $hasInAppPurchase")
                inAppPurchaseDeferred.complete(hasInAppPurchase)
            } else {
                Log.e("BillingManager", "Failed to query in-app purchases with response code: ${billingResult.responseCode}")
                inAppPurchaseDeferred.completeExceptionally(Exception("Failed to query in-app purchases"))
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hasSubscription = subscriptionDeferred.await()
                val hasInAppPurchase = inAppPurchaseDeferred.await()

                isPremiumUser = hasSubscription || hasInAppPurchase
//                isPremiumUser = true


                //val isPremiumFromPrefs = preferenceManager.isPremiumUser()
                val isPremiumFromPrefs = Preferences.isPremium()

                Log.d("BillingManager", "hasSubscription: $hasSubscription | hasInAppPurchase: $hasInAppPurchase")

                if (isPremiumUser != isPremiumFromPrefs) {
                    //preferenceManager.setPremiumUser(isPremiumUser)
                    Preferences.setPremiumStatus(isPremiumUser)
                    AppState.updatePremiumStatus(isPremiumUser)

                    restartApp(applicationContext)
                }

            } catch (e: Exception) {
                Log.e("BillingManager", "Error while checking premium status", e)
            }
        }
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Billing response OK")
                    checkPremiumStatus()
                    getPrices()
                } else {
                    Log.e("BillingManager", billingResult.debugMessage)
                    retryBillingServiceConnection()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e("BillingManager", "Billing service disconnected")
                retryBillingServiceConnection()
            }
        })
    }

    private fun retryBillingServiceConnection() {
        val maxTries = 3
        var tries = 1
        var isConnectionEstablished = false
        do {
            try {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            isConnectionEstablished = true
                            Log.d("BillingManager", "Billing connection retry succeeded.")
                            checkPremiumStatus()
                        } else {
                            Log.e("BillingManager", "Billing connection retry failed: ${billingResult.debugMessage}")
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        Log.e("BillingManager", "Billing service disconnected during retry")
                    }
                })
                tries++
            } catch (e: Exception) {
                e.message?.let { Log.e("BillingManager", it) }
                tries++
            }
        } while (tries <= maxTries && !isConnectionEstablished)
    }

    private fun getPrice(productId: String, callback: (String, String) -> Unit) {
        val productDetailsParamsList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(
                    if (productId == "premium_lifetime")
                        BillingClient.ProductType.INAPP
                    else
                        BillingClient.ProductType.SUBS
                )
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productDetailsParamsList)

        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                if (productDetails.subscriptionOfferDetails != null) {

                    val offerDetails = productDetails.subscriptionOfferDetails!![0]
                    val pricingPhases = offerDetails.pricingPhases.pricingPhaseList

                    val lastPricingPhase = pricingPhases.last()
                    val price = lastPricingPhase.formattedPrice

                    val currency = ""

                    callback(price, currency)
                } else if (productDetails.oneTimePurchaseOfferDetails != null) {
                    val price = productDetails.oneTimePurchaseOfferDetails!!.formattedPrice
                    val currency = ""
                    callback(price, currency)
                } else {
                    callback("Unavailable", "Unavailable")
                }
            } else {
                callback("Unavailable", "Unavailable")
            }
        }
    }


    fun getPrices() {

        CoroutineScope(Dispatchers.IO).launch {
//            getPrice("weekly_subscription") { price, currency ->
//                _priceData.update {
//                    it.copy(weekly = SinglePriceData(price, currency))
//                }
//            }

            getPrice("weekly_subscription_with_trial") { price, currency ->
                _priceData.update {
                    it.copy(weeklyWithTrial = SinglePriceData(price, currency))
                }
            }

            getPrice("weekly_subscription_with_trial_v2") { price, currency ->
                _priceData.update {
                    it.copy(weeklyWithTrialV2 = SinglePriceData(price, currency))
                }
            }

            getPrice("subscription_monthly") { price, currency ->
                _priceData.update {
                    it.copy(monthly = SinglePriceData(price, currency))
                }
            }

            getPrice("yearly_subscription") { price, currency ->
                _priceData.update {
                    it.copy(yearly = SinglePriceData(price, currency))
                }
            }

//            getPrice("premium_lifetime") { price, currency ->
//                _priceData.update {
//                    it.copy(lifetime = SinglePriceData(price, currency))
//                }
//            }
        }
    }

    private suspend fun acknowledgePurchase(purchaseToken: String): BillingResult {
        return suspendCoroutine { continuation ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                continuation.resume(billingResult)
            }
        }
    }

    private suspend fun acknowledgePurchaseWithRetry(purchaseToken: String): BillingResult {
        val maxTries = 3
        var tries = 0
        var lastResult: BillingResult

        do {
            lastResult = acknowledgePurchase(purchaseToken)
            if (lastResult.responseCode == BillingClient.BillingResponseCode.OK) {
                return lastResult
            }
            delay(2000L * (tries + 1))
            tries++
        } while (tries < maxTries)

        return lastResult
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}



//
///*
//    To jest mocno zajebana klasa, ale działa bardzo dobrze. Więc za dużo bym tu nie zmieniał
//    Narazie bierzemy pod uwagę i jednorazowy zakup premium i subskrypcję
//    Być może dojdzie zakup diabełków za cash
// */
//class BillingManager(
//    private val applicationContext: Context,
//    //private val viewModel: DiamondsViewModel? = null
//) : PurchasesUpdatedListener {
//
//   // private val viewModelRef = if (viewModel != null) WeakReference(viewModel) else null
//    //private val viewModelRef = null
//
//    private val billingClient: BillingClient = BillingClient.newBuilder(applicationContext)
//        .setListener(this)
//        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
//        .build()
//
//    private var isProcessingPurchase = false
//
//    private val _priceData: MutableStateFlow<PriceData> = MutableStateFlow(PriceData("Loading...","","Loading...",""))
//    val priceData = _priceData.asStateFlow()
//
//    init {
//        startBillingConnection()
//    }
//
//    var isPremiumUser by mutableStateOf(false)
//        private set
//
//    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
//        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
//            for (purchase in purchases) {
//                handlePurchase(purchase)
//            }
//        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//            isProcessingPurchase = false
//        } else {
//            Log.e("BillingManager", "Error in purchase flow with response code: ${billingResult.responseCode}")
//            isProcessingPurchase = false
//        }
//    }
//
//    private fun restartApp(context: Context) {
//        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
//        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        context.startActivity(intent)
//        (context as? Activity)?.finish()
//    }
//
//    private fun handlePurchase(purchase: Purchase) {
//        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//            Log.d("BillingManager", "Handling purchase: ${purchase.products}")
//            if (!purchase.isAcknowledged) {
//                CoroutineScope(Dispatchers.IO).launch {
//                    val billingResult = acknowledgePurchaseWithRetry(purchase.purchaseToken)
//                    withContext(Dispatchers.Main) {
//                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
////                            val preferenceManager = PreferenceManager(applicationContext)
////                            preferenceManager.setPremiumUser(true)
//                            Preferences.setPremiumStatus(true)
//                            AppState.updatePremiumStatus(true)
//
//                            checkPremiumStatus()
//                            restartApp(applicationContext)
//                        } else {
//                            Log.e("BillingManager", "Failed to acknowledge purchase with response code: ${billingResult.responseCode}")
//                        }
//                        isProcessingPurchase = false
//                    }
//                }
//            } else {
//                isProcessingPurchase = false
//            }
//        } else {
//            isProcessingPurchase = false
//        }
//    }
//
//    fun startPurchaseFlow(activity: Activity, productId: String, billingType: String) {
//        Log.d("BillingManager", "Starting purchase flow for productId: $productId and billingType: $billingType")
//
//        if (isProcessingPurchase) {
//            Log.w("BillingManager", "Purchase flow already in process")
//            return  // Prevent multiple purchase flows
//        }
//        isProcessingPurchase = true
//
//        val productDetailsParamsList = listOf(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(productId)
//                .setProductType(
//                    if (billingType == BillingClient.ProductType.SUBS)
//                        BillingClient.ProductType.SUBS
//                    else
//                        BillingClient.ProductType.INAPP
//                )
//                .build()
//        )
//
//        val params = QueryProductDetailsParams.newBuilder()
//        params.setProductList(productDetailsParamsList)
//
//        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
//                for (productDetails in productDetailsList) {
//
//                    val flowParams = if (billingType == BillingClient.ProductType.SUBS) {
//                        // For subscription
//                        if (productDetails.subscriptionOfferDetails != null) {
//                            val offerToken = productDetails.subscriptionOfferDetails!![0].offerToken
//                            BillingFlowParams.newBuilder()
//                                .setProductDetailsParamsList(
//                                    listOf(
//                                        BillingFlowParams.ProductDetailsParams.newBuilder()
//                                            .setProductDetails(productDetails)
//                                            .setOfferToken(offerToken)
//                                            .build()
//                                    )
//                                )
//                                .build()
//                        } else {
//                            isProcessingPurchase = false
//                            return@queryProductDetailsAsync
//                        }
//                    } else {
//                        BillingFlowParams.newBuilder()
//                            .setProductDetailsParamsList(
//                                listOf(
//                                    BillingFlowParams.ProductDetailsParams.newBuilder()
//                                        .setProductDetails(productDetails)
//                                        .build()
//                                )
//                            )
//                            .build()
//                    }
//
//                    val billingResultLaunch = billingClient.launchBillingFlow(activity, flowParams)
//                    if (billingResultLaunch.responseCode != BillingClient.BillingResponseCode.OK) {
//                        isProcessingPurchase = false
//                    }
//                }
//            } else {
//                isProcessingPurchase = false // Reset flag if query fails
//            }
//        }
//    }
//
//    fun checkPremiumStatus() {
//        Log.d("BillingManager", "Checking premium status")
//
//        val subscriptionDeferred = CompletableDeferred<Boolean>()
//        val inAppPurchaseDeferred = CompletableDeferred<Boolean>()
//
//        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { billingResult, purchases ->
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                val hasSubscription = purchases.any { it.products.contains("weekly_subscription") && it.purchaseState == Purchase.PurchaseState.PURCHASED }
//                Log.d("BillingManager", "Subscription status checked: $hasSubscription")
//
////                viewModelRef?.get()?.setUserSubscribe(hasSubscription)
//                subscriptionDeferred.complete(hasSubscription)
//            } else {
//                Log.e("BillingManager", "Failed to query subscriptions with response code: ${billingResult.responseCode}")
//                subscriptionDeferred.completeExceptionally(Exception("Failed to query subscriptions"))
//            }
//        }
//
//        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { billingResult, purchases ->
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                val hasInAppPurchase = purchases.any { it.products.contains("premium_lifetime") && it.purchaseState == Purchase.PurchaseState.PURCHASED }
//                Log.d("BillingManager", "In-app purchase status checked: $hasInAppPurchase")
////                viewModelRef?.get()?.setUserBought(hasInAppPurchase)
//                inAppPurchaseDeferred.complete(hasInAppPurchase)
//            } else {
//                Log.e("BillingManager", "Failed to query in-app purchases with response code: ${billingResult.responseCode}")
//                inAppPurchaseDeferred.completeExceptionally(Exception("Failed to query in-app purchases"))
//            }
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val hasSubscription = subscriptionDeferred.await()
//                val hasInAppPurchase = inAppPurchaseDeferred.await()
//
//                isPremiumUser = hasSubscription || hasInAppPurchase
//
//                Log.d("BillingManager", "hasSubscription: $hasSubscription | hasInAppPurchase: $hasInAppPurchase")
//
////                viewModelRef?.get()?.checkPremiumStatus()
//
//            } catch (e: Exception) {
//                Log.e("BillingManager", "Error while checking premium status", e)
//            }
//        }
//    }
//
//    private fun startBillingConnection() {
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    Log.d("BillingManager", "Billing response OK")
//                    checkPremiumStatus()
//                    getPrices()
//                } else {
//                    Log.e("BillingManager", billingResult.debugMessage)
//                    retryBillingServiceConnection()
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                Log.e("BillingManager", "Billing service disconnected")
//                retryBillingServiceConnection()
//            }
//        })
//    }
//
//    private fun retryBillingServiceConnection() {
//        val maxTries = 3
//        var tries = 1
//        var isConnectionEstablished = false
//        do {
//            try {
//                billingClient.startConnection(object : BillingClientStateListener {
//                    override fun onBillingSetupFinished(billingResult: BillingResult) {
//                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                            isConnectionEstablished = true
//                            Log.d("BillingManager", "Billing connection retry succeeded.")
//                            checkPremiumStatus()
//                        } else {
//                            Log.e("BillingManager", "Billing connection retry failed: ${billingResult.debugMessage}")
//                        }
//                    }
//
//                    override fun onBillingServiceDisconnected() {
//                        Log.e("BillingManager", "Billing service disconnected during retry")
//                    }
//                })
//                tries++
//            } catch (e: Exception) {
//                e.message?.let { Log.e("BillingManager", it) }
//                tries++
//            }
//        } while (tries <= maxTries && !isConnectionEstablished)
//    }
//
//    private fun getPrice(productId: String, callback: (String, String) -> Unit) {
//        val productDetailsParamsList = listOf(
//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(productId)
//                .setProductType(
//                    if (productId == "weekly_subscription")
//                        BillingClient.ProductType.SUBS
//                    else
//                        BillingClient.ProductType.INAPP
//                )
//                .build()
//        )
//
//        val params = QueryProductDetailsParams.newBuilder()
//        params.setProductList(productDetailsParamsList)
//
//        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
//                val productDetails = productDetailsList[0]
//                if (productDetails.subscriptionOfferDetails != null) {
//                    val pricingPhase = productDetails.subscriptionOfferDetails!![0].pricingPhases.pricingPhaseList[0]
//                    val price = pricingPhase.formattedPrice
//                    Log.d("BillingManager","Price: $price")
//                    val currency = pricingPhase.priceCurrencyCode
//                    Log.d("BillingManager","Currency: $currency")
//                    //val currency = ""
//                    callback(price, currency)
//                } else if (productDetails.oneTimePurchaseOfferDetails != null) {
//                    val price = productDetails.oneTimePurchaseOfferDetails!!.formattedPrice
//                    val currency = productDetails.oneTimePurchaseOfferDetails!!.priceCurrencyCode
//                    //val currency = ""
//                    Log.d("BillingManager","Price: $price $currency")
//                    callback(price, currency)
//                } else {
//                    callback("Unavailable", "Unavailable")
//                }
//            } else {
//                callback("Unavailable", "Unavailable")
//            }
//        }
//    }
//
//    fun getPrices() {
//        CoroutineScope(Dispatchers.IO).launch {
//            getPrice("weekly_subscription") { price, currency ->
//                _priceData.update {
//                    it.copy(weeklyPrice = price, weeklyCurrency = currency)
//                }
//            }
//
//            getPrice("premium_lifetime") { price, currency ->
//                _priceData.update {
//                    it.copy(lifetimePrice = price, lifetimeCurrency = currency)
//                }
//            }
//        }
//    }
//
//    private suspend fun acknowledgePurchase(purchaseToken: String): BillingResult {
//        return suspendCoroutine { continuation ->
//            val params = AcknowledgePurchaseParams.newBuilder()
//                .setPurchaseToken(purchaseToken)
//                .build()
//            billingClient.acknowledgePurchase(params) { billingResult ->
//                continuation.resume(billingResult)
//            }
//        }
//    }
//
//    private suspend fun acknowledgePurchaseWithRetry(purchaseToken: String): BillingResult {
//        val maxTries = 3
//        var tries = 0
//        var lastResult: BillingResult
//
//        do {
//            lastResult = acknowledgePurchase(purchaseToken)
//            if (lastResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                return lastResult
//            }
//            delay(2000L * (tries + 1))
//            tries++
//        } while (tries < maxTries)
//
//        return lastResult
//    }
//
//    fun endConnection() {
//        billingClient.endConnection()
//    }
//}
//
//data class PriceData(
//    val weeklyPrice: String = "1",
//    val weeklyCurrency: String = "USD",
//    val lifetimePrice: String = "10",
//    val lifetimeCurrency: String = "USD"
//)
//
////data class PriceData(
////    val weeklyPrice: String = "Unavailable",
////    val weeklyCurrency: String = "Unavailable",
////    val lifetimePrice: String = "Unavailable",
////    val lifetimeCurrency: String = "Unavailable"
////)
//
