package com.ugikpoenya.adsmanager

import android.content.Context
import android.util.Log
import android.widget.RelativeLayout
import com.ugikpoenya.adsmanager.ads.AdmobManager
import com.ugikpoenya.adsmanager.ads.AppLovinManager
import com.ugikpoenya.adsmanager.ads.FacebookManager
import com.ugikpoenya.adsmanager.ads.UnityManager
import com.ugikpoenya.servermanager.ServerPrefs

var intervalCounter = 0
var ORDER_ADMOB: Int = 0
var ORDER_FACEBOOK: Int = 1
var ORDER_UNITY: Int = 2
var ORDER_APPLOVIN: Int = 3

class AdsManager {
    val LOG = "LOG_ADS_MANAGER"
    fun initAds(context: Context, function: () -> (Unit)) {
        val itemModel = ServerPrefs(context).getItemModel()
        Log.d(LOG, "Ads Manager initAds")
        if (itemModel !== null && itemModel.admob_gdpr) {
            AdmobManager().initGdpr(context, function)
        } else {
            AdmobManager().initAdmobAds(context, function)
        }
        FacebookManager().initFacebookAds(context)
        UnityManager().initUnityAds(context)
        AppLovinManager().initAppLovinAds(context)
    }

    fun showOpenAds(context: Context, function: (() -> Unit)? = null) {
        Log.d(LOG, "showOpenAds")
        AdmobManager().showOpenAdsAdmob(context, function)
    }

    fun initBanner(context: Context, view: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        val itemModel = ServerPrefs(context).getItemModel()
        var bannerView: Boolean? = true
        if (PAGE.lowercase().trim() == "home") bannerView = itemModel?.home_banner
        if (PAGE.lowercase().trim() == "detail") bannerView = itemModel?.detail_banner
        if (bannerView !== null && bannerView) {
            if (view.childCount == 0) {
                Log.d(LOG, "initBanner $ORDER $PAGE")
                var priority: String? = ""
                if (PAGE.lowercase().trim() == "home") priority = itemModel?.home_priority
                if (PAGE.lowercase().trim() == "detail") priority = itemModel?.detail_priority
                if (priority.isNullOrEmpty()) priority = itemModel?.DEFAULT_PRIORITY

                val array = priority?.split(",")?.map { it.toInt() }
                if (array !== null && array.contains(ORDER)) {
                    when {
                        array[ORDER] == ORDER_ADMOB -> AdmobManager().initAdmobBanner(context, view, ORDER + 1, PAGE)
                        array[ORDER] == ORDER_FACEBOOK -> FacebookManager().initFacebookBanner(context, view, ORDER + 1, PAGE)
                        array[ORDER] == ORDER_UNITY -> UnityManager().initUnityBanner(context, view, ORDER + 1, PAGE)
                        array[ORDER] == ORDER_APPLOVIN -> AppLovinManager().initAppLovinBanner(context, view, ORDER + 1, PAGE)
                        else -> initBanner(context, view, ORDER + 1, PAGE)
                    }
                }
            }
        } else {
            Log.d(LOG, "Banner $PAGE disable")
        }
    }

    fun initNative(context: Context, view: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        val itemModel = ServerPrefs(context).getItemModel()
        var nativeView: Boolean? = true
        if (PAGE.lowercase().trim() == "home") nativeView = itemModel?.home_native
        if (PAGE.lowercase().trim() == "detail") nativeView = itemModel?.detail_native

        if (nativeView !== null && nativeView) {
            if (view.childCount == 0) {
                Log.d(LOG, "initNative $ORDER $PAGE")
                var priority: String? = ""
                if (PAGE.lowercase().trim() == "home") priority = itemModel?.home_priority
                if (PAGE.lowercase().trim() == "detail") priority = itemModel?.detail_priority
                if (priority.isNullOrEmpty()) priority = itemModel?.DEFAULT_PRIORITY

                val array = priority?.split(",")?.map { it.toInt() }
                if (array != null && array.contains(ORDER)) {
                    when {
                        array[ORDER] == ORDER_ADMOB -> AdmobManager().initAdmobNative(context, view, ORDER + 1, PAGE)
                        array[ORDER] == ORDER_FACEBOOK -> FacebookManager().initFacebookNative(context, view, ORDER + 1, PAGE)
                        array[ORDER] == ORDER_APPLOVIN -> AppLovinManager().initAppLovinNative(context, view, ORDER + 1, PAGE)
                        else -> initNative(context, view, ORDER + 1, PAGE)
                    }
                }
            }
        } else {
            Log.d(LOG, "Native $PAGE disable")
        }
    }

    fun showInterstitial(context: Context, ORDER: Int = 0) {
        val itemModel = ServerPrefs(context).getItemModel()
        Log.d(LOG, "Show Interstitial $ORDER intervalCounter $intervalCounter")
        if (intervalCounter <= 0) {
            var priority: String? = itemModel?.interstitial_priority
            if (priority.isNullOrEmpty()) priority = itemModel?.DEFAULT_PRIORITY
            val array = priority?.split(",")?.map { it.toInt() }
            if (array != null && array.contains(ORDER)) {
                when {
                    array[ORDER] == ORDER_ADMOB -> AdmobManager().showInterstitialAdmob(context, ORDER + 1)
                    array[ORDER] == ORDER_FACEBOOK -> FacebookManager().showInterstitialFacebook(context, ORDER + 1)
                    array[ORDER] == ORDER_UNITY -> UnityManager().showInterstitialUnity(context, ORDER + 1)
                    array[ORDER] == ORDER_APPLOVIN -> AppLovinManager().showInterstitialAppLovin(context, ORDER + 1)
                    else -> showInterstitial(context, ORDER + 1)
                }
            }
        } else {
            intervalCounter--
        }
    }

    fun showRewardedAds(context: Context, ORDER: Int = 0) {
        val itemModel = ServerPrefs(context).getItemModel()
        Log.d("LOG", "Show  RewardedAds $ORDER")
        var priority: String? = itemModel?.interstitial_priority
        if (priority.isNullOrEmpty()) priority = itemModel?.DEFAULT_PRIORITY
        val array = priority?.split(",")?.map { it.toInt() }
        if (array != null && array.contains(ORDER)) {
            when {
                array[ORDER] == ORDER_ADMOB -> AdmobManager().showRewardedAdmob(context, ORDER + 1)
                array[ORDER] == ORDER_FACEBOOK -> FacebookManager().showRewardedFacebook(context, ORDER + 1)
                array[ORDER] == ORDER_UNITY -> UnityManager().showRewardedUnity(context, ORDER + 1)
                array[ORDER] == ORDER_APPLOVIN -> AppLovinManager().showRewardedAppLovin(context, ORDER + 1)
                else -> showRewardedAds(context, ORDER + 1)
            }
        } else {
            Log.d("LOG", "All rewarded null")
            showInterstitial(context, 0)
        }
    }

}