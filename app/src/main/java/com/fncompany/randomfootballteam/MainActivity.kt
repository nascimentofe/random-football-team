package com.fncompany.randomfootballteam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.fncompany.randomfootballteam.commons.ads.GoogleMobileAdsConsentManager
import com.fncompany.randomfootballteam.databinding.ActivityMainBinding
import com.fncompany.randomfootballteam.ui.camera.CameraFragment
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.android.inject


class MainActivity : FragmentActivity() {

    private val googleMobileAdsConsentManager by inject<GoogleMobileAdsConsentManager>()
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButtons()
        setupOnBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
    }

    private fun setupOnBackPressed() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
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

        btnCamera.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.main_container, CameraFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}