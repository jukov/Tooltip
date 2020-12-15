package com.github.jukov.tooltip.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BindingFragment<VB: ViewBinding>: Fragment() {

    private lateinit var bindingInternal: VB
    protected val binding: VB
        get() = bindingInternal

    abstract fun onCreateDataBinding(): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingInternal = onCreateDataBinding()
        return bindingInternal.root
    }
}
