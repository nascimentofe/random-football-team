package com.fncompany.randomfootballteam

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.fncompany.randomfootballteam.ads.GoogleMobileAdsConsentManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.Date

class App: MultiDexApplication(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        registerActivityLifecycleCallbacks(this@App)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this@App)
        appOpenAdManager = AppOpenAdManager()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            // Show the ad (if available) when the app moves to foreground.
            appOpenAdManager.showAdIfAvailable(it)
        }
    }

    inner class AppOpenAdManager {

        private var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(applicationContext)
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
        private var loadTime: Long = 0

        /**
         * Load an ad.
         *
         * @param context the context of the activity that loads the ad
         */
        fun loadAd(context: Context) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                AD_UNIT_ID,
                request,
                object : AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        Log.d(LOG_TAG, "onAdLoaded.")
                        Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show()
                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                        Log.d(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
                        Toast.makeText(context, "onAdFailedToLoad", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }

        /** Check if ad was loaded more than n hours ago. */
        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        /** Check if ad exists and can be shown. */
        private fun isAdAvailable(): Boolean {
            // Ad references in the app open beta will time out after four hours, but this time limit
            // may change in future beta versions. For details, see:
            // https://support.google.com/admob/answer/9341964?hl=en
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         */
        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(
                activity = activity,
                onShowAdCompleteListener = {
                    // Empty because the user will go back to the activity that shows the ad.
                }
            )
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
         */
        fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: () -> Unit) {
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.")
                return
            }

            // If the app open ad is not available yet, invoke the callback.
            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.")
                onShowAdCompleteListener.invoke()
                if (googleMobileAdsConsentManager.canRequestAds) {
                    loadAd(activity)
                }
                return
            }

            Log.d(LOG_TAG, "Will show ad.")

            appOpenAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    /** Called when full screen content is dismissed. */
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        Log.d(LOG_TAG, "onAdDismissedFullScreenContent.")
                        Toast.makeText(activity, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
                            .show()

                        onShowAdCompleteListener.invoke()
                        if (googleMobileAdsConsentManager.canRequestAds) {
                            loadAd(activity)
                        }
                    }

                    /** Called when fullscreen content failed to show. */
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAd = false
                        Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.message)
                        Toast.makeText(
                            activity,
                            "onAdFailedToShowFullScreenContent",
                            Toast.LENGTH_SHORT
                        ).show()

                        onShowAdCompleteListener.invoke()
                        if (googleMobileAdsConsentManager.canRequestAds) {
                            loadAd(activity)
                        }
                    }

                    /** Called when fullscreen content is shown. */
                    override fun onAdShowedFullScreenContent() {
                        Log.d(LOG_TAG, "onAdShowedFullScreenContent.")
                        Toast.makeText(activity, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            isShowingAd = true
            appOpenAd?.show(activity)
        }
    }


    override fun onActivityStarted(activity: Activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        // This is an ad unit ID for a test ad. Replace with your own app open ad unit ID.
        const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
        const val LOG_TAG = "Random Football Team#TAG"

        // Check your logcat output for the test device hashed ID e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device" or
        // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345") to set this as
        // a debug device".
        const val TEST_DEVICE_HASHED_ID = "ABCDEF012345"
    }
}