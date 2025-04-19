package com.ugikpoenya.adsmanager.ads

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.ugikpoenya.adsmanager.R


fun populateAdmobNative(nativeAd: NativeAd, adView: NativeAdView) {
    // You must call destroy on old ads when you are done with them,
    // otherwise you will have a memory leak.

    // Set the media view. Media content will be automatically populated in the media view once
    // adView.setNativeAd() is called.
    adView.mediaView = adView.findViewById(R.id.media_view)

    // Set other ad assets.
    adView.headlineView = adView.findViewById(R.id.primary)
    adView.bodyView = adView.findViewById(R.id.body)
    adView.callToActionView = adView.findViewById(R.id.cta)
    adView.iconView = adView.findViewById(R.id.icon)
    adView.starRatingView = adView.findViewById(R.id.rating_bar)
    adView.advertiserView = adView.findViewById(R.id.secondary)

    // The headline is guaranteed to be in every UnifiedNativeAd.
    (adView.headlineView as TextView).text = nativeAd.headline

    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.INVISIBLE
    } else {
        if (adView.bodyView !== null) {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as Button).text = nativeAd.callToAction
    }

    if (nativeAd.icon == null) {
        adView.iconView?.visibility = View.GONE
    } else {
        (adView.iconView as ImageView).setImageDrawable(
            nativeAd.icon?.drawable
        )
        adView.iconView?.visibility = View.VISIBLE
    }

    if (nativeAd.starRating == null) {
        adView.starRatingView?.visibility = View.INVISIBLE
    } else {
        (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
        adView.starRatingView?.visibility = View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        adView.advertiserView?.visibility = View.INVISIBLE
    } else {
        (adView.advertiserView as TextView).text = nativeAd.advertiser
        adView.advertiserView?.visibility = View.VISIBLE
    }

    // This method tells the Google Mobile Ads SDK that you have finished populating your
    // native ad view with this native ad. The SDK will populate the adView's MediaView
    // with the media content from this native ad.
    adView.setNativeAd(nativeAd)
}