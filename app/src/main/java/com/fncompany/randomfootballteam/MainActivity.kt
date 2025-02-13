package com.fncompany.randomfootballteam

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fncompany.randomfootballteam.ads.GoogleMobileAdsConsentManager
import com.google.android.gms.ads.MobileAds


class MainActivity : ComponentActivity() {

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Override the default implementation when the user presses the back key.
        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        googleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(applicationContext)

        setContent {
            MainScreen(googleMobileAdsConsentManager)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(googleMobileAdsConsentManager: GoogleMobileAdsConsentManager) {
    val context = LocalContext.current
    val activity = LocalActivity.current!!
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Main activity") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            actions = {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        bitmap = ImageBitmap(100,100),
                        contentDescription = "More"
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                        DropdownMenuItem(text = { Text("Configurações") },
                            onClick = {
                                googleMobileAdsConsentManager.showPrivacyOptionsForm(activity) { formError ->
                                    if (formError != null) {
                                        Toast.makeText(
                                            context, formError.message, Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                showMenu = false
                            })
                    }
                    DropdownMenuItem(text = { Text("Ads Inspector") },
                        onClick = {
                            MobileAds.openAdInspector(context) { error ->
                                error?.let {
                                    Log.d(App.LOG_TAG, it.message)
                                }
                            }
                            showMenu = false
                        })
                }
            })
    }) { innerPadding ->
        MainContent(Modifier.padding(innerPadding))
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Main activity",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Main activity",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}