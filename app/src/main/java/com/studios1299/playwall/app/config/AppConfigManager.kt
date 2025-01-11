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

    //var weeklySubscriptionWithTrialVersion = "1"
    var useV2WeeklySubscription = false
    var initialDevils = 5
    //var adUnitId = "ca-app-pub-9017574195680735/7081123130" // RELEASE ONLY
    var adUnitId = "ca-app-pub-3940256099942544/5224354917" // DEBUG MODE
    //var backendUrl = "http://77.237.234.47:3000/"
    var backendUrl = "https://app.myplaywall.com/"

    var termsOfServices = "https://myplaywall.com/terms-of-service/"
    var privacyPolicy = "https://myplaywall.com/privacy-policy/"
    var contentPolicy = "https://myplaywall.com/content-policy/"
    var faq = "https://myplaywall.com/faq/"
    var insta = "https://myplaywall.com/faq/"
    var tiktok = "https://myplaywall.com/faq/"

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
