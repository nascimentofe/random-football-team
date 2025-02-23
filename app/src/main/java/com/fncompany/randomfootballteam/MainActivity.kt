package com.fncompany.randomfootballteam

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.fncompany.randomfootballteam.ads.GoogleMobileAdsConsentManager
import com.fncompany.randomfootballteam.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds

class MainActivity : Activity() {

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
        setContentView(binding.root)
        setupButtons()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    private fun setupButtons() = with(binding) {
        btnSettings.isVisible = googleMobileAdsConsentManager.isPrivacyOptionsRequired
        btnSettings.setOnClickListener {
            googleMobileAdsConsentManager.showPrivacyOptionsForm(this@MainActivity) { formError ->
                if (formError != null) {
                    Toast.makeText(this@MainActivity, formError.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnInspector.setOnClickListener {
            MobileAds.openAdInspector(this@MainActivity) { error ->
                // Error will be non-null if ad inspector closed due to an error.
                error?.let { Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show() }
            }
        }
    }
}