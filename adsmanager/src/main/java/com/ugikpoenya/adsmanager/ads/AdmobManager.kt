package com.ugikpoenya.adsmanager.ads


import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.ugikpoenya.adsmanager.AdsManager
import com.ugikpoenya.adsmanager.R
import com.ugikpoenya.adsmanager.intervalCounter
import com.ugikpoenya.servermanager.ServerPrefs
import androidx.core.view.isEmpty


var admobRewardedAd: RewardedAd? = null
var admobInterstitial: InterstitialAd? = null
private var appOpenAd: AppOpenAd? = null
private var isShowingOpenAd = false
var consentInformation: ConsentInformation? = null
var ADMOB_TEST_DEVICE_ID: ArrayList<String> = ArrayList()

class AdmobManager {
    val LOG = "LOG_ADS_ADMOB"
    fun addTestDeviceId(test_id: String) {
        ADMOB_TEST_DEVICE_ID.add(test_id)
    }

    fun initAdmobAds(context: Context) {
        Log.d(LOG, "Admob test device " + ADMOB_TEST_DEVICE_ID.size)
        val itemModel = ServerPrefs(context).getItemModel()

        if (itemModel?.admob_banner.isNullOrEmpty()
            && itemModel?.admob_interstitial.isNullOrEmpty()
            && itemModel?.admob_native.isNullOrEmpty()
            && itemModel?.admob_rewarded_ads.isNullOrEmpty()
            && itemModel?.admob_open_ads.isNullOrEmpty()
        ) {
            Log.d(LOG, "initAdmobAds disable")
        } else {
            MobileAds.initialize(context) {
                Log.d(LOG, "initAdmobAds successfully")
                initInterstitialAdmob(context)
                initRewardedAdmob(context)
                initOpenAdsAdmob(context)
            }
        }
        isShowingOpenAd = false
    }

