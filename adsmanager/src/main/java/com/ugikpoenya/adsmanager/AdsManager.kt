package com.ugikpoenya.adsmanager

import android.content.Context
import android.util.Log
import android.widget.RelativeLayout
import com.ugikpoenya.adsmanager.ads.AdmobManager
import com.ugikpoenya.adsmanager.ads.AppLovinManager
import com.ugikpoenya.adsmanager.ads.FacebookManager
import com.ugikpoenya.adsmanager.ads.UnityManager
import com.ugikpoenya.servermanager.ServerManager
import com.ugikpoenya.servermanager.ServerPrefs
import androidx.core.view.isEmpty
import com.ugikpoenya.servermanager.model.ItemModel

var globalItemModel = ItemModel()
var ORDER_ADMOB: Int = 0
var ORDER_FACEBOOK: Int = 1
var ORDER_UNITY: Int = 2
var ORDER_APPLOVIN: Int = 3

class AdsManager {
    val LOG = "LOG_ADS_MANAGER"

    fun initAds(context: Context, function: () -> (Unit)) {
        Log.d(LOG, "Ads Manager initAds")
        val itemModel = ServerPrefs(context).getItemModel()
        if (itemModel != null) {
            Log.d(LOG, "GlobalItemModel init")
            globalItemModel = itemModel
        }
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
        var pageBanner = ServerManager().getItemKey(context, PAGE + "_banner")

        if ((pageBanner !== "false")) {
            if (view.isEmpty()) {
                val itemModel = ServerPrefs(context).getItemModel()
                var priority = ServerManager().getItemKey(context, PAGE + "_priority")
                if (priority.isNullOrEmpty()) priority = itemModel?.DEFAULT_PRIORITY
                Log.d(LOG, "initBanner $ORDER $PAGE $priority")

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
        var pageNative = ServerManager().getItemKey(context, PAGE + "_native")

        if (pageNative !== "false") {
            if (view.isEmpty()) {
                val itemModel = ServerPrefs(context).getItemModel()
                var priority = ServerManager().getItemKey(context, PAGE + "_priority")
                if (priority.isNullOrEmpty()) priority = itemModel?.DEFAULT_PRIORITY
                Log.d(LOG, "initNative $ORDER $PAGE $priority")

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

    fun interstitielSuccessfullyDisplayed(context: Context) {
        Log.d(LOG, "interstitielSuccessfullyDisplayed")
        globalItemModel.interstitial_interval_counter = globalItemModel.interstitial_interval
        globalItemModel.interstitial_last_shown_time = System.currentTimeMillis()
    }

    fun isInterstitielAllowedReadyShow(context: Context): Boolean {
        if (globalItemModel.interstitial_interval_counter > 0) {
            Log.d(LOG, "Disable Show Interstitial intervalCounter " + globalItemModel.interstitial_interval_counter)
            globalItemModel.interstitial_interval_counter--
            return false
        }

        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val interstitial_delay_first = globalItemModel.interstitial_delay_first * 1000L // 30 detik
        val interstitial_delay = globalItemModel.interstitial_delay * 1000L // 30 detik
        val installTime = packageInfo.firstInstallTime

        val currentTime = System.currentTimeMillis()
        val durationInstalTime = currentTime - installTime
        val durationLastTime = currentTime - globalItemModel.interstitial_last_shown_time

        Log.d(
            LOG, "=====Interstitial===================" +
                    "\nInstallTime : $installTime " +
                    "\ncurrentTime : $currentTime " +
                    "\nlastAdShown : " + globalItemModel.interstitial_last_shown_time +
                    "\n=====Duration===================" +

                    "\ndurationInstalTime : $durationInstalTime " +
                    "\ndurationLastTime   : $durationLastTime " +
                    "\n=====Delay===================" +
                    "\ndelay_first : $interstitial_delay_first " +
                    "\ndelay       : $interstitial_delay"
        )

        if (durationInstalTime < interstitial_delay_first) {
            Log.d(LOG, "Disable Show Interstitial kurang dari durasi instal time")
            return false
        }

        if (durationLastTime < interstitial_delay) {
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
    }

    fun showRewardedAds(context: Context, ORDER: Int = 0, callbackFunction: ((isRewarded: Boolean) -> Unit)) {
        val itemModel = ServerPrefs(context).getItemModel()
        Log.d("LOG", "Show  RewardedAds $ORDER")
        var priority: String? = itemModel?.interstitial_priority
        if (priority.isNullOrEmpty()) priority = itemModel?.DEFAULT_PRIORITY
        val array = priority?.split(",")?.map { it.toInt() }
        if (array != null && array.contains(ORDER)) {
            when {
                array[ORDER] == ORDER_ADMOB -> AdmobManager().showRewardedAdmob(context, ORDER + 1, callbackFunction)
                array[ORDER] == ORDER_FACEBOOK -> FacebookManager().showRewardedFacebook(context, ORDER + 1, callbackFunction)
                array[ORDER] == ORDER_UNITY -> UnityManager().showRewardedUnity(context, ORDER + 1, callbackFunction)
                array[ORDER] == ORDER_APPLOVIN -> AppLovinManager().showRewardedAppLovin(context, ORDER + 1, callbackFunction)
                else -> showRewardedAds(context, ORDER + 1, callbackFunction)
            }
        } else {
            Log.d("LOG", "All rewarded null")
            callbackFunction(false)
        }
    }

}