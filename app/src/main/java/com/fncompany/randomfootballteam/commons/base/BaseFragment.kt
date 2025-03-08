package com.fncompany.randomfootballteam.commons.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.fncompany.randomfootballteam.commons.utils.Binding
import kotlin.reflect.full.findAnnotation

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    protected lateinit var binding: T
        private set

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = this::class.findAnnotation<Binding>()?.let {
        binding = it.bindType.java.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        ).invoke(null, inflater, container, false) as T
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    fun setStatusBarColor(@ColorRes color: Int) {
        activity?.window?.statusBarColor = requireContext().getColor(color)
    }

    fun setAdjustResize() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    fun setAdjustPan() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    open fun setupBackPressedListener(action: (() -> Unit)) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            action.invoke()
        }
    }

    abstract fun setup()
}