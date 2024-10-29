package com.studios1299.playwall.monetization.data

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.studios1299.playwall.app.config.AppConfigManager

// test ad unit id
private const val adUnitId = "ca-app-pub-3940256099942544/5224354917"

class AdManager(private val activity: Activity) {

    private var rewardedAd: RewardedAd? = null
    private var isLoadingAd = false
    private val appConfig = AppConfigManager

    // Loading the ad with detailed debug logs
    fun loadRewardedAd(onAdLoaded: (Boolean) -> Unit = {}) {
        Log.e("AdManager", "Starting to load rewarded ad")

        // Check if Ad is already loading or disabled
        if (isLoadingAd) {
            Log.e("AdManager", "Ad is already loading")
            return
        }
        if (!appConfig.enableRewardAd) {
            Log.e("AdManager", "Rewarded ads are disabled in AppConfig")
            onAdLoaded(false)
            return
        }

        if (rewardedAd != null) {
            Log.e("AdManager", "Rewarded ad is already loaded")
            onAdLoaded(true)
            return
        }

        isLoadingAd = true
        Log.e("AdManager", "Loading rewarded ad with ad unit ID: $adUnitId")

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(activity, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoadingAd = false
                Log.e("AdManager", "Rewarded ad loaded successfully")
                onAdLoaded(true)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                rewardedAd = null
                isLoadingAd = false
                Log.e("AdManager", "Failed to load rewarded ad: ${loadAdError.message}")
                onAdLoaded(false)
            }
        })
    }

    // Displaying the ad if loaded, with logging
    fun showRewardedAdIfLoaded(
        onAdClosed: (() -> Unit)? = null,
        onRewardEarned: (() -> Unit)? = null,
        onAdNotLoaded: (() -> Unit)? = null
    ) {
        if (rewardedAd != null) {
            Log.e("AdManager", "Rewarded ad is loaded and ready to show")
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    Log.e("AdManager", "Rewarded ad was dismissed by the user")
                    onAdClosed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    Log.e("AdManager", "Failed to show rewarded ad: ${adError.message}")
                    onAdClosed?.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.e("AdManager", "Rewarded ad is being shown")
                }
            }
            try {
                rewardedAd?.show(activity) { rewardItem ->
                    Log.e("AdManager", "User earned the reward: ${rewardItem.amount} ${rewardItem.type}")
                    onRewardEarned?.invoke()
                }
            } catch (e: Exception) {
                Log.e("AdManager", "Error occurred while showing rewarded ad: $e")
            }

        } else {
            Log.e("AdManager", "No rewarded ad available to show")
            onAdNotLoaded?.invoke()
        }
    }
}