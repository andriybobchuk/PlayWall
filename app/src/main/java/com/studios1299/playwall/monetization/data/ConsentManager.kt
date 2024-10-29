package com.studios1299.playwall.monetization.data

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class ConsentManager private constructor(context: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gatherConsent(
        activity: Activity,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
    ) {

        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    // Consent has been gathered.
                    onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                }
            },
            { requestConsentError ->
                onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
            },
        )
    }

    companion object {
        @Volatile private var instance: ConsentManager? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: ConsentManager(context).also { instance = it }
                }
    }
}


// UŻYCIE:
//try {
//    gatherConsent()
//} catch (e: Exception) {
//    Log.d("SplashActivity", "Error gathering consent: $e")
//}
//
//
//
//private fun gatherConsent() {
//    consentManager.gatherConsent(this) { error ->
//        if (error == null) {
//            Log.d("SplashActivity", "Consent gathered")
//        } else {
//            Log.e("SplashActivity", "Consent gathering error: ${error.message}")
//        }
//        consentFormDisplayed = true
//        navigateToMainActivity(0)
//    }
//}