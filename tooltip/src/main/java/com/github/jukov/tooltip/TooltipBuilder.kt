package com.github.jukov.tooltip

import android.animation.Animator
import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

fun showTooltip(
    fragment: Fragment,
    targetView: View,
    tooltipLayout: View,
    configurator: TooltipBuilder.() -> Unit = {},
): Tooltip =
    TooltipBuilder(fragment, targetView, tooltipLayout)
        .apply { configurator() }
        .show()

fun showTooltip(
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
        .show()

fun showTooltip(
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
        .show()

fun showTooltip(
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
        .show()

fun showTooltip(
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
        .show()

fun showTooltip(
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
        .show()

@Suppress("unused")
class TooltipBuilder {

    private val targetView: View
    private val tooltipView: View

    private val window: Window

    private val activity: Activity

    private val targetViewRect = Rect()
    private val rootGlobalRect = Rect()
    private val rootGlobalOffset = Point()
    private val location = IntArray(2)

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

    private fun handleScrollingParent(tooltip: Tooltip) {
        findNestedScrollParent(targetView)
            ?.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                val beforeTargetViewTop = targetViewRect.top
                dumpTargetViewRect(window.decorView as ViewGroup)

                tooltip.translationY = tooltip.translationY + (targetViewRect.top - beforeTargetViewTop)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findScrollParent(targetView)
                ?.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                    val beforeTargetViewTop = targetViewRect.top
                    dumpTargetViewRect(window.decorView as ViewGroup)

                    tooltip.translationY = tooltip.translationY + (targetViewRect.top - beforeTargetViewTop)
                }

            findHorizontalScrollParent(targetView)
                ?.setOnScrollChangeListener { _, scrollX, _, oldScrollX, _ ->
                    val beforeTargetViewLeft = targetViewRect.left
                    dumpTargetViewRect(window.decorView as ViewGroup)

                    tooltip.translationX = tooltip.translationX + (targetViewRect.left - beforeTargetViewLeft)
                }
        }
    }

    private fun findNestedScrollParent(view: View): NestedScrollView? {
        return if (view.parent == null || view.parent !is View) {
            null
        } else if (view.parent is NestedScrollView) {
            view.parent as NestedScrollView
        } else {
            findNestedScrollParent(view.parent as View)
        }
    }

    private fun findScrollParent(view: View): ScrollView? {
        return if (view.parent == null || view.parent !is View) {
            null
        } else if (view.parent is ScrollView) {
            view.parent as ScrollView
        } else {
            findScrollParent(view.parent as View)
        }
    }

    private fun findHorizontalScrollParent(view: View): HorizontalScrollView? {
        return if (view.parent == null || view.parent !is View) {
            null
        } else if (view.parent is HorizontalScrollView) {
            view.parent as HorizontalScrollView
        } else {
            findHorizontalScrollParent(view.parent as View)
        }
    }

    var themeRes: Int = R.style.Widget_Tooltip

    var position: Tooltip.Position = Tooltip.Position.TOP

    var onDisplayListener: ((View) -> Unit)? = null
    var onHideListener: ((View) -> Unit)? = null
    var afterHideListener: ((View) -> Unit)? = null

    var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()

    fun show(): Tooltip {
        val tooltip = Tooltip(activity, themeRes, tooltipView)
            .apply {
                position = this@TooltipBuilder.position
                onDisplayListener = this@TooltipBuilder.onDisplayListener
                afterHideListener = this@TooltipBuilder.afterHideListener
                tooltipAnimation = this@TooltipBuilder.tooltipAnimation
            }

        if (!tooltip.dimEnabled) {
            handleScrollingParent(tooltip)
        }

        val decorView = window.decorView as ViewGroup
        targetView.postDelayed(
            {
                dumpTargetViewRect(decorView)

                tooltip.addToParent(decorView)

                tooltip.doOnPreDraw {
                    tooltip.show(targetViewRect, decorView.width, decorView.height)
                }
            },
            50L
        )
        return tooltip
    }

    private fun dumpTargetViewRect(decorView: ViewGroup) {
        targetView.getGlobalVisibleRect(targetViewRect)

        decorView.getGlobalVisibleRect(rootGlobalRect, rootGlobalOffset)

        targetView.getLocationOnScreen(location)

        targetViewRect.left = location[0]
        targetViewRect.top -= rootGlobalOffset.y
        targetViewRect.bottom -= rootGlobalOffset.y
        targetViewRect.left -= rootGlobalOffset.x
        targetViewRect.right -= rootGlobalOffset.x
    }

    interface TooltipAnimation {
        fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?)
        fun animateExit(view: View, animatorListener: Animator.AnimatorListener?)
    }
}