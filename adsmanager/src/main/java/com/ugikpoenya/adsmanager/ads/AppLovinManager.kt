package com.ugikpoenya.adsmanager.ads

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxAdViewConfiguration
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.applovin.sdk.AppLovinSdkUtils
import com.ugikpoenya.adsmanager.AdsManager
import com.ugikpoenya.adsmanager.globalItemModel


private var interstitialAd: MaxInterstitialAd? = null
var APPLOVIN_TEST_DEVICE_ID: ArrayList<String> = ArrayList()

class AppLovinManager {
    val LOG = "LOG_ADS_APPLOVIN"
    fun addTestDeviceId(test_id: String) {
        APPLOVIN_TEST_DEVICE_ID.add(test_id)
    }

    fun initAppLovinAds(context: Context) {
        if (globalItemModel.applovin_merc.isEmpty()
            && globalItemModel.applovin_banner.isEmpty()
            && globalItemModel.applovin_interstitial.isEmpty()
            && globalItemModel.applovin_open_ads.isEmpty()
            && globalItemModel.applovin_rewarded_ads.isEmpty()
            && globalItemModel.applovin_sdk_key.isEmpty()
        ) {
            Log.d(LOG, "initAppLovinAds disable")
        } else {
            // Create the initialization configuration
            val initConfig = AppLovinSdkInitializationConfiguration
                .builder(globalItemModel.applovin_sdk_key)
                .setMediationProvider(AppLovinMediationProvider.MAX)

            if (FACEBOOK_TEST_DEVICE_ID.size > 0) {
                initConfig.testDeviceAdvertisingIds = APPLOVIN_TEST_DEVICE_ID
            }
            Log.d(LOG, "initAppLovinAds Test " + APPLOVIN_TEST_DEVICE_ID.size)


            // Initialize the SDK with the configuration
            AppLovinSdk.getInstance(context).initialize(initConfig.build()) { sdkConfig ->
                Log.d(LOG, "initAppLovinAds successfully")
                initInterstitialAppLovin()
            }
        }
    }

