package com.fncompany.randomfootballteam.features.home

import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.fncompany.randomfootballteam.R
import com.fncompany.randomfootballteam.commons.ads.GoogleMobileAdsConsentManager
import com.fncompany.randomfootballteam.commons.base.BaseFragment
import com.fncompany.randomfootballteam.commons.utils.Binding
import com.fncompany.randomfootballteam.databinding.FragmentHomeBinding
import org.koin.android.ext.android.inject

@Binding(FragmentHomeBinding::class)
class HomeFragment: BaseFragment<FragmentHomeBinding>() {

    private val googleMobileAdsConsentManager by inject<GoogleMobileAdsConsentManager>()

    override fun setup() {
        setupButtons()
    }

    private fun setupButtons() = with(binding) {
        btnSettings.isVisible = googleMobileAdsConsentManager.isPrivacyOptionsRequired
        btnSettings.setOnClickListener {
            googleMobileAdsConsentManager.showPrivacyOptionsForm(requireActivity()) { formError ->
                if (formError != null) {
                    Toast.makeText(requireActivity(), formError.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnInspector.setOnClickListener {
            com.google.android.gms.ads.MobileAds.openAdInspector(requireActivity()) { error ->
                // Error will be non-null if ad inspector closed due to an error.
                error?.let { Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show() }
            }
        }

        btnCamera.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_camera_fragment)
        }
    }
}