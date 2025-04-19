package com.ugikpoenya.sampleapp.adsmanager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ugikpoenya.adsmanager.ads.AdmobManager
import com.ugikpoenya.servermanager.ServerPrefs

class MainActivity : AppCompatActivity() {
    val LOG = "LOG_ADS_MANAGER"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun admobResetGdpr(view: View) {
        Log.d(LOG, "admobResetGdpr")
        AdmobManager().resetGDPR()
    }

    fun showOpenAdsAdmob(view: View) {
        Log.d(LOG, "showInterstitialAdmob")
        AdmobManager().showOpenAdsAdmob(this)
    }

    fun showInterstitialAdmob(view: View) {
        Log.d(LOG, "showInterstitialAdmob")
        AdmobManager().showInterstitialAdmob(this, 0)
    }

    fun showRewardedAdmob(view: View) {
        Log.d(LOG, "showInterstitialAdmob")
        AdmobManager().showRewardedAdmob(this, 0)
    }

    fun initAdmobBanner(view: View) {
        Log.d(LOG, "showInterstitialAdmob")
        val lyViewAds = findViewById<RelativeLayout>(R.id.lyViewAds)
        lyViewAds.removeAllViews()
        AdmobManager().initAdmobBanner(this, lyViewAds, 0)
    }

    fun initAdmobNative(view: View) {
        Log.d(LOG, "showInterstitialAdmob")
        val lyViewAds = findViewById<RelativeLayout>(R.id.lyViewAds)
        lyViewAds.removeAllViews()
        AdmobManager().initAdmobNative(this, lyViewAds, 0, "home")
    }
}