    fun initAdmobBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        val admob_banner = ServerPrefs(context).getItemModel()?.admob_banner
        if (admob_banner.isNullOrEmpty()) {
            Log.d(LOG, "Admob Banner ID Not Set")
            AdsManager().initBanner(context, VIEW, ORDER, PAGE)
        } else if (VIEW.isEmpty()) {
            val outMetrics = Resources.getSystem().displayMetrics
            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
            val adView = AdView(context)
            VIEW.addView(adView)
            adView.adUnitId = admob_banner.toString()
            adView.setAdSize(adSize)
            Log.d(LOG, "Admob Banner Init")
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(LOG, "Admob Banner loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(LOG, "Admob Banner failed to load")
                    VIEW.removeAllViews()
                    AdsManager().initBanner(context, VIEW, ORDER, PAGE)
                }
            }
        }
    }

    fun initAdmobNative(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        val itemModel = ServerPrefs(context).getItemModel()
        val admob_native = itemModel?.admob_native
        if (admob_native.isNullOrEmpty()) {
            Log.d(LOG, "Admob Native ID Not Set")
            AdsManager().initNative(context, VIEW, ORDER, PAGE)
        } else if (VIEW.isEmpty()) {
            Log.d(LOG, "Admob Native Init")
            val adLoader = AdLoader.Builder(context, admob_native)
                .forNativeAd { nativeAd ->
                    val nativeType = when (PAGE) {
                        "home" -> itemModel.home_native_view
                        "detail" -> itemModel.detail_native_view
                        else -> ""
                    }

                    val nativeLayout = if (nativeType == "medium") {
                        R.layout.native_ads_layout_admob_medium
                    } else {
                        R.layout.native_ads_layout_admob_small
                    }
                    Log.d(LOG, "Admob native ads loaded")
                    val adView = (context as Activity).layoutInflater
                        .inflate(nativeLayout, null) as NativeAdView
                    populateAdmobNative(nativeAd, adView)
                    VIEW.addView(adView)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(LOG, "Failed to load Admob native " + adError.message)
                        VIEW.removeAllViews()
                        AdsManager().initNative(context, VIEW, ORDER, PAGE)
                    }
                })
                .build()
            val adRequest = AdRequest.Builder().build()
            adLoader.loadAd(adRequest)
        }
    }


    fun showInterstitialAdmob(context: Context, ORDER: Int = 0) {
        val itemModel = ServerPrefs(context).getItemModel()

        if (admobInterstitial != null) {
            admobInterstitial?.show(context as Activity)
            intervalCounter = itemModel?.interstitial_interval?.toInt() ?: 0
            Log.d(LOG, "Interstitial admob Show")
        } else {
            Log.d(LOG, "Interstitial admob not loaded")
            AdsManager().showInterstitial(context, ORDER)
        }
    }

    fun initInterstitialAdmob(context: Context) {
        val itemModel = ServerPrefs(context).getItemModel()

        if (itemModel?.admob_interstitial.isNullOrEmpty()) {
            Log.d(LOG, "Admob Interstitial ID set")
        } else {
            Log.d(LOG, "Init Admob Interstitial ")
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                itemModel.admob_interstitial.toString(),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(LOG, "Interstitial admob failed to load")
                        admobInterstitial = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(LOG, "Interstitial admob loaded")
                        admobInterstitial = interstitialAd
                        admobInterstitial?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    Log.d(LOG, "Interstitial admob Ad was dismissed")
                                }

                                override fun onAdShowedFullScreenContent() {
                                    Log.d(LOG, "Interstitial admob Ad showed fullscreen content.")
                                    admobInterstitial = null
                                    initInterstitialAdmob(context)
                                }
                            }
                    }
                })
        }
    }


    fun showRewardedAdmob(context: Context, ORDER: Int = 0) {
        if (admobRewardedAd != null) {
            Log.d(LOG, "Rewarded admob Show")
            admobRewardedAd.let { ad ->
                ad?.show((context as Activity)) { rewardItem ->
                    Log.d(LOG, "RewardedAdmob User earned the reward.")
                }
            }
        } else {
            Log.d(LOG, "Rewarded admob not loaded")
            AdsManager().showRewardedAds(context, ORDER)
        }
    }

    fun initRewardedAdmob(context: Context) {
        val itemModel = ServerPrefs(context).getItemModel()
        if (itemModel?.admob_rewarded_ads.isNullOrEmpty()) {
            Log.d(LOG, "Admob Rewarded ID not set")
        } else {
            Log.d(LOG, "Init Admob Rewarded ")
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context, itemModel.admob_rewarded_ads.toString(), adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(LOG, "Admob Rewarded " + adError.message)
                    admobRewardedAd = null
                }

                override fun onAdLoaded(admobRewarded: RewardedAd) {
                    Log.d(LOG, "Admob Rewarded  was loaded.")
                    admobRewardedAd = admobRewarded
                    admobRewarded.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            Log.d(LOG, "Admob Rewarded was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.d(LOG, "Admob Rewarded dismissed fullscreen content.")
                            admobRewardedAd = null
                            initRewardedAdmob(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            Log.e(LOG, "Admob Rewarded failed to show fullscreen content.")
                        }

                        override fun onAdImpression() {
                            Log.d(LOG, "Admob Rewarded recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(LOG, "Ad showed fullscreen content.")
                        }
                    }
                }
            })
        }
    }

    // Init open ads
    fun initOpenAdsAdmob(context: Context) {
        val itemModel = ServerPrefs(context).getItemModel()
        val request = AdRequest.Builder().build()
        if (itemModel?.admob_open_ads.isNullOrEmpty()) {
            Log.d(LOG, "Admob Open Ads Disable")
        } else if (appOpenAd == null) {
            Log.d(LOG, "Admob Open Ads Init")
            AppOpenAd.load(
                context, itemModel.admob_open_ads.toString(), request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        Log.d(LOG, "Admob Open Ads Loaded")
                        appOpenAd = ad
                        showOpenAdsAdmob(context)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(LOG, "Admob Open Ads Filed " + loadAdError.message)
                    }
                }
            )
        }
    }

    fun showOpenAdsAdmob(context: Context) {
        if (appOpenAd == null) Log.d(LOG, "Admob Open Ads  null.")
        if (isShowingOpenAd) Log.d(LOG, "Admob Open Ads  Sudah tampil tadi, pisan ae.. ndak tuman.")

        if (appOpenAd !== null && !isShowingOpenAd) {
            Log.d(LOG, "Admob Open Ads  Will show ad.")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        Log.d(LOG, "Admob Open Ads  Dismissed")
                        initOpenAdsAdmob(context)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                    override fun onAdShowedFullScreenContent() {
                        Log.d(LOG, "Admob Open Ads  Showed")
                        isShowingOpenAd = true
                    }
                }
            appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
            appOpenAd!!.show(context as Activity)
        }
    }

    fun resetGDPR() {
        consentInformation?.reset()
    }

    // GDPR Init
    fun initGdpr(context: Context, function: () -> (Unit)) {
        Log.d(LOG, "Init GDPR")
        val params = ConsentRequestParameters.Builder()


        if (ADMOB_TEST_DEVICE_ID.size > 0) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            ADMOB_TEST_DEVICE_ID.forEach {
                debugSettings.addTestDeviceHashedId(it)
            }
            params.setConsentDebugSettings(debugSettings.build())
        }

        consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation?.requestConsentInfoUpdate(context as Activity, params.build(), {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(context) { loadAndShowError: FormError? ->
                if (loadAndShowError != null) {
                    Log.d(LOG, String.format("%s: %s", loadAndShowError.errorCode, loadAndShowError.message))
                }

                if (consentInformation!!.canRequestAds()) {
                    Log.d(LOG, "GDPR canRequestAds")
                    initAdmobAds(context)
                }

                function()
            }
        }, { requestConsentError: FormError ->
            Log.d(LOG, String.format("%s: %s", requestConsentError.errorCode, requestConsentError.message))
        })
    }
}