    fun initAppLovinBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        if (globalItemModel.applovin_banner.isEmpty()) {
            Log.d(LOG, "AppLovin Banner ID Not Set")
            AdsManager().initBanner(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            Log.d(LOG, "AppLovin Banner Init")


            val adView = MaxAdView(globalItemModel.applovin_banner, context)
            adView.setListener(object : MaxAdViewAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Banner onAdLoaded")
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Banner onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Banner onAdHidden")
                }

                override fun onAdClicked(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Banner onAdClicked")
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    Log.d(LOG, "AppLovin Banner onAdLoadFailed")
                    Log.d(LOG, p1.message)
                    VIEW.removeAllViews()
                    AdsManager().initBanner(context, VIEW, ORDER, PAGE)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.d(LOG, "AppLovin Banner onAdDisplayFailed")
                    Log.d(LOG, p1.message)
                }

                override fun onAdExpanded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Banner onAdExpanded")
                }

                override fun onAdCollapsed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Banner onAdCollapsed")
                }

            })

            val width = ViewGroup.LayoutParams.MATCH_PARENT
            // Banner height on phones and tablets is 50 and 90, respectively
            val heightPx = 90
            adView.layoutParams = FrameLayout.LayoutParams(width, heightPx)
            // Set background or background color for banners to be fully functional
            adView.setBackgroundColor(Color.BLACK)
            VIEW.addView(adView)
            adView.loadAd()
        }
    }

    fun initInterstitialAppLovin() {
        if (globalItemModel.applovin_interstitial.isEmpty()) {
            Log.d(LOG, "AppLovin Interstitial ID set")
        } else {
            Log.d(LOG, "Init AppLovin Interstitial ")
            interstitialAd = MaxInterstitialAd(globalItemModel.applovin_interstitial)
            interstitialAd?.setExtraParameter("container_view_ads", "true")
            interstitialAd?.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Interstitial onAdLoaded")
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Interstitial onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Interstitial onAdHidden")
                    interstitialAd?.loadAd()
                }

                override fun onAdClicked(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Interstitial onAdClicked")
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    Log.d(LOG, "AppLovin Interstitial onAdLoadFailed")
                    Log.d(LOG, p1.message)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.d(LOG, "AppLovin Interstitial onAdDisplayFailed")
                    Log.d(LOG, p1.message)
                }
            })

            // Load the first ad
            interstitialAd?.loadAd()
        }
    }

    fun showInterstitialAppLovin(context: Context, ORDER: Int = 0) {
        if (interstitialAd != null && interstitialAd!!.isReady) {
            interstitialAd?.showAd(context as Activity)
            Log.d(LOG, "Interstitial AppLovin Show")
            AdsManager().interstitielSuccessfullyDisplayed(context)
        } else {
            Log.d(LOG, "Interstitial AppLovin not loaded")
            AdsManager().showInterstitial(context, ORDER)
        }
    }

    fun showRewardedAppLovin(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        if (globalItemModel.applovin_rewarded_ads.isEmpty()) {
            Log.d(LOG, "AppLovin Rewarded ID Not set")
            AdsManager().showRewardedAds(context, ORDER, callbackFunction)
        } else {
            Log.d(LOG, "Init AppLovin Rewarded ")
            var isRewardEarned = false
            var rewardedAd = MaxRewardedAd.getInstance(globalItemModel.applovin_rewarded_ads)
            rewardedAd?.setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin RewardedAd onAdLoaded")
                    rewardedAd.showAd(context as Activity)
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin RewardedAd onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd) {
                    Log.d(LOG, "AppLovin RewardedAd onAdHidden")
                    AdsManager().RewardedAdsSuccessfullyDisplayed(context)
                    callbackFunction(isRewardEarned)
                }

                override fun onAdClicked(p0: MaxAd) {
                    Log.d(LOG, "AppLovin RewardedAd onAdClicked")
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    Log.d(LOG, "AppLovin RewardedAd onAdLoadFailed")
                    Log.d(LOG, p1.message)
                    AdsManager().showRewardedAds(context, ORDER, callbackFunction)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.d(LOG, "AppLovin RewardedAd onAdDisplayFailed")
                    Log.d(LOG, p1.message)
                    AdsManager().showRewardedAds(context, ORDER, callbackFunction)
                }

                override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
                    Log.d(LOG, "AppLovin RewardedAd onUserRewarded")
                    isRewardEarned = true
                }
            })
            rewardedAd?.loadAd()
        }
    }

    fun initAppLovinNative(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        if (globalItemModel.applovin_merc.isEmpty()) {
            Log.d(LOG, "AppLovin MREC ID not set ")
            AdsManager().initNative(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            Log.d(LOG, "AppLovin MREC Init")

            // Set a custom width, in dp, for the inline adaptive MREC. Otherwise stretch to screen width by using ViewGroup.LayoutParams.MATCH_PARENT
            val widthDp = 400
            val widthPx = AppLovinSdkUtils.dpToPx(context, widthDp);

            // Set a maximum height, in dp, for the inline adaptive MREC. Otherwise use standard MREC height of 250 dp
            // Google recommends a height greater than 50 dp, with a minimum of 32 dp and a maximum equal to the screen height
            // The value must also not exceed the height of the MaxAdView
            val heightDp = 300
            val heightPx = AppLovinSdkUtils.dpToPx(context, heightDp);


            val config = MaxAdViewConfiguration.builder()
                .setAdaptiveType(MaxAdViewConfiguration.AdaptiveType.INLINE)
                .setAdaptiveWidth(widthDp) // Optional: The adaptive ad will span the width of the application window if a value is not specified
                .setInlineMaximumHeight(heightDp) // Optional: The maximum height will be the screen height if a value is not specified
                .build()


            val adView = MaxAdView(globalItemModel.applovin_merc, MaxAdFormat.MREC, context)
            adView.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx)
            adView.setBackgroundColor(Color.BLACK)
            adView.setListener(object : MaxAdViewAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin MREC onAdLoaded")
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin MREC onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd) {
                    Log.d(LOG, "AppLovin MREC onAdHidden")
                }

                override fun onAdClicked(p0: MaxAd) {
                    Log.d(LOG, "AppLovin MREC onAdClicked")
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    Log.d(LOG, "AppLovin MREC onAdLoadFailed")
                    Log.d(LOG, p1.message)
                    VIEW.removeAllViews()
                    AdsManager().initNative(context, VIEW, ORDER, PAGE)
                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.d(LOG, "AppLovin MREC onAdDisplayFailed")
                    Log.d(LOG, p1.message)
                }

                override fun onAdExpanded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin MREC onAdExpanded")
                }

                override fun onAdCollapsed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin MREC onAdCollapsed")
                }

            })
            VIEW.addView(adView)
            adView.loadAd()
        }
    }

    // Init open ads
    fun showOpenAdsAppLovin(context: Context, ORDER: Int = 0, callbackFunction: (() -> Unit)) {
        if (globalItemModel.applovin_open_ads.isEmpty()) {
            Log.d(LOG, "AppLovin Open Ads Disable")
            AdsManager().showOpenAds(context, ORDER, callbackFunction)
        } else {
            Log.d(LOG, "AppLovin Open Ads Init")
            var appOpenAd = MaxAppOpenAd(globalItemModel.applovin_open_ads)
            appOpenAd.setListener(object : MaxAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Open Ads onAdLoaded")
                    appOpenAd.showAd()
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Open Ads onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Open Ads onAdHidden")
                    AdsManager().OpenAdsSuccessfullyDisplayed(context)
                    callbackFunction()
                }

                override fun onAdClicked(p0: MaxAd) {
                    Log.d(LOG, "AppLovin Open Ads onAdClicked")
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    Log.d(LOG, "AppLovin Open Ads onAdLoadFailed")
                    Log.d(LOG, p1.message)
                    AdsManager().showOpenAds(context, ORDER, callbackFunction)

                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.d(LOG, "AppLovin Open Ads onAdDisplayFailed")
                    Log.d(LOG, p1.message)
                    AdsManager().showOpenAds(context, ORDER, callbackFunction)
                }
            })
            appOpenAd.loadAd()

        }

    }

}