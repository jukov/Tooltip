package com.github.jukov.tooltip

import android.animation.Animator
import android.app.Activity
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

/**
 * Created by florentchampigny on 02/06/2017.
 */

fun tooltipOf(
    fragment: Fragment,
    targetView: View,
    tooltipLayout: View,
    configurator: TooltipBuilder.() -> Unit,
): Tooltip =
    TooltipBuilder(fragment, targetView, tooltipLayout)
        .apply { configurator() }
        .show()

fun tooltipOf(
    activity: Activity,
    @IdRes targetViewRes: Int,
    @LayoutRes tooltipLayoutRes: Int,
    configurator: TooltipBuilder.() -> Unit,
): Tooltip =
    TooltipBuilder(activity, targetViewRes, tooltipLayoutRes)
        .apply { configurator() }
        .show()

@Suppress("unused")
class TooltipBuilder {

    private val targetView: View
    private val tooltip: Tooltip

    private val window: Window

    private val activity: Activity

    constructor(
        fragment: Fragment,
        @IdRes targetViewRes: Int,
        @LayoutRes tooltipLayoutRes: Int,
    ) : this(
        fragment,
        fragment.requireView().findViewById(targetViewRes),
        LayoutInflater.from(fragment.requireContext()).inflate(tooltipLayoutRes, null)
    )

    constructor(fragment: Fragment, targetView: View, tooltipLayout: View) {
        this.activity = fragment.requireActivity()
        this.targetView = targetView
        this.window =
            (fragment as? DialogFragment)?.dialog?.window ?: fragment.requireActivity().window
        this.tooltip = Tooltip(fragment.requireContext(), tooltipLayout)
        handleScrollingParent(targetView)
    }

    constructor(
        activity: Activity,
        @IdRes targetViewRes: Int,
        @LayoutRes tooltipLayoutRes: Int,
    ) : this(
        activity,
        activity.findViewById(targetViewRes),
        LayoutInflater.from(activity).inflate(tooltipLayoutRes, null)
    )

    constructor(activity: Activity, targetView: View, tooltipLayout: View) {
        this.activity = activity
        this.targetView = targetView
        this.window = activity.window
        this.tooltip = Tooltip(activity, tooltipLayout)
        handleScrollingParent(targetView)
    }

    private fun handleScrollingParent(targetView: View) {
        findScrollParent(targetView)
            ?.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                tooltip.translationY = tooltip.translationY - (scrollY - oldScrollY)
            }
    }

    private fun findScrollParent(view: View): NestedScrollView? {
        return if (view.parent == null || view.parent !is View) {
            null
        } else if (view.parent is NestedScrollView) {
            view.parent as NestedScrollView
        } else {
            findScrollParent(view.parent as View)
        }
    }

    var position: Position
        get() = tooltip.position
        set(value) {
            tooltip.position = value
        }

    var arrowWidthDp: Float
        get() = pxToDp(tooltip.arrowWidth, activity)
        set(value) {
            tooltip.arrowWidth = dpToPx(value, activity)
        }

    var arrowHeight: Float
        get() = pxToDp(tooltip.arrowHeight, activity)
        set(value) {
            tooltip.arrowHeight = dpToPx(value, activity)
        }

    var arrowSourceMargin: Float
        get() = pxToDp(tooltip.arrowSourceMargin, activity)
        set(value) {
            tooltip.arrowSourceMargin = dpToPx(value, activity)
        }

    var arrowTargetMargin: Float
        get() = pxToDp(tooltip.arrowTargetMargin, activity)
        set(value) {
            tooltip.arrowTargetMargin = dpToPx(value, activity)
        }

    var cornerDp: Float
        get() = pxToDp(tooltip.cornerRadius, activity)
        set(value) {
            tooltip.cornerRadius = dpToPx(value, activity)
        }

    var margin: Float
        get() = pxToDp(tooltip.tooltipMargin, activity)
        set(value) {
            tooltip.tooltipMargin = dpToPx(value, activity)
        }

    var align: Align
        get() = tooltip.align
        set(value) {
            tooltip.align = value
        }

    var duration: Long
        get() = tooltip.durationMillis
        set(value) {
            tooltip.durationMillis = value
        }

    var colorInt: Int
        get() = tooltip.color
        set(value) {
            tooltip.color = value
        }

    var shadowColorInt: Int
        get() = tooltip.shadowColor
        set(value) {
            tooltip.shadowColor = value
        }

    var bubblePaint: Paint
        get() = tooltip.bubblePaint
        set(value) {
            tooltip.bubblePaint = value
        }

    var onDisplayListener: ((View) -> Unit)?
        get() = tooltip.onDisplayListener
        set(value) {
            tooltip.onDisplayListener = value
        }

    var onHideListener: ((View) -> Unit)?
        get() = tooltip.onHideListener
        set(value) {
            tooltip.onHideListener = value
        }

    var tooltipAnimation: TooltipAnimation
        get() = tooltip.tooltipAnimation
        set(value) {
            tooltip.tooltipAnimation = value
        }

    var clickToHide: Boolean
        get() = tooltip.clickToHide
        set(value) {
            tooltip.clickToHide = value
        }

    fun setWithShadow(withShadow: Boolean): TooltipBuilder {
        tooltip.setWithShadow(withShadow)
        return this
    }

    fun setAutoHide(autoHide: Boolean, duration: Long): TooltipBuilder {
        tooltip.autoHide = autoHide
        tooltip.durationMillis = duration
        return this
    }

    fun setPaddingRelative(start: Float, top: Float, end: Float, bottom: Float): TooltipBuilder {
        tooltip.tooltipPaddingTop = top
        tooltip.tooltipPaddingBottom = bottom
        tooltip.tooltipPaddingStart = start
        tooltip.tooltipPaddingEnd = end
        return this
    }

    fun border(color: Int, width: Float): TooltipBuilder {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.color = color
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = width
        tooltip.borderPaint = borderPaint
        return this
    }

    fun show(): Tooltip {
        val decorView = window.decorView as ViewGroup
        targetView.postDelayed(
            {
                val targetViewRect = Rect()

                targetView.getGlobalVisibleRect(targetViewRect)

                val rootGlobalRect = Rect()
                val rootGlobalOffset = Point()
                decorView.getGlobalVisibleRect(rootGlobalRect, rootGlobalOffset)

                val location = IntArray(2)
                targetView.getLocationOnScreen(location)

                targetViewRect.left = location[0]
                targetViewRect.top -= rootGlobalOffset.y
                targetViewRect.bottom -= rootGlobalOffset.y
                targetViewRect.left -= rootGlobalOffset.x
                targetViewRect.right -= rootGlobalOffset.x

                decorView.addView(
                    tooltip,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                tooltip.doOnPreDraw {
                    tooltip.show(targetViewRect, decorView.width, decorView.height)
                }
            },
            50L
        )
        return tooltip
    }

    enum class Position {
        START,
        END,
        TOP,
        BOTTOM;

        fun isHorizontal() = this == START || this == END

        fun isVertical() = this == TOP || this == BOTTOM
    }

    enum class Align {
        START, CENTER, END
    }

    interface TooltipAnimation {
        fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?)
        fun animateExit(view: View, animatorListener: Animator.AnimatorListener?)
    }
}