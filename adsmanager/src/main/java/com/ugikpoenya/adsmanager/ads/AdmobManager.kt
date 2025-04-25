package com.ugikpoenya.adsmanager.ads


import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
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
import com.ugikpoenya.adsmanager.ORDER_ADMOB
import com.ugikpoenya.adsmanager.R
import com.ugikpoenya.adsmanager.globalItemModel
import com.ugikpoenya.servermanager.ServerManager


var admobRewardedAd: RewardedAd? = null
var admobInterstitial: InterstitialAd? = null
var consentInformation: ConsentInformation? = null
var ADMOB_TEST_DEVICE_ID: ArrayList<String> = ArrayList()

class AdmobManager {
    val LOG = "LOG_ADS_ADMOB"
    fun addTestDeviceId(test_id: String) {
        ADMOB_TEST_DEVICE_ID.add(test_id)
    }

    fun initAdmobAds(context: Context, callbackFunction: () -> (Unit)) {
        Log.d(LOG, "Admob test device " + ADMOB_TEST_DEVICE_ID.size)
        if (globalItemModel.admob_banner.isEmpty()
            && globalItemModel.admob_interstitial.isEmpty()
            && globalItemModel.admob_native.isEmpty()
            && globalItemModel.admob_rewarded_ads.isEmpty()
            && globalItemModel.admob_open_ads.isEmpty()
        ) {
            Log.d(LOG, "initAdmobAds disable")
        } else {
            var isAdmobInitCalled = false

            MobileAds.initialize(context) { initializationStatus ->
                val statusMap = initializationStatus.adapterStatusMap
                for ((adapterClass, status) in statusMap) {
                    Log.d(LOG, "Adapter: $adapterClass, State: ${status.initializationState}, Description: ${status.description}")
                }

                Log.d(LOG, "initAdmobAds successfully")
                if (!isAdmobInitCalled) { // Periksa hanya sekali saat inisialisasi pertama
                    isAdmobInitCalled = true  // Update status
                    initInterstitialAdmob(context)
                    initRewardedAdmob(context)
                    AdsManager().showOpenAds(context, ORDER_ADMOB, callbackFunction)
                }
            }

            // Timeout manual 10 detik
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isAdmobInitCalled) {
                    Log.e(LOG, "AdMob init timeout, lanjutkan tanpa menunggu")

                    // Tetap lanjut walau AdMob gagal init
                    initInterstitialAdmob(context)
                    initRewardedAdmob(context)
                    AdsManager().showOpenAds(context, ORDER_ADMOB, callbackFunction)
                }
            }, 10000)
        }
    }

    fun initAdmobBanner(context: Context, VIEW: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        if (globalItemModel.admob_banner.isEmpty()) {
            Log.d(LOG, "Admob Banner ID Not Set")
            AdsManager().initBanner(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            val outMetrics = Resources.getSystem().displayMetrics
            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
            val adView = AdView(context)
            VIEW.addView(adView)
            adView.adUnitId = globalItemModel.admob_banner.toString()
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
        if (globalItemModel.admob_native.isEmpty()) {
            Log.d(LOG, "Admob Native ID Not Set")
            AdsManager().initNative(context, VIEW, ORDER, PAGE)
        } else if (VIEW.childCount == 0) {
            Log.d(LOG, "Admob Native Init")
            val adLoader = AdLoader.Builder(context, globalItemModel.admob_native)
                .forNativeAd { nativeAd ->
                    var nativeType = ServerManager().getItemKey(context, PAGE + "_native_view")

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
        if (admobInterstitial != null) {
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
            admobInterstitial?.show(context as Activity)
            Log.d(LOG, "Interstitial admob Show")
            AdsManager().interstitielSuccessfullyDisplayed(context)
        } else {
            Log.d(LOG, "Interstitial admob not loaded")
            AdsManager().showInterstitial(context, ORDER)
        }
    }

    fun initInterstitialAdmob(context: Context) {
        if (globalItemModel.admob_interstitial.isNullOrEmpty()) {
            Log.d(LOG, "Admob Interstitial ID set")
        } else {
            Log.d(LOG, "Init Admob Interstitial ")
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                globalItemModel.admob_interstitial.toString(),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(LOG, "Interstitial admob failed to load")
                        admobInterstitial = null
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(LOG, "Interstitial admob loaded")
                        admobInterstitial = interstitialAd
                    }
                })
        }
    }


    fun showRewardedAdmob(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        if (admobRewardedAd != null) {
            Log.d(LOG, "Rewarded admob Show")
            var isRewardEarned = false
            admobRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    Log.d(LOG, "Admob Rewarded was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(LOG, "Admob Rewarded dismissed fullscreen content.")
                    AdsManager().RewardedAdsSuccessfullyDisplayed(context)
                    callbackFunction(isRewardEarned)
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
            admobRewardedAd?.show(context as Activity) { rewardItem ->
                Log.d(LOG, "RewardedAdmob: User earned the reward.")
                isRewardEarned = true
            }
        } else {
            Log.d(LOG, "Rewarded admob not loaded")
            AdsManager().showRewardedAds(context, ORDER, callbackFunction)
        }
    }

    fun initRewardedAdmob(context: Context) {
        if (globalItemModel.admob_rewarded_ads.isEmpty()) {
            Log.d(LOG, "Admob Rewarded ID not set")
        } else {
            Log.d(LOG, "Init Admob Rewarded ")
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context, globalItemModel.admob_rewarded_ads.toString(), adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(LOG, "Admob Rewarded " + adError.message)
                    admobRewardedAd = null
                }

                override fun onAdLoaded(admobRewarded: RewardedAd) {
                    Log.d(LOG, "Admob Rewarded  was loaded.")
                    admobRewardedAd = admobRewarded
                }
            })
        }
    }

    // Init open ads
    fun showOpenAdsAdmob(context: Context, ORDER: Int = 0, callbackFunction: (() -> Unit)) {
        val request = AdRequest.Builder().build()
        if (globalItemModel.admob_open_ads.isEmpty()) {
            Log.d(LOG, "Admob Open Ads Disable")
            AdsManager().showOpenAds(context, ORDER, callbackFunction)
        } else {
            Log.d(LOG, "Admob Open Ads Init")
            AppOpenAd.load(
                context, globalItemModel.admob_open_ads.toString(), request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        Log.d(LOG, "Admob Open Ads Loaded")
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(LOG, "Admob Open Ads  Dismissed")
                                AdsManager().OpenAdsSuccessfullyDisplayed(context)
                                callbackFunction()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                AdsManager().showOpenAds(context, ORDER, callbackFunction)
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(LOG, "Admob Open Ads  Showed")
                            }
                        }
                        ad.show(context as Activity)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(LOG, "Admob Open Ads Filed " + loadAdError.message)
                        AdsManager().showOpenAds(context, ORDER, callbackFunction)
                    }
                }
            )

        }
    }

    fun resetGDPR() {
        consentInformation?.reset()
    }

    // GDPR Init
    fun initGdpr(context: Context, callbackFunction: () -> (Unit)) {
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
                    initAdmobAds(context, callbackFunction)
                } else {
                    Log.d(LOG, "GDPR canNotRequestAds")
                    callbackFunction()
                }

            }
        }, { requestConsentError: FormError ->
            Log.d(LOG, String.format("%s: %s", requestConsentError.errorCode, requestConsentError.message))
            initAdmobAds(context, callbackFunction)
        })
    }
}