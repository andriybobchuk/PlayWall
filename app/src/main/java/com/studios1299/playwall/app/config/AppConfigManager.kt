package com.studios1299.playwall.app.config

object AppConfigManager {
    var enableRewardAd = true
    var enableBannerAd = true
    var enableGridAd = true
    var enableInterstitialAd = true
    var allowPremiumPreview = true
    var enableAppOpenAd = true
    var enableAppOpenAdOnResume = false
    var enableUserDailyReward = true
    var enableUserOpinionPrompt = true

    var weeklySubscriptionWithTrialVersion = "1"
    var initialDevils = 8
    var adUnitId = ""
    var backendUrl = "http://77.237.234.47:3000/"

    fun updateConfig(config: Map<String, Boolean>) {
        //enableRewardAd = config["enable_reward_ad"] ?: enableRewardAd
        enableBannerAd = config["enable_banner_ad"] ?: enableBannerAd
        enableGridAd = config["enable_grid_ad"] ?: enableGridAd
        enableInterstitialAd = config["enable_interstitial_ad"] ?: enableInterstitialAd
        allowPremiumPreview = config["allow_premium_preview"] ?: allowPremiumPreview
        enableAppOpenAd = config["enable_app_open_ad"] ?: enableAppOpenAd
        enableAppOpenAdOnResume = config["enable_app_open_ad_on_resume"] ?: enableAppOpenAdOnResume
        enableUserDailyReward = config["enable_user_daily_reward"] ?: enableUserDailyReward
        enableUserOpinionPrompt = config["enable_user_opinion_prompt"] ?: enableUserOpinionPrompt
    }
}
