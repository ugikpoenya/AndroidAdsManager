package com.ugikpoenya.adsmanager.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.RelativeLayout
import com.ugikpoenya.adsmanager.AdsManager
import com.ugikpoenya.adsmanager.globalItemModel
import com.unity3d.ads.*
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

class UnityManager {
    val LOG = "LOG_ADS_UNITY"
    fun initUnityAds(context: Context) {
        if (globalItemModel.unity_game_id.isEmpty()) {
            Log.d(LOG, "initUnityAds disable")
        } else {
            if (globalItemModel.unity_test_mode) {
                Log.d(LOG, "Init Unity Ads Test Mode ")
            } else {
                Log.d(LOG, "Init Unity Ads Production ")
            }

            UnityAds.initialize(context, globalItemModel.unity_game_id, globalItemModel.unity_test_mode, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(LOG, "onUnityAdsReady onInitializationComplete")
                    initInterstitialUnity(context)
                    initRewardedUnity(context)
                }

                override fun onInitializationFailed(
                    p0: UnityAds.UnityAdsInitializationError?,
                    p1: String?,
                ) {
                    Log.d(LOG, "onUnityAdsReady onInitializationFailed")
                }
            })
        }
    }

    fun initUnityBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        if (globalItemModel.unity_banner.isEmpty()) {
            Log.d(LOG, "Unity Banner ID Not Set")
            AdsManager().initBanner(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            Log.d(LOG, "Unity banner init")
            val bannerView = BannerView(context as Activity, globalItemModel.unity_banner, UnityBannerSize.getDynamicSize(context))
            bannerView.listener = object : BannerView.IListener {
                override fun onBannerLeftApplication(p0: BannerView?) {
                    Log.d(LOG, "Unity onBannerLeftApplication ")
                }

                override fun onBannerClick(p0: BannerView?) {
                    Log.d(LOG, "Unity onBannerClick ")
                }

                override fun onBannerLoaded(p0: BannerView?) {
                    Log.d(LOG, "Unity onBannerLoaded ")
                    VIEW.addView(p0)
                }

                override fun onBannerShown(bannerAdView: BannerView?) {
                    Log.d(LOG, "Unity onBannerShown ")
                }

                override fun onBannerFailedToLoad(p0: BannerView?, p1: BannerErrorInfo?) {
                    Log.d(LOG, "Unity onBannerFailedToLoad ")
                    VIEW.clearAnimation()
                    AdsManager().initBanner(context, VIEW, ORDER, PAGE)
                }
            }
            bannerView.load()
        }
    }

    fun initInterstitialUnity(context: Context) {
        if (globalItemModel.unity_interstitial.isNullOrEmpty()) {
            Log.d(LOG, "Unity Interstitial ID Not set")
        } else {
            Log.d(LOG, "Init Unity Ads Interstitial ")
            UnityAds.load(globalItemModel.unity_interstitial, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(p0: String?) {
                    Log.d(LOG, "Interstitial onUnityAdsAdLoaded $p0")
                }

                override fun onUnityAdsFailedToLoad(
                    p0: String?,
                    p1: UnityAds.UnityAdsLoadError?,
                    p2: String?,
                ) {
                    Log.d(LOG, "Interstitial onUnityAdsFailedToLoad")
                }
            })
        }
    }

    fun showInterstitialUnity(context: Context, ORDER: Int = 0) {
        if (globalItemModel.unity_interstitial.isNullOrEmpty()) {
            Log.d(LOG, "Unity Interstitial ID Not set")
            AdsManager().showInterstitial(context, ORDER)
        } else {
            Log.d(LOG, "Show Unity Ads Interstitial ")
            UnityAds.show(context as Activity, globalItemModel.unity_interstitial, UnityAdsShowOptions(), object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    p0: String?,
                    p1: UnityAds.UnityAdsShowError?,
                    p2: String?,
                ) {
                    Log.d(LOG, "Interstitial onUnityAdsShowFailure")
                    AdsManager().showInterstitial(context, ORDER)
                }

                override fun onUnityAdsShowStart(p0: String?) {
                    Log.d(LOG, "Interstitial onUnityAdsShowStart")
                }

                override fun onUnityAdsShowClick(p0: String?) {
                    Log.d(LOG, "Interstitial onUnityAdsShowClick")
                }

                override fun onUnityAdsShowComplete(
                    p0: String?,
                    p1: UnityAds.UnityAdsShowCompletionState?,
                ) {
                    Log.d(LOG, "Interstitial onUnityAdsShowComplete")
                    AdsManager().interstitielSuccessfullyDisplayed(context)
                    initInterstitialUnity(context)
                }
            })
        }
    }


    fun initRewardedUnity(context: Context) {
        if (globalItemModel.unity_rewarded_ads.isEmpty()) {
            Log.d(LOG, "Unity RewardedAds ID Not set")
        } else {
            Log.d(LOG, "Init Unity Ads RewardedAds ")
            UnityAds.load(globalItemModel.unity_rewarded_ads, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(p0: String?) {
                    Log.d(LOG, "RewardedAds onUnityAdsAdLoaded $p0")
                }

                override fun onUnityAdsFailedToLoad(
                    p0: String?,
                    p1: UnityAds.UnityAdsLoadError?,
                    p2: String?,
                ) {
                    Log.d(LOG, "RewardedAds onUnityAdsFailedToLoad")
                }
            })
        }
    }

    fun showRewardedUnity(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        if (globalItemModel.unity_rewarded_ads.isEmpty()) {
            Log.d(LOG, "Unity RewardedAds ID Not set")
            AdsManager().showRewardedAds(context, ORDER, callbackFunction)
        } else {
            Log.d(LOG, "Init Unity Ads RewardedAds ")
            UnityAds.show(context as Activity, globalItemModel.unity_rewarded_ads, UnityAdsShowOptions(), object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    p0: String?,
                    p1: UnityAds.UnityAdsShowError?,
                    p2: String?,
                ) {
                    Log.d(LOG, "RewardedAds onUnityAdsShowFailure")
                    AdsManager().showRewardedAds(context, ORDER, callbackFunction)
                }

                override fun onUnityAdsShowStart(p0: String?) {
                    Log.d(LOG, "RewardedAds onUnityAdsShowStart")
                }

                override fun onUnityAdsShowClick(p0: String?) {
                    Log.d(LOG, "RewardedAds onUnityAdsShowClick")
                }

                override fun onUnityAdsShowComplete(
                    p0: String,
                    p1: UnityAds.UnityAdsShowCompletionState,
                ) {
                    Log.d(LOG, "RewardedAds onUnityAdsShowComplete")
                    callbackFunction(true)
                    initRewardedUnity(context)
                }
            })
        }
    }
}