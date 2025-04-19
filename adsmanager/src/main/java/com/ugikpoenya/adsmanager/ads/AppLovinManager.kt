package com.ugikpoenya.adsmanager.ads

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.ugikpoenya.adsmanager.AdsManager
import com.ugikpoenya.adsmanager.intervalCounter
import com.ugikpoenya.servermanager.ServerPrefs
import androidx.core.view.isEmpty
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.ugikpoenya.adsmanager.R


private var interstitialAd: MaxInterstitialAd? = null
var APPLOVIN_TEST_DEVICE_ID: ArrayList<String> = ArrayList()

class AppLovinManager {
    val LOG = "LOG_ADS_APPLOVIN"
    fun addTestDeviceId(test_id: String) {
        APPLOVIN_TEST_DEVICE_ID.add(test_id)
    }

    fun initAppLovinAds(context: Context) {
        val itemModel = ServerPrefs(context).getItemModel()

        if (itemModel?.applovin_native.isNullOrEmpty() && itemModel?.applovin_banner.isNullOrEmpty() && itemModel?.applovin_interstitial.isNullOrEmpty() && itemModel?.applovin_open_ads.isNullOrEmpty() && itemModel?.applovin_rewarded_ads.isNullOrEmpty() && itemModel?.applovin_sdk_key.isNullOrEmpty()) {
            Log.d(LOG, "initAppLovinAds disable")
        } else {
            // Create the initialization configuration
            val initConfig = AppLovinSdkInitializationConfiguration
                .builder(itemModel.applovin_sdk_key)
                .setMediationProvider(AppLovinMediationProvider.MAX)

            if (FACEBOOK_TEST_DEVICE_ID.size > 0) {
                initConfig.testDeviceAdvertisingIds = APPLOVIN_TEST_DEVICE_ID
            }
            Log.d(LOG, "initAppLovinAds Test " + APPLOVIN_TEST_DEVICE_ID.size)


            // Initialize the SDK with the configuration
            AppLovinSdk.getInstance(context).initialize(initConfig.build()) { sdkConfig ->
                Log.d(LOG, "initAppLovinAds successfully")
                initInterstitialAppLovin(context)
            }
        }
    }

    fun initAppLovinBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        val applovin_banner = ServerPrefs(context).getItemModel()?.applovin_banner
        if (applovin_banner.isNullOrEmpty()) {
            Log.d(LOG, "AppLovin Banner ID Not Set")
            AdsManager().initBanner(context, VIEW, ORDER, PAGE)
        } else if (VIEW.isEmpty()) {
            Log.d(LOG, "AppLovin Banner Init")

            val adView = MaxAdView(applovin_banner, context)
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

    fun initInterstitialAppLovin(context: Context) {
        val applovin_interstitial = ServerPrefs(context).getItemModel()?.applovin_interstitial
        if (applovin_interstitial.isNullOrEmpty()) {
            Log.d(LOG, "AppLovin Interstitial ID set")
        } else {
            Log.d(LOG, "Init AppLovin Interstitial ")
            interstitialAd = MaxInterstitialAd(applovin_interstitial, context)
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
        val itemModel = ServerPrefs(context).getItemModel()

        if (interstitialAd != null && interstitialAd!!.isReady) {
            interstitialAd?.showAd()

            intervalCounter = itemModel?.interstitial_interval?.toInt() ?: 0
            Log.d(LOG, "Interstitial AppLovin Show")
        } else {
            Log.d(LOG, "Interstitial AppLovin not loaded")
            AdsManager().showInterstitial(context, ORDER)
        }
    }

    fun showRewardedAppLovin(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        val applovin_rewarded_ads = ServerPrefs(context).getItemModel()?.applovin_rewarded_ads
        if (applovin_rewarded_ads.isNullOrEmpty()) {
            Log.d(LOG, "AppLovin Rewarded ID Not set")
            AdsManager().showRewardedAds(context, ORDER, callbackFunction)
        } else {
            Log.d(LOG, "Init AppLovin Rewarded ")
            var isRewardEarned = false
            var rewardedAd = MaxRewardedAd.getInstance(applovin_rewarded_ads, context as Activity)
            rewardedAd?.setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    Log.d(LOG, "AppLovin RewardedAd onAdLoaded")
                    rewardedAd.showAd(context)
                }

                override fun onAdDisplayed(p0: MaxAd) {
                    Log.d(LOG, "AppLovin RewardedAd onAdDisplayed")
                }

                override fun onAdHidden(p0: MaxAd) {
                    Log.d(LOG, "AppLovin RewardedAd onAdHidden")
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
        val applovin_native = ServerPrefs(context).getItemModel()?.applovin_native
        if (applovin_native.isNullOrEmpty()) {
            Log.d(LOG, "AppLovin Native ID not set ")
            AdsManager().initNative(context, VIEW, ORDER, PAGE)
        } else if (VIEW.isEmpty()) {
            var nativeAd: MaxAd? = null
            val nativeAdLoader = MaxNativeAdLoader(applovin_native, context)
            nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                    // Clean up any pre-existing native ad to prevent memory leaks.
                    if (nativeAd != null) {
                        nativeAdLoader.destroy(nativeAd)
                    }
                    nativeAd = ad
                    Log.d(LOG, "AppLovin onNativeAdLoaded")
                    // Add ad view to view.
                    VIEW.removeAllViews()
                    VIEW.addView(nativeAdView)
                }

                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                    Log.d(LOG, "AppLovin onNativeAdLoadFailed")
                    Log.d(LOG, error.message)
                    VIEW.removeAllViews()
                    AdsManager().initNative(context, VIEW, ORDER, PAGE)
                }

                override fun onNativeAdClicked(ad: MaxAd) {
                    Log.d(LOG, "AppLovin onNativeAdClicked")
                }
            })

            val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(R.layout.native_ads_layout_applovin)
                .setTitleTextViewId(R.id.title_text_view)
                .setBodyTextViewId(R.id.body_text_view)
                .setAdvertiserTextViewId(R.id.advertiser_text_view)
                .setIconImageViewId(R.id.icon_image_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.options_view)
                .setStarRatingContentViewGroupId(R.id.star_rating_view)
                .setCallToActionButtonId(R.id.cta_button)
                .build()
            var nativeAdView = MaxNativeAdView(binder, context)
            nativeAdLoader.loadAd(nativeAdView)
        }
    }
}