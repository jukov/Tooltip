package com.github.jukov.tooltip

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BindingActivity<VB: ViewBinding> : AppCompatActivity() {

    private lateinit var bindingInternal: VB
    protected val binding: VB
        get() = bindingInternal

    abstract fun onCreateDataBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingInternal = onCreateDataBinding()
        setContentView(bindingInternal.root)
    }
}