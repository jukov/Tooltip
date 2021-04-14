@file:Suppress("unused")

package com.github.jukov.tooltip

import android.animation.Animator
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

fun makeTooltip(
    fragment: Fragment,
    targetView: View,
    tooltipLayout: View,
    configurator: TooltipBuilder.() -> Unit = {},
): Tooltip =
    TooltipBuilder(fragment, targetView, tooltipLayout)
        .apply { configurator() }
        .build()

fun makeTooltip(
    fragment: Fragment,
    targetView: View,
    @LayoutRes tooltipLayoutRes: Int,
    configurator: TooltipBuilder.() -> Unit = {},
): Tooltip =
    TooltipBuilder(
        fragment,
        targetView,
        LayoutInflater.from(fragment.requireContext()).inflate(tooltipLayoutRes, null)
    )
        .apply { configurator() }
        .build()

fun makeTooltip(
    fragment: Fragment,
    @IdRes targetViewRes: Int,
    @LayoutRes tooltipLayoutRes: Int,
    configurator: TooltipBuilder.() -> Unit = {},
): Tooltip =
    TooltipBuilder(
        fragment,
        fragment.requireView().findViewById(targetViewRes),
        LayoutInflater.from(fragment.requireContext()).inflate(tooltipLayoutRes, null)
    )
        .apply { configurator() }
        .build()

fun makeTooltip(
    activity: Activity,
    targetView: View,
    tooltipLayout: View,
    configurator: TooltipBuilder.() -> Unit = {},
): Tooltip =
    TooltipBuilder(
        activity,
        targetView,
        tooltipLayout
    )
        .apply { configurator() }
        .build()

fun makeTooltip(
    activity: Activity,
    targetView: View,
    @LayoutRes tooltipLayoutRes: Int,
    configurator: TooltipBuilder.() -> Unit = {},
): Tooltip =
    TooltipBuilder(
        activity,
        targetView,
        LayoutInflater.from(activity).inflate(tooltipLayoutRes, null)
    )
        .apply { configurator() }
        .build()

fun makeTooltip(
    activity: Activity,
    @IdRes targetViewRes: Int,
    @LayoutRes tooltipLayoutRes: Int,
    configurator: TooltipBuilder.() -> Unit = {},
): Tooltip =
    TooltipBuilder(
        activity,
        activity.findViewById(targetViewRes),
        LayoutInflater.from(activity).inflate(tooltipLayoutRes, null)
    )
        .apply { configurator() }
        .build()

@Suppress("unused")
class TooltipBuilder {

    private val targetView: View
    private val tooltipView: View

    private val window: Window

    private val activity: Activity

    constructor(fragment: Fragment, targetView: View, tooltipView: View) {
        this.activity = fragment.requireActivity()
        this.targetView = targetView
        this.tooltipView = tooltipView
        this.window =
            (fragment as? DialogFragment)?.dialog?.window ?: fragment.requireActivity().window
    }

    constructor(activity: Activity, targetView: View, tooltipView: View) {
        this.activity = activity
        this.targetView = targetView
        this.tooltipView = tooltipView
        this.window = activity.window
    }

    var themeRes: Int = R.style.Tooltip

    var position: Tooltip.Position = Tooltip.Position.TOP

    var onDisplayListener: ((View) -> Unit)? = null
    var onHideListener: ((View) -> Unit)? = null
    var afterHideListener: ((View) -> Unit)? = null

    var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()

    fun build(): Tooltip = Tooltip(activity, themeRes, tooltipView, targetView, window)
        .apply {
            position = this@TooltipBuilder.position
            onDisplayListener = this@TooltipBuilder.onDisplayListener
            afterHideListener = this@TooltipBuilder.afterHideListener
            tooltipAnimation = this@TooltipBuilder.tooltipAnimation
        }

    interface TooltipAnimation {
        fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?)
        fun animateExit(view: View, animatorListener: Animator.AnimatorListener?)
    }
}