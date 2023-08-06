@file:Suppress("unused")

package info.jukov.tooltip

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
class TooltipBuilder(
    private val activity: Activity,
    private val targetView: View,
    private val tooltipView: View,
    private val touchTargetView: View,
    private val window: Window
) {
    private var themeRes: Int = R.style.Tooltip
    private var position: Tooltip.Position = Tooltip.Position.TOP
    private var onDisplayListener: ((View) -> Unit)? = null
    private var onHideListener: ((View) -> Unit)? = null
    private var afterHideListener: ((View) -> Unit)? = null
    private var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()

    constructor(
        fragment: Fragment,
        @IdRes targetViewRes: Int,
        @IdRes touchTargetViewRes: Int = targetViewRes,
        @LayoutRes tooltipLayoutRes: Int,
    ) : this(
        fragment,
        fragment.requireView().findViewById(targetViewRes),
        fragment.requireView().findViewById(touchTargetViewRes),
        LayoutInflater.from(fragment.requireContext()).inflate(tooltipLayoutRes, null)
    )

    constructor(
        fragment: Fragment,
        targetView: View,
        touchTargetView: View = targetView,
        @LayoutRes tooltipLayoutRes: Int,
    ) : this(
        fragment,
        targetView,
        touchTargetView,
        LayoutInflater.from(fragment.requireContext()).inflate(tooltipLayoutRes, null)
    )

    constructor(
        fragment: Fragment,
        targetView: View,
        touchTargetView: View,
        tooltipView: View,
    ) : this(
        fragment.requireActivity(),
        targetView,
        tooltipView,
        touchTargetView,
        (fragment as? DialogFragment)?.dialog?.window ?: fragment.requireActivity().window
    )

    constructor(
        activity: Activity,
        targetView: View,
        touchTargetView: View = targetView,
        @LayoutRes tooltipLayoutRes: Int,
    ) : this(
        activity,
        targetView,
        touchTargetView,
        LayoutInflater.from(activity).inflate(tooltipLayoutRes, null)
    )

    constructor(
        activity: Activity,
        @IdRes targetViewRes: Int,
        @IdRes touchTargetViewRes: Int = targetViewRes,
        @LayoutRes tooltipLayoutRes: Int,
    ) : this(
        activity,
        activity.findViewById(targetViewRes),
        activity.findViewById(touchTargetViewRes),
        LayoutInflater.from(activity).inflate(tooltipLayoutRes, null)
    )

    constructor(
        activity: Activity,
        targetView: View,
        tooltipView: View,
        touchTargetView: View,
    ) : this(activity, targetView, tooltipView, touchTargetView, activity.window)

    fun setTheme(@StyleRes themeRes: Int): TooltipBuilder = apply {
        this.themeRes = themeRes
    }

    fun setPosition(position: Tooltip.Position): TooltipBuilder = apply {
        this.position = position
    }

    fun setOnDisplayListener(onDisplayListener: (View) -> Unit) = apply {
        this.onDisplayListener = onDisplayListener
    }

    fun setOnHideListener(onHideListener: (View) -> Unit) = apply {
        this.onHideListener = onHideListener
    }

    fun setAfterHideListener(afterHideListener: (View) -> Unit) = apply {
        this.afterHideListener = afterHideListener
    }

    fun setTooltipAnimation(tooltipAnimation: TooltipAnimation) = apply {
        this.tooltipAnimation = tooltipAnimation
    }

    fun build(): Tooltip =
        Tooltip(activity, themeRes, tooltipView, targetView, touchTargetView, window)
            .apply {
                position = this@TooltipBuilder.position
                onDisplayListener = this@TooltipBuilder.onDisplayListener
                onHideListener = this@TooltipBuilder.onHideListener
                afterHideListener = this@TooltipBuilder.afterHideListener
                tooltipAnimation = this@TooltipBuilder.tooltipAnimation
            }

    fun show(): Tooltip = build().apply { show() }
}