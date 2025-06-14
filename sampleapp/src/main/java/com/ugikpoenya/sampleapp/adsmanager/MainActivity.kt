package com.ugikpoenya.sampleapp.adsmanager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ugikpoenya.adsmanager.AdsManager
import com.ugikpoenya.adsmanager.ORDER_ADMOB
import com.ugikpoenya.adsmanager.ORDER_APPLOVIN
import com.ugikpoenya.adsmanager.ORDER_FACEBOOK
import com.ugikpoenya.adsmanager.ORDER_UNITY
import com.ugikpoenya.adsmanager.ads.AdmobManager
import com.ugikpoenya.adsmanager.ads.AppLovinManager
import com.ugikpoenya.adsmanager.ads.FacebookManager
import com.ugikpoenya.adsmanager.ads.UnityManager

class MainActivity : AppCompatActivity() {
    val LOG = "LOG_ADS_MANAGER"
    var lyViewBannerAds: RelativeLayout? = null
    var lyViewNativeAds: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lyViewBannerAds = findViewById<RelativeLayout>(R.id.lyViewBannerAds)
        lyViewNativeAds = findViewById<RelativeLayout>(R.id.lyViewNativeAds)
        AdsManager().initBanner(this, lyViewBannerAds!!, 0, "home")
        AdsManager().initNative(this, lyViewNativeAds!!, 0, "home")
    }

    fun admobResetGdpr(view: View) {
        Log.d(LOG, "admobResetGdpr")
        AdmobManager().resetGDPR()
    }

    fun showOpenAdsAdmob(view: View) {
        Log.d(LOG, "showOpenAdsAdmob")
        AdsManager().showOpenAds(this, ORDER_ADMOB) {
            Log.d(LOG, "Show OpenAds Callback Function")
        }
    }

    fun showInterstitialAdmob(view: View) {
        Log.d(LOG, "showInterstitialAdmob")
        AdsManager().showInterstitial(this, ORDER_ADMOB)
    }

    fun showRewardedAdmob(view: View) {
        Log.d(LOG, "showRewardedAdmob")
        AdsManager().showRewardedAds(this, ORDER_ADMOB) { isRewarded ->
            Log.d(LOG, "Show Rewarded Callback Function : $isRewarded")
        }
    }

    fun initAdmobBanner(view: View) {
        Log.d(LOG, "initAdmobBanner")
        lyViewBannerAds?.removeAllViews()
        AdsManager().initBanner(this, lyViewBannerAds!!, ORDER_ADMOB)
    }

    fun initAdmobNative(view: View) {
        Log.d(LOG, "initAdmobNative")
        lyViewNativeAds?.removeAllViews()
        AdsManager().initNative(this, lyViewNativeAds!!, ORDER_ADMOB)
    }

    fun initAdmobNativeHome(view: View) {
        Log.d(LOG, "initAdmobNative")
        lyViewNativeAds?.removeAllViews()
        AdsManager().initNative(this, lyViewNativeAds!!, ORDER_ADMOB, "home")
    }

    fun initAdmobNativeDetail(view: View) {
        Log.d(LOG, "initAdmobNative")
        lyViewNativeAds?.removeAllViews()
        AdsManager().initNative(this, lyViewNativeAds!!, ORDER_ADMOB, "detail")
    }

    fun initFacebookNative(view: View) {
        Log.d(LOG, "initFacebookNative")
        lyViewNativeAds?.removeAllViews()
        FacebookManager().initFacebookNative(this, lyViewNativeAds!!, ORDER_FACEBOOK)
    }

    fun initFacebookNativeHome(view: View) {
        Log.d(LOG, "initFacebookNative")
        lyViewNativeAds?.removeAllViews()
        FacebookManager().initFacebookNative(this, lyViewNativeAds!!, ORDER_FACEBOOK, "home")
    }

    fun initFacebookNativeDetail(view: View) {
        Log.d(LOG, "initFacebookNative")
        lyViewNativeAds?.removeAllViews()
        FacebookManager().initFacebookNative(this, lyViewNativeAds!!, ORDER_FACEBOOK, "detail")
    }

    fun initFacebookBanner(view: View) {
        Log.d(LOG, "initFacebookBanner")
        lyViewBannerAds?.removeAllViews()
        FacebookManager().initFacebookBanner(this, lyViewBannerAds!!, ORDER_FACEBOOK)
    }

    fun showInterstitialFacebook(view: View) {
        Log.d(LOG, "showInterstitialFacebook")
        FacebookManager().showInterstitialFacebook(this, ORDER_FACEBOOK)
    }

    fun showRewardedFacebook(view: View) {
        Log.d(LOG, "showRewardedFacebook")
        FacebookManager().showRewardedFacebook(this, ORDER_FACEBOOK) { isRewarded -> Log.d(LOG, "isRewarded : $isRewarded") }
    }

    fun initUnityBanner(view: View) {
        Log.d(LOG, "initUnityBanner")
        lyViewBannerAds?.removeAllViews()
        UnityManager().initUnityBanner(this, lyViewBannerAds!!, ORDER_UNITY)
    }

    fun showInterstitialUnity(view: View) {
        Log.d(LOG, "showInterstitialUnity")
        UnityManager().showInterstitialUnity(this, ORDER_UNITY)
    }

    fun showRewardedUnity(view: View) {
        Log.d(LOG, "showRewardedUnity")
        UnityManager().showRewardedUnity(this, ORDER_UNITY) { isRewarded -> Log.d(LOG, "isRewarded : $isRewarded") }
    }

    fun initAppLovinNative(view: View) {
        Log.d(LOG, "initAppLovinNative")
        lyViewNativeAds?.removeAllViews()
        AppLovinManager().initAppLovinNative(this, lyViewNativeAds!!, ORDER_APPLOVIN, "home")
    }

    fun initAppLovinBanner(view: View) {
        Log.d(LOG, "initAppLovinBanner")
        lyViewBannerAds?.removeAllViews()
        AppLovinManager().initAppLovinBanner(this, lyViewBannerAds!!, ORDER_APPLOVIN)
    }

    fun showInterstitialAppLovin(view: View) {
        Log.d(LOG, "showInterstitialAppLovin")
        AppLovinManager().showInterstitialAppLovin(this, ORDER_APPLOVIN)
    }

    fun showRewardedAppLovin(view: View) {
        Log.d(LOG, "showRewardedAppLovin")
        AppLovinManager().showRewardedAppLovin(this, ORDER_APPLOVIN) { isRewarded -> Log.d(LOG, "isRewarded : $isRewarded") }
    }

    fun showOpenAdsAppLovin(view: View) {
        Log.d(LOG, "showRewardedAppLovin")
        AppLovinManager().showOpenAdsAppLovin(this) {
            Log.d(LOG, "Show AppLovin OpenAds Callback Function")
        }
    }
}