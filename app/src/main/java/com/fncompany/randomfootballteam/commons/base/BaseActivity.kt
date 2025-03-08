package com.fncompany.randomfootballteam.commons.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.fncompany.randomfootballteam.commons.utils.Binding
import com.fncompany.randomfootballteam.commons.utils.LanguageEnum
import java.util.Locale
import kotlin.reflect.full.findAnnotation

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureBinding()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)
        setup()
    }

    @Suppress("UNCHECKED_CAST")
    private fun configureBinding() {
        this::class.findAnnotation<Binding>()?.let {
            val inflateBind = it.bindType.java.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            binding = inflateBind.invoke(null, layoutInflater, null, false) as T
        }
    }

    fun setStatusBarColor(color: Int) {
        window.statusBarColor = color
    }

    fun setFullscreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun fitBackgroundToWindow(fitWindow: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, fitWindow)
    }

    @Suppress("DEPRECATION")
    protected fun setLanguage(langEnum: LanguageEnum) {
        val defaultLocale = Locale(langEnum.name.lowercase(), langEnum.country)

        val config = resources.configuration
        val locale = Locale(defaultLocale.language)
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    abstract fun setup()
}