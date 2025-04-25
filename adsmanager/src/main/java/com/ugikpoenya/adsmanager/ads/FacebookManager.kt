package com.ugikpoenya.adsmanager.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.RelativeLayout
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSettings
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdListener
import com.facebook.ads.NativeBannerAd
import com.facebook.ads.NativeBannerAdView
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.ugikpoenya.adsmanager.AdsManager
import com.ugikpoenya.adsmanager.R
import com.ugikpoenya.adsmanager.globalItemModel
import com.ugikpoenya.servermanager.ServerManager


var facebookInterstitial: InterstitialAd? = null
var FACEBOOK_TEST_DEVICE_ID: ArrayList<String> = ArrayList()

class FacebookManager {
    val LOG = "LOG_ADS_FAN"
    fun addTestDeviceId(test_id: String) {
        FACEBOOK_TEST_DEVICE_ID.add(test_id)
    }

    fun initFacebookAds(context: Context) {
        Log.d(LOG, "Facebook test device " + FACEBOOK_TEST_DEVICE_ID.size)
        if (globalItemModel.facebook_banner.isEmpty()
            && globalItemModel.facebook_native.isEmpty()
            && globalItemModel.facebook_interstitial.isEmpty()
            && globalItemModel.facebook_rewarded_ads.isEmpty()
        ) {
            Log.d(LOG, "initFacebookAds disable")
        } else {
            if (FACEBOOK_TEST_DEVICE_ID.isNotEmpty()) {
                AdSettings.addTestDevices(FACEBOOK_TEST_DEVICE_ID)
            }
            Log.d(LOG, "initFacebookAds Test " + FACEBOOK_TEST_DEVICE_ID.size)

            AudienceNetworkAds
                .buildInitSettings(context)
                .withInitListener {
                    Log.d(LOG, "Facebook Ads Initialized")
                    initFacebookInterstitial(context)
                }.initialize()
        }
    }

    fun initFacebookBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        if (globalItemModel.facebook_banner.isEmpty()) {
            Log.d(LOG, "Facebook Banner ID Not Set")
            AdsManager().initBanner(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            val adView = AdView(context, globalItemModel.facebook_banner, AdSize.BANNER_HEIGHT_50)
            val loadAdConfig = adView.buildLoadAdConfig()
                .withAdListener(object : AdListener {
                    override fun onAdClicked(p0: Ad?) {
                        Log.d(LOG, " Facebook Banner onAdClicked")
                    }

                    override fun onError(p0: Ad?, p1: AdError?) {
                        Log.d(LOG, " Facebook Banner onError" + p1?.errorMessage)
                        VIEW.removeAllViews()
                        AdsManager().initBanner(context, VIEW, ORDER, PAGE)
                    }

                    override fun onAdLoaded(p0: Ad?) {
                        Log.d(LOG, " Facebook Banner onAdLoaded")
                        VIEW.addView(adView)
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                        Log.d(LOG, "Facebook Banner onLoggingImpression")
                    }
                })
                .build()
            adView.loadAd(loadAdConfig)
        }
    }

    fun initFacebookNative(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        if (globalItemModel.facebook_native.isEmpty()) {
            Log.d(LOG, "Facebook Native ID not set ")
            AdsManager().initNative(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            var nativeType = ServerManager().getItemKey(context, "native_view_$PAGE")
            if (nativeType.isNullOrEmpty()) nativeType = globalItemModel.native_view

            if (nativeType == "medium") {
                initFacebookNativeMeidum(context, VIEW, ORDER, PAGE)
            } else {
                initFacebookNativeBanner(context, VIEW, ORDER, PAGE)
            }
        }
    }


    fun initFacebookInterstitial(context: Context) {
        if (globalItemModel.facebook_interstitial.isEmpty()) {
            Log.d(LOG, "Facebook Interstitial ID Not Set")
        } else {
            Log.d(LOG, "Init Facebook Interstitial ")
            facebookInterstitial = InterstitialAd(context, globalItemModel.facebook_interstitial)
            val loadAdConfig = facebookInterstitial?.buildLoadAdConfig()
                ?.withAdListener(object : InterstitialAdListener {
                    override fun onInterstitialDisplayed(p0: Ad?) {
                        Log.d(LOG, "Facebook Interstitial onInterstitialDisplayed")
                    }

                    override fun onAdClicked(p0: Ad?) {
                        Log.d(LOG, "Facebook Interstitial onAdClicked")
                    }

                    override fun onInterstitialDismissed(p0: Ad?) {
                        Log.d(LOG, "Facebook Interstitial onInterstitialDismissed")
                        facebookInterstitial?.loadAd()
                    }

                    override fun onError(p0: Ad?, p1: AdError?) {
                        Log.d(LOG, "Facebook Interstitial onError " + p1?.errorMessage)
                    }

                    override fun onAdLoaded(p0: Ad?) {
                        Log.d(LOG, "Facebook Interstitial onAdLoaded")
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                        Log.d(LOG, "Facebook Interstitial onLoggingImpression")
                    }
                })
                ?.build()
            facebookInterstitial?.loadAd(loadAdConfig)
        }
    }


    fun showInterstitialFacebook(context: Context, ORDER: Int = 0) {
        if (facebookInterstitial != null && facebookInterstitial!!.isAdLoaded) {
            facebookInterstitial?.show()
            Log.d(LOG, "Interstitial Facebook Show")
            AdsManager().interstitielSuccessfullyDisplayed(context)
        } else {
            Log.d(LOG, "Interstitial Facebook not loaded")
            AdsManager().showInterstitial(context, ORDER)
        }
    }

    fun showRewardedFacebook(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        if (globalItemModel.facebook_rewarded_ads.isEmpty()) {
            Log.d(LOG, "Facebook Rewarded ID Not set")
            AdsManager().showRewardedAds(context, ORDER, callbackFunction)
        } else {
            Log.d(LOG, "Init Facebook Rewarded ")
            var isRewardEarned = false
            var facebookRewarded = RewardedVideoAd(context, globalItemModel.facebook_rewarded_ads)
            val rewardedVideoAdListener: RewardedVideoAdListener = object : RewardedVideoAdListener {
                override fun onError(ad: Ad, error: AdError) {
                    Log.d(LOG, "Facebook Rewarded video ad failed to load: " + error.errorMessage)
                    AdsManager().showRewardedAds(context, ORDER, callbackFunction)
                }

                override fun onAdLoaded(ad: Ad) {
                    Log.d(LOG, "Facebook Rewarded video ad is loaded and ready to be displayed!")
                    facebookRewarded.show()
                }

                override fun onAdClicked(ad: Ad) {
                    Log.d(LOG, "Facebook Rewarded video ad clicked!")
                }

                override fun onLoggingImpression(ad: Ad) {
                    Log.d(LOG, "Facebook Rewarded video ad impression logged!")
                }

                override fun onRewardedVideoCompleted() {
                    Log.d(LOG, "Facebook Rewarded video completed!")
                    isRewardEarned = true
                }

                override fun onRewardedVideoClosed() {
                    Log.d(LOG, "Facebook Rewarded video ad closed!")
                    AdsManager().RewardedAdsSuccessfullyDisplayed(context)
                    callbackFunction(isRewardEarned)
                }
            }
            facebookRewarded.loadAd(
                facebookRewarded.buildLoadAdConfig()
                    ?.withAdListener(rewardedVideoAdListener)
                    ?.build()
            )
        }
    }

    fun initFacebookNativeBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        Log.d(LOG, "Facebook Native Banner Init")
        val mNativeBannerAd = NativeBannerAd(context, globalItemModel.facebook_banner)
        val loadAdConfig = mNativeBannerAd.buildLoadAdConfig()
            .withAdListener(object : NativeAdListener {
                override fun onAdClicked(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Banner onAdClicked")
                }

                override fun onMediaDownloaded(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Banner onMediaDownloaded")
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.d(LOG, "Facebook Native Banner onError" + p1?.errorMessage)
                    VIEW.removeAllViews()
                    AdsManager().initNative(context, VIEW, ORDER, PAGE)
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Banner onAdLoaded")
                    val adView = NativeBannerAdView.render(
                        context,
                        mNativeBannerAd,
                        NativeBannerAdView.Type.HEIGHT_100
                    )
                    VIEW.addView(adView)
                }

                override fun onLoggingImpression(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Banner onLoggingImpression")
                }
            })
            .build()
        mNativeBannerAd.loadAd(loadAdConfig)
    }


    fun initFacebookNativeMeidum(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        Log.d(LOG, "Facebook Native Meidum  Init")
        val mAdView =
            (context as Activity).layoutInflater.inflate(
                R.layout.native_ads_layout_facebook,
                VIEW,
                false
            )
        val nativeAd = NativeAd(context, globalItemModel.facebook_native)
        val loadAdConfig = nativeAd.buildLoadAdConfig()
            .withAdListener(object : NativeAdListener {
                override fun onAdClicked(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Meidum onAdClicked")
                }

                override fun onMediaDownloaded(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Meidum onMediaDownloaded")
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.d(LOG, "Facebook Native Meidum onError" + p1?.errorMessage)
                    VIEW.removeAllViews()
                    AdsManager().initNative(context, VIEW, ORDER, PAGE)
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Meidum onAdLoaded")
                    if (nativeAd !== p0) {
                        // Race condition, load() called again before last ad was displayed
                        return
                    }
                    if (mAdView == null) {
                        return
                    }
                    nativeAd.unregisterView()
                    populateFacebookNative(nativeAd, mAdView)
                    VIEW.addView(mAdView)
                }

                override fun onLoggingImpression(p0: Ad?) {
                    Log.d(LOG, "Facebook Native Meidum onLoggingImpression")
                }
            })
            .build()
        nativeAd.loadAd(loadAdConfig)
    }
}