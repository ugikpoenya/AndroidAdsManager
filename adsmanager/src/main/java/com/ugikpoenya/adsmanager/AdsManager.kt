package com.ugikpoenya.adsmanager

import android.content.Context
import android.util.Log
import android.widget.RelativeLayout
import com.ugikpoenya.adsmanager.ads.AdmobManager
import com.ugikpoenya.adsmanager.ads.AppLovinManager
import com.ugikpoenya.adsmanager.ads.FacebookManager
import com.ugikpoenya.adsmanager.ads.UnityManager
import com.ugikpoenya.servermanager.ServerPrefs
import com.ugikpoenya.servermanager.model.ItemModel

var globalItemModel = ItemModel()
var ORDER_ADMOB: Int = 0
var ORDER_FACEBOOK: Int = 1
var ORDER_UNITY: Int = 2
var ORDER_APPLOVIN: Int = 3
var interstitialFirstCount = true

class AdsManager {
    val LOG = "LOG_ADS_MANAGER"

    fun initAds(context: Context, callbackFunction: () -> (Unit)) {
        Log.d(LOG, "Ads Manager initAds")
        val itemModel = ServerPrefs(context).getItemModel()
        if (itemModel != null) {
            Log.d(LOG, "GlobalItemModel init")
            globalItemModel = itemModel
            globalItemModel.open_ads_last_shown_time = ServerPrefs(context).open_ads_last_shown_time
            globalItemModel.interstitial_last_shown_time = ServerPrefs(context).interstitial_last_shown_time
            globalItemModel.rewarded_ads_last_shown_time = ServerPrefs(context).rewarded_ads_last_shown_time
        }
        if (itemModel !== null && itemModel.admob_gdpr) {
            AdmobManager().initGdpr(context, callbackFunction)
        } else {
            AdmobManager().initAdmobAds(context, callbackFunction)
        }
        FacebookManager().initFacebookAds(context)
        UnityManager().initUnityAds(context)
        AppLovinManager().initAppLovinAds(context)
    }

    fun showOpenAds(context: Context, ORDER: Int = 0, callbackFunction: (() -> Unit)) {
        if (!isOpenAdsAllowedReadyShow(context)) return callbackFunction()

        Log.d(LOG, "Show showOpenAds $ORDER")
        val priorityList = globalItemModel.DEFAULT_PRIORITY
            .split(",")
            .mapNotNull { it.toIntOrNull() }

        if (ORDER >= priorityList.size) {
            Log.d(LOG, "All showOpenAds null")
            return callbackFunction()
        }

        val nextOrder = ORDER + 1
        when (priorityList[ORDER]) {
            ORDER_ADMOB -> AdmobManager().showOpenAdsAdmob(context, nextOrder, callbackFunction)
            ORDER_APPLOVIN -> AppLovinManager().showOpenAdsAppLovin(context, nextOrder, callbackFunction)
            else -> showOpenAds(context, nextOrder, callbackFunction)
        }
    }

    fun OpenAdsSuccessfullyDisplayed(context: Context) {
        Log.d(LOG, "OpenAdsSuccessfullyDisplayed")
        globalItemModel.open_ads_last_shown_time = System.currentTimeMillis()
        ServerPrefs(context).open_ads_last_shown_time = globalItemModel.open_ads_last_shown_time
    }

    fun isOpenAdsAllowedReadyShow(context: Context): Boolean {
        val currentTime = System.currentTimeMillis()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val installTime = packageInfo.firstInstallTime

        val delayFirst = globalItemModel.open_ads_delay_first * 1000L
        val delay = globalItemModel.open_ads_delay * 1000L
        val lastShown = globalItemModel.open_ads_last_shown_time

        val durationInstall = currentTime - installTime
        val durationSinceLast = currentTime - lastShown

        Log.d(
            LOG, """
        =====OpenAds===================
        InstallTime        : $installTime
        CurrentTime        : $currentTime
        LastAdShown        : $lastShown
        =====Duration=======================
        DurationInstall    : ${durationInstall / 1000}s
        DurationSinceLast  : ${durationSinceLast / 1000}s
        =====Delay=========================
        Delay First        : ${delayFirst / 1000}s
        Delay              : ${delay / 1000}s
    """.trimIndent()
        )


        if (durationInstall < delayFirst) {
            Log.d(LOG, "Disable Show OpenAds kurang dari durasi instal time")
            return false
        }

        if (durationSinceLast < delay) {
            Log.d(LOG, "Disable Show OpenAds kurang dari durasi last time")
            return false
        }
        return true
    }

    fun initBanner(context: Context, view: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        var pageBanner = ServerPrefs(context).getItemKey(PAGE + "_banner")
        Log.d(LOG, PAGE + "_banner : " + pageBanner)

        if (pageBanner.toString() != "false") {
            if (view.childCount == 0) {
                var priority = ServerPrefs(context).getItemKey(PAGE + "_priority")
                if (priority.isNullOrEmpty()) priority = globalItemModel.DEFAULT_PRIORITY
                val priorityList = priority.split(",")
                    .mapNotNull { it.toIntOrNull() }

                if (ORDER >= priorityList.size) {
                    Log.d(LOG, "All Banner null")
                    return // Berhenti untuk menghindari rekursi tak terhingga
                }

                Log.d(LOG, "initBanner $ORDER $PAGE $priority")
                val nextOrder = ORDER + 1
                when (priorityList[ORDER]) {
                    ORDER_ADMOB -> AdmobManager().initAdmobBanner(context, view, nextOrder, PAGE)
                    ORDER_FACEBOOK -> FacebookManager().initFacebookBanner(context, view, nextOrder, PAGE)
                    ORDER_UNITY -> UnityManager().initUnityBanner(context, view, nextOrder, PAGE)
                    ORDER_APPLOVIN -> AppLovinManager().initAppLovinBanner(context, view, nextOrder, PAGE)
                    else -> initBanner(context, view, nextOrder, PAGE)
                }
            }
        } else {
            Log.d(LOG, "Banner $PAGE disable")
        }

    }

    fun initNative(context: Context, view: RelativeLayout, ORDER: Int = 0, PAGE: String = "") {
        var pageNative = ServerPrefs(context).getItemKey(PAGE + "_native")
        Log.d(LOG, PAGE + "_native : " + pageNative)

        if (pageNative != "false") {
            if (view.childCount == 0) {
                var priority = ServerPrefs(context).getItemKey(PAGE + "_priority")
                if (priority.isNullOrEmpty()) priority = globalItemModel.DEFAULT_PRIORITY
                Log.d(LOG, "initNative $ORDER $PAGE $priority")

                val priorityList = priority.split(",")
                    .mapNotNull { it.toIntOrNull() }

                if (ORDER >= priorityList.size) {
                    Log.d(LOG, "All Native null")
                    return // Berhenti untuk menghindari rekursi tak terhingga
                }

                val nextOrder = ORDER + 1
                when (priorityList[ORDER]) {
                    ORDER_ADMOB -> AdmobManager().initAdmobNative(context, view, nextOrder, PAGE)
                    ORDER_FACEBOOK -> FacebookManager().initFacebookNative(context, view, nextOrder, PAGE)
                    ORDER_APPLOVIN -> AppLovinManager().initAppLovinNative(context, view, nextOrder, PAGE)
                    else -> initNative(context, view, nextOrder, PAGE)
                }
            }
        } else {
            Log.d(LOG, "Native $PAGE disable")
        }
    }

    fun interstitielSuccessfullyDisplayed(context: Context) {
        Log.d(LOG, "interstitielSuccessfullyDisplayed")
        globalItemModel.interstitial_interval_counter = globalItemModel.interstitial_interval
        globalItemModel.interstitial_last_shown_time = System.currentTimeMillis()
    }

    fun isInterstitielAllowedReadyShow(context: Context): Boolean {
        if (interstitialFirstCount) {
            Log.d(LOG, "interstitialFirstCount")
            globalItemModel.interstitial_interval_counter = globalItemModel.interstitial_interval_first
            interstitialFirstCount = false
        }

        if (globalItemModel.interstitial_interval_counter > 0) {
            Log.d(LOG, "Disable Show Interstitial intervalCounter " + globalItemModel.interstitial_interval_counter)
            globalItemModel.interstitial_interval_counter--
            return false
        }

        val currentTime = System.currentTimeMillis()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val installTime = packageInfo.firstInstallTime

        val delayFirst = globalItemModel.interstitial_delay_first * 1000L
        val delay = globalItemModel.interstitial_delay * 1000L
        val lastShown = globalItemModel.interstitial_last_shown_time

        val durationInstall = currentTime - installTime
        val durationSinceLast = currentTime - lastShown

        Log.d(
            LOG, """
        =====Interstitial===================
        InstallTime        : $installTime
        CurrentTime        : $currentTime
        LastAdShown        : $lastShown
        =====Duration=======================
        DurationInstall    : ${durationInstall / 1000}s
        DurationSinceLast  : ${durationSinceLast / 1000}s
        =====Delay=========================
        Delay First        : ${delayFirst / 1000}s
        Delay              : ${delay / 1000}s
    """.trimIndent()
        )

        if (durationInstall < delayFirst) {
            Log.d(LOG, "Disable Show Interstitial kurang dari durasi instal time")
            return false
        }

        if (durationSinceLast < delay) {
            Log.d(LOG, "Disable Show Interstitial kurang dari durasi last time")
            return false
        }
        return true
    }

    fun showInterstitial(context: Context, ORDER: Int = 0) {
        if (!isInterstitielAllowedReadyShow(context)) return

        Log.d(LOG, "Show Interstitial $ORDER intervalCounter " + globalItemModel.interstitial_interval_counter)
        var priority: String? = globalItemModel.interstitial_priority
        if (priority.isNullOrEmpty()) priority = globalItemModel.DEFAULT_PRIORITY
        val priorityList = priority.split(",")
            .mapNotNull { it.toIntOrNull() }

        if (ORDER >= priorityList.size) {
            Log.d(LOG, "All Interstitial null")
            return // Berhenti untuk menghindari rekursi tak terhingga
        }

        val nextOrder = ORDER + 1
        when (priorityList[ORDER]) {
            ORDER_ADMOB -> AdmobManager().showInterstitialAdmob(context, nextOrder)
            ORDER_FACEBOOK -> FacebookManager().showInterstitialFacebook(context, nextOrder)
            ORDER_UNITY -> UnityManager().showInterstitialUnity(context, nextOrder)
            ORDER_APPLOVIN -> AppLovinManager().showInterstitialAppLovin(context, nextOrder)
            else -> showInterstitial(context, nextOrder)
        }
    }

    fun RewardedAdsSuccessfullyDisplayed(context: Context) {
        Log.d(LOG, "RewardedAdsSuccessfullyDisplayed")
        val now = System.currentTimeMillis()
        globalItemModel.rewarded_ads_last_shown_time = now
        ServerPrefs(context).rewarded_ads_last_shown_time = now
    }

    fun showRewardedAds(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        if (!isRewardedAdsAllowedReadyShow(context)) return

        Log.d(LOG, "Show RewardedAds $ORDER")
        val priorityList = globalItemModel.interstitial_priority
            .split(",")
            .mapNotNull { it.toIntOrNull() }

        if (ORDER >= priorityList.size) {
            Log.d(LOG, "All rewarded null")
            callbackFunction(false)
            return // Berhenti untuk menghindari rekursi tak terhingga
        }

        val nextOrder = ORDER + 1
        when (priorityList[ORDER]) {
            ORDER_ADMOB -> AdmobManager().showRewardedAdmob(context, nextOrder, callbackFunction)
            ORDER_FACEBOOK -> FacebookManager().showRewardedFacebook(context, nextOrder, callbackFunction)
            ORDER_UNITY -> UnityManager().showRewardedUnity(context, nextOrder, callbackFunction)
            ORDER_APPLOVIN -> AppLovinManager().showRewardedAppLovin(context, nextOrder, callbackFunction)
            else -> showRewardedAds(context, nextOrder, callbackFunction)
        }
    }


    fun isRewardedAdsAllowedReadyShow(context: Context): Boolean {
        if (globalItemModel.rewarded_ads_interval_counter > 0) {
            Log.d(LOG, "Disable Show RewardedAds intervalCounter ${globalItemModel.rewarded_ads_interval_counter}")
            globalItemModel.rewarded_ads_interval_counter--
            return false
        }

        val currentTime = System.currentTimeMillis()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val installTime = packageInfo.firstInstallTime

        val delayFirst = globalItemModel.rewarded_ads_delay_first * 1000L
        val delay = globalItemModel.rewarded_ads_delay * 1000L
        val lastShown = globalItemModel.rewarded_ads_last_shown_time

        val durationInstall = currentTime - installTime
        val durationSinceLast = currentTime - lastShown

        Log.d(
            LOG, """
        =====RewardedAds===================
        InstallTime        : $installTime
        CurrentTime        : $currentTime
        LastAdShown        : $lastShown
        =====Duration=======================
        DurationInstall    : ${durationInstall / 1000}s
        DurationSinceLast  : ${durationSinceLast / 1000}s
        =====Delay=========================
        Delay First        : ${delayFirst / 1000}s
        Delay              : ${delay / 1000}s
    """.trimIndent()
        )

        return when {
            durationInstall < delayFirst -> {
                Log.d(LOG, "Disable Show RewardedAds: belum cukup durasi install")
                false
            }

            durationSinceLast < delay -> {
                Log.d(LOG, "Disable Show RewardedAds: belum cukup durasi sejak terakhir ditampilkan")
                false
            }

            else -> true
        }
    }

}