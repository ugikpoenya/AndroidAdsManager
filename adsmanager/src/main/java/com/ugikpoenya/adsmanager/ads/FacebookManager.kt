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
import com.ugikpoenya.adsmanager.intervalCounter
import com.ugikpoenya.servermanager.ServerPrefs
import androidx.core.view.isEmpty


var facebookInterstitial: InterstitialAd? = null
var FACEBOOK_TEST_DEVICE_ID: ArrayList<String> = ArrayList()

class FacebookManager {
    val LOG = "LOG_ADS_FAN"
    fun addTestDeviceId(test_id: String) {
        FACEBOOK_TEST_DEVICE_ID.add(test_id)
    }

    fun initFacebookAds(context: Context) {
        Log.d(LOG, "Facebook test device " + FACEBOOK_TEST_DEVICE_ID.size)
        val itemModel = ServerPrefs(context).getItemModel()
        if (itemModel?.facebook_banner.isNullOrEmpty()
            && itemModel?.facebook_native.isNullOrEmpty()
            && itemModel?.facebook_interstitial.isNullOrEmpty()
            && itemModel?.facebook_rewarded_ads.isNullOrEmpty()
        ) {
            Log.d(LOG, "initFacebookAds disable")
        } else {
            if (FACEBOOK_TEST_DEVICE_ID.size > 0) {
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
        val facebook_banner = ServerPrefs(context).getItemModel()?.facebook_banner
        if (facebook_banner.isNullOrEmpty()) {
            Log.d(LOG, "Facebook Banner ID Not Set")
            AdsManager().initBanner(context, VIEW, ORDER, PAGE)
        } else if (VIEW.isEmpty()) {
            val adView = AdView(context, facebook_banner, AdSize.BANNER_HEIGHT_50)
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
        val itemModel = ServerPrefs(context).getItemModel()
        val facebook_native = itemModel?.facebook_native
        if (facebook_native.isNullOrEmpty()) {
            Log.d(LOG, "Facebook Native ID not set ")
            AdsManager().initNative(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            val nativeType = when (PAGE) {
                "home" -> itemModel.home_native_view
                "detail" -> itemModel.detail_native_view
                else -> ""
            }
            if (nativeType == "medium") {
                initFacebookNativeMeidum(context, VIEW, ORDER, PAGE)
            } else {
                initFacebookNativeBanner(context, VIEW, ORDER, PAGE)
            }
        }
    }


    fun initFacebookInterstitial(context: Context) {
        val facebook_interstitial = ServerPrefs(context).getItemModel()?.facebook_interstitial
        if (facebook_interstitial.isNullOrEmpty()) {
            Log.d(LOG, "Facebook Interstitial ID Not Set")
        } else {
            Log.d(LOG, "Init Facebook Interstitial ")
            facebookInterstitial = InterstitialAd(context, facebook_interstitial)
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
        val itemModel = ServerPrefs(context).getItemModel()

        if (facebookInterstitial != null && facebookInterstitial!!.isAdLoaded) {
            facebookInterstitial?.show()
            intervalCounter = itemModel?.interstitial_interval?.toInt() ?: 0
            Log.d(LOG, "Interstitial Facebook Show")
        } else {
            Log.d(LOG, "Interstitial Facebook not loaded")
            AdsManager().showInterstitial(context, ORDER)
        }
    }

    fun showRewardedFacebook(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        val itemModel = ServerPrefs(context).getItemModel()
        if (itemModel?.facebook_rewarded_ads.isNullOrEmpty()) {
            Log.d(LOG, "Facebook Rewarded ID Not set")
            AdsManager().showRewardedAds(context, ORDER, callbackFunction)
        } else {
            Log.d(LOG, "Init Facebook Rewarded ")
            var isRewardEarned = false
            var facebookRewarded = RewardedVideoAd(context, itemModel.facebook_rewarded_ads)
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

//    fun initRewardedFacebook(context: Context) {
//        val itemModel = ServerPrefs(context).getItemModel()
//        if (itemModel?.facebook_rewarded_ads.isNullOrEmpty()) {
//            Log.d(LOG, "Facebook Rewarded ID Not set")
//        } else {
//            Log.d(LOG, "Init Facebook Rewarded ")
//            var facebookRewarded = RewardedVideoAd(context, itemModel.facebook_rewarded_ads)
//            val rewardedVideoAdListener: RewardedVideoAdListener = object : RewardedVideoAdListener {
//                override fun onError(ad: Ad, error: AdError) {
//                    Log.d(LOG, "Facebook Rewarded video ad failed to load: " + error.errorMessage)
//                }
//
//                override fun onAdLoaded(ad: Ad) {
//                    Log.d(LOG, "Facebook Rewarded video ad is loaded and ready to be displayed!")
//                }
//
//                override fun onAdClicked(ad: Ad) {
//                    Log.d(LOG, "Facebook Rewarded video ad clicked!")
//                }
//
//                override fun onLoggingImpression(ad: Ad) {
//                    Log.d(LOG, "Facebook Rewarded video ad impression logged!")
//                }
//
//                override fun onRewardedVideoCompleted() {
//                    Log.d(LOG, "Facebook Rewarded video completed!")
//                    initRewardedFacebook(context)
//                }
//
//                override fun onRewardedVideoClosed() {
//                    Log.d(LOG, "Facebook Rewarded video ad closed!")
//                }
//            }
//            facebookRewarded?.loadAd(
//                facebookRewarded?.buildLoadAdConfig()
//                    ?.withAdListener(rewardedVideoAdListener)
//                    ?.build()
//            )
//        }
//    }


    fun initFacebookNativeBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        Log.d(LOG, "Facebook Native Banner Init")
        val facebook_banner = ServerPrefs(context).getItemModel()?.facebook_banner
        val mNativeBannerAd = NativeBannerAd(context, facebook_banner)
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
        val facebook_native = ServerPrefs(context).getItemModel()?.facebook_native

        val mAdView =
            (context as Activity).layoutInflater.inflate(
                R.layout.native_ads_layout_facebook,
                VIEW,
                false
            )
        val nativeAd = NativeAd(context, facebook_native)
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