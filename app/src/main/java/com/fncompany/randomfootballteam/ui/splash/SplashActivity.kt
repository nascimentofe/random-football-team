package com.fncompany.randomfootballteam.ui.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import com.fncompany.randomfootballteam.App
import com.fncompany.randomfootballteam.MainActivity
import com.fncompany.randomfootballteam.R
import com.fncompany.randomfootballteam.ads.GoogleMobileAdsConsentManager
import com.fncompany.randomfootballteam.ads.OnShowAdCompleteListener
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SplashActivity : Activity() {

  private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
  private val isMobileAdsInitializeCalled = AtomicBoolean(false)
  private val gatherConsentFinished = AtomicBoolean(false)
  private var secondsRemaining: Long = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)

    // Log the Mobile Ads SDK version.
    Log.d(LOG_TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

    // Create a timer so the SplashActivity will be displayed for a fixed amount of time.
    createTimer()

    googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
    googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
      if (consentError != null) {
        // Consent not obtained in current session.
        Log.w(LOG_TAG, String.format("%s: %s", consentError.errorCode, consentError.message))
      }

      gatherConsentFinished.set(true)

      if (googleMobileAdsConsentManager.canRequestAds) {
        initializeMobileAdsSdk()
      }

      if (secondsRemaining <= 0) {
        startMainActivity()
      }
    }

    // This sample attempts to load ads using consent obtained in the previous session.
    if (googleMobileAdsConsentManager.canRequestAds) {
      initializeMobileAdsSdk()
    }
  }

  /**
   * Create the countdown timer, which counts down to zero and show the app open ad.
   *
   * @param time the number of milliseconds that the timer counts down from
   */
  private fun createTimer() {
    val counterTextView: TextView = findViewById(R.id.timer)
    val countDownTimer: CountDownTimer =
      object : CountDownTimer(COUNTER_TIME_MILLISECONDS, 1000) {
        override fun onTick(millisUntilFinished: Long) {
          secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
          counterTextView.text = "App is done loading in: $secondsRemaining"
        }

        override fun onFinish() {
          secondsRemaining = 0
          counterTextView.text = "Done."

          (application as App).appOpenAdManager.showAdIfAvailable(
            activity = this@SplashActivity,
            onShowAdCompleteListener = object : OnShowAdCompleteListener {
              override fun onShowAdComplete() {
                // Check if the consent form is currently on screen before moving to the main
                // activity.
                if (gatherConsentFinished.get()) {
                  startMainActivity()
                }
              }
            }
          )
        }
      }
    countDownTimer.start()
  }

  private fun initializeMobileAdsSdk() {
    if (isMobileAdsInitializeCalled.getAndSet(true)) {
      return
    }

    // Set your test devices.
    MobileAds.setRequestConfiguration(
      RequestConfiguration.Builder()
        .setTestDeviceIds(listOf(App.TEST_DEVICE_HASHED_ID))
        .build()
    )

    CoroutineScope(Dispatchers.IO).launch {
      // Initialize the Google Mobile Ads SDK on a background thread.
      MobileAds.initialize(this@SplashActivity) {}
      runOnUiThread {
        // Load an ad on the main thread.
        (application as App).appOpenAdManager.loadAd(this@SplashActivity)
      }
    }

    // Load an ad.
  }

  /** Start the MainActivity. */
  fun startMainActivity() {
    finishActivity(0)
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
  }

  companion object {
    // Number of milliseconds to count down before showing the app open ad. This simulates the time
    // needed to load the app.
    private const val COUNTER_TIME_MILLISECONDS = 5000L

    private const val LOG_TAG = "SplashActivity"
  }
}
