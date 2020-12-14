package com.github.jukov.tooltip

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class TestCaseFragment<VB: ViewBinding>: BaseFragment<VB>() {

    abstract val name: String

    private val tooltips = ArrayList<Tooltip>()

    protected lateinit var targetView: View

    abstract fun initTargetView()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initTargetView()

        showTooltips()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tooltips.forEach { it.close() }
    }

    abstract fun showTooltips()

    protected fun showTooltip(
        position: Tooltip.Position,
        align: Tooltip.Align  = Tooltip.Align.CENTER,
    ) {
        tooltips += showTooltip(
            fragment = this,
            targetView,
            R.layout.tooltip_text_long
        ) {
            this.align = align
            this.position = position
            clickToHide = true
            setAutoHide(false, 0)
        }
    }
}