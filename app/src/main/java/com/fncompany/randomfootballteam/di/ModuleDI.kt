package com.fncompany.randomfootballteam.di

import com.fncompany.randomfootballteam.commons.ads.GoogleMobileAdsConsentManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single {
        GoogleMobileAdsConsentManager(androidContext())
    }
}