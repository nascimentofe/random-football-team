package com.fncompany.randomfootballteam.features.camera

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.fncompany.randomfootballteam.commons.base.BaseFragment
import com.fncompany.randomfootballteam.commons.camerax.CameraManager
import com.fncompany.randomfootballteam.commons.utils.Binding
import com.fncompany.randomfootballteam.databinding.FragmentCameraBinding

@Binding(FragmentCameraBinding::class)
class CameraFragment : BaseFragment<FragmentCameraBinding>() {

    private lateinit var cameraManager: CameraManager
    private var isCameraPermissionGranted = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                isCameraPermissionGranted = true
                cameraManager.startCamera()
            } else {
                // voltar ao frag anterior
                Toast.makeText(
                    requireContext(),
                    "Forneça permissão de CAMERA para continuar",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun setup() {
        createCameraManager()
        checkCameraPermission()
    }

    private fun createCameraManager() {
        cameraManager = CameraManager(
            context = requireContext(),
            finderView = binding.previewViewFinder,
            lifecycleOwner = this,
            graphicOverlay = binding.graphicOverlayFinder
        )
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isCameraPermissionGranted = true
            cameraManager.startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}