package com.github.jukov.tooltip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.annotation.StyleRes
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.github.jukov.tooltip.TooltipBuilder.TooltipAnimation
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
@Suppress("unused")
class Tooltip(
    context: Context,
    @StyleRes themeRes: Int,
    view: View,
    private val targetView: View,
    private val window: Window
) : FrameLayout(context) {

    var arrowWidth: Float = 0f
    var arrowHeight: Float = 0f
    var arrowSourceMargin: Float = 0f
    var arrowTargetMargin: Float = 0f

    var cornerRadius: Float = 0f

    var shadowColor: Int = COLOR_SHADOW_DEFAULT
    var shadowPadding: Float = 0f
    var shadowWidth: Float = 0f

    var tooltipMargin: Float = 0f

    var tooltipPaddingStart: Float = 0f
    var tooltipPaddingTop: Float = 0f
    var tooltipPaddingEnd: Float = 0f
    var tooltipPaddingBottom: Float = 0f

    var color: Int
        get() = bubblePaint.color
        set(value) {
            bubblePaint.color = value
        }

    var borderColor: Int
        get() = borderPaint.color
        set(value) {
            borderPaint.color = value
        }

    var borderWidth: Float
        get() = borderPaint.strokeWidth
        set(value) {
            borderPaint.strokeWidth = value
        }

    private var borderEnabled: Boolean = false

    var clickToHide = false
    var clickOutsideToHide = false

    var cancelable = false

    internal var autoHide = false
    internal var autoHideAfterMillis: Long = 0

    var onDisplayListener: ((View) -> Unit)? = null
    var onHideListener: ((View) -> Unit)? = null
    var afterHideListener: ((View) -> Unit)? = null

    var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()
    private val dimAnimation: TooltipAnimation = FadeTooltipAnimation()

    var position = Position.BOTTOM
        set(value) {
            field = value
            when (value) {
                Position.TOP -> setPaddingRelative(
                    tooltipPaddingStart.roundToInt(),
                    tooltipPaddingTop.roundToInt(),
                    tooltipPaddingEnd.roundToInt(),
                    tooltipPaddingBottom.roundToInt() + arrowHeight.roundToInt()
                )
                Position.BOTTOM -> setPaddingRelative(
                    tooltipPaddingStart.roundToInt(),
                    tooltipPaddingTop.roundToInt() + arrowHeight.roundToInt(),
                    tooltipPaddingEnd.roundToInt(),
                    tooltipPaddingBottom.roundToInt()
                )
                Position.START -> setPaddingRelative(
                    tooltipPaddingStart.roundToInt(),
                    tooltipPaddingTop.roundToInt(),
                    tooltipPaddingEnd.roundToInt() + arrowHeight.roundToInt(),
                    tooltipPaddingBottom.roundToInt()
                )
                Position.END -> setPaddingRelative(
                    tooltipPaddingStart.roundToInt() + arrowHeight.roundToInt(),
                    tooltipPaddingTop.roundToInt(),
                    tooltipPaddingEnd.roundToInt(),
                    tooltipPaddingBottom.roundToInt()
                )
            }
            postInvalidate()
        }

    private val targetViewRect = Rect()
    private val rootGlobalRect = Rect()
    private val rootGlobalOffset = Point()
    private val location = IntArray(2)

    private val bubblePath = Path()
    private val bubbleRect = RectF()

    private val bubblePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            style = Paint.Style.FILL
        }

    private val borderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            style = Paint.Style.STROKE
        }

    private val positioningDelegate: PositioningDelegate =
        if (context.resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL) {
            RtlPositioningDelegate()
        } else {
            LtrPositioningDelegate()
        }

    var dimEnabled: Boolean = false
        private set

    private val dimView: DimView = DimView(context)

    private var recyclerViewOnScrollListener: RecyclerView.OnScrollListener? = null

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, bubblePaint)
        setWithShadow(true)
        addView(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        visibility = View.INVISIBLE

        dimView.setOnTouchListener { _, event ->
            if (targetViewRect.contains(event.x.toInt(), event.y.toInt())) {
                targetView.dispatchTouchEvent(event)
                true
            } else {
                false
            }
        }

        dimView.setOnClickListener {
            if (cancelable || clickOutsideToHide) {
                close()
            }
        }

        val resolvedThemeRes = if (themeRes == 0) {
            R.style.Tooltip
        } else {
            themeRes
        }

        val typedArray = context.obtainStyledAttributes(
            resolvedThemeRes,
            R.styleable.Tooltip
        )

        arrowWidth = typedArray.getDimension(
            R.styleable.Tooltip_arrowWidth,
            dpToPx(ARROW_WIDTH_DEFAULT_DP, context)
        )
        arrowHeight = typedArray.getDimension(
            R.styleable.Tooltip_arrowHeight,
            dpToPx(ARROW_HEIGHT_DEFAULT_DP, context)
        )
        arrowSourceMargin = typedArray.getDimension(
            R.styleable.Tooltip_arrowSourceMargin,
            dpToPx(ARROW_SOURCE_MARGIN_DEFAULT_DP, context)
        )
        arrowTargetMargin = typedArray.getDimension(
            R.styleable.Tooltip_arrowTargetMargin,
            dpToPx(ARROW_TARGET_MARGIN_DEFAULT_DP, context)
        )
        cornerRadius = typedArray.getDimension(
            R.styleable.Tooltip_cornerRadius,
            dpToPx(CORNER_RADIUS_DEFAULT_DP, context)
        )
        tooltipMargin = typedArray.getDimension(
            R.styleable.Tooltip_tooltipMargin,
            dpToPx(TOOLTIP_MARGIN_DEFAULT_DP, context)
        )
        tooltipPaddingStart = typedArray.getDimension(
            R.styleable.Tooltip_tooltipPaddingStart,
            dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
        )
        tooltipPaddingTop = typedArray.getDimension(
            R.styleable.Tooltip_tooltipPaddingTop,
            dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
        )
        tooltipPaddingEnd = typedArray.getDimension(
            R.styleable.Tooltip_tooltipPaddingEnd,
            dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
        )
        tooltipPaddingBottom = typedArray.getDimension(
            R.styleable.Tooltip_tooltipPaddingBottom,
            dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
        )
        bubblePaint.color =
            typedArray.getColor(R.styleable.Tooltip_backgroundColor, COLOR_BUBBLE_DEFAULT)

        val shadowEnabled = typedArray.getBoolean(R.styleable.Tooltip_shadowEnabled, true)

        if (shadowEnabled) {
            shadowColor = typedArray.getColor(R.styleable.Tooltip_shadowColor, COLOR_SHADOW_DEFAULT)
            shadowPadding = typedArray.getDimension(
                R.styleable.Tooltip_shadowPadding,
                dpToPx(SHADOW_PADDING_DEFAULT_DP, context)
            )
            shadowWidth = typedArray.getDimension(
                R.styleable.Tooltip_shadowWidth,
                dpToPx(SHADOW_WIDTH_DEFAULT_DP, context)
            )

            bubblePaint.setShadowLayer(shadowWidth, 0f, 0f, shadowColor)
        }

        borderEnabled = typedArray.getBoolean(R.styleable.Tooltip_borderEnabled, false)

        if (borderEnabled) {
            borderPaint.color =
                typedArray.getColor(R.styleable.Tooltip_borderColor, Color.TRANSPARENT)
            borderPaint.strokeWidth = typedArray.getDimension(R.styleable.Tooltip_arrowHeight, 0f)
        }

        cancelable = typedArray.getBoolean(R.styleable.Tooltip_cancelable, true)
        clickOutsideToHide = typedArray.getBoolean(R.styleable.Tooltip_clickOutsideToHide, true)
        clickToHide =
            typedArray.getBoolean(R.styleable.Tooltip_clickToHide, true) || clickOutsideToHide
        autoHide = typedArray.getBoolean(R.styleable.Tooltip_autoHide, false)
        autoHideAfterMillis =
            typedArray.getInteger(R.styleable.Tooltip_autoHideAfterMillis, 0).toLong()

        dimEnabled = typedArray.getBoolean(R.styleable.Tooltip_dimEnabled, false)
        if (dimEnabled) {
            val dimColor = typedArray.getColor(R.styleable.Tooltip_dimColor, Color.TRANSPARENT)
            val dimCornerRadius = typedArray.getDimension(
                R.styleable.Tooltip_dimTargetViewCornerRadius,
                dpToPx(CORNER_RADIUS_DEFAULT_DP, context)
            )
            val dimPadding = typedArray.getDimension(
                R.styleable.Tooltip_dimTargetViewPadding,
                dpToPx(CORNER_RADIUS_DEFAULT_DP, context)
            )

            dimView.cornerRadius = dimCornerRadius
            dimView.padding = dimPadding
            dimView.setDimColor(dimColor)
        }

        typedArray.recycle()
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        bubbleRect.set(
            shadowPadding,
            shadowPadding,
            width - shadowPadding,
            height - shadowPadding
        )
        positioningDelegate.updateBubblePath()
    }

    override fun draw(canvas: Canvas?) {
        if (bubblePath.isEmpty) return
        super.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (bubblePath.isEmpty) return

        val bubblePath = bubblePath
        val bubblePaint = bubblePaint

        canvas.drawPath(bubblePath, bubblePaint)

        if (borderEnabled) {
            canvas.drawPath(bubblePath, borderPaint)
        }
    }

    fun setWithShadow(withShadow: Boolean) {
        if (withShadow) {
            bubblePaint.setShadowLayer(shadowWidth, 0f, 0f, shadowColor)
        } else {
            bubblePaint.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        }
    }

    private fun startEnterAnimation() {
        dimAnimation.animateEnter(dimView, null)
        tooltipAnimation.animateEnter(this, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (onDisplayListener != null) {
                    onDisplayListener?.invoke(this@Tooltip)
                }
            }
        })
    }

    private fun startExitAnimation(animatorListener: Animator.AnimatorListener) {
        dimAnimation.animateExit(dimView, null)
        tooltipAnimation.animateExit(this, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animatorListener.onAnimationEnd(animation)
            }
        })
    }

    private fun handleAutoRemove() {
        if (clickToHide) {
            setOnClickListener {
                if (clickToHide) {
                    this.close()
                }
            }
        }
        if (autoHide) {
            postDelayed({ this.close() }, autoHideAfterMillis)
        }
    }

    private fun addToParent(viewGroup: ViewGroup) {
        if (dimEnabled || clickOutsideToHide) {
            viewGroup.addView(
                dimView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        viewGroup.addView(
            this,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun show() {
        if (!dimEnabled) {
            handleScrollingParent()
        }

        val decorView = window.decorView as ViewGroup
        targetView.postDelayed(
            {
                dumpTargetViewRect(decorView)

                addToParent(decorView)

                doOnPreDraw {
                    show(targetViewRect, decorView.width, decorView.height)
                }
            },
            50L
        )
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

    private fun handleScrollingParent() {
        findNestedScrollParent(targetView)
            ?.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
                val beforeTargetViewTop = targetViewRect.top
                dumpTargetViewRect(window.decorView as ViewGroup)

                translationY += (targetViewRect.top - beforeTargetViewTop)
            }

        findRecyclerViewParent(targetView)
            ?.apply {
                recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        this@Tooltip.translationY -= dy
                    }
                }

                recyclerViewOnScrollListener?.let { recyclerViewOnScrollListener ->
                    addOnScrollListener(recyclerViewOnScrollListener)
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findScrollParent(targetView)
                ?.setOnScrollChangeListener { _, _, _, _, _ ->
                    val beforeTargetViewTop = targetViewRect.top
                    dumpTargetViewRect(window.decorView as ViewGroup)

                    translationY += (targetViewRect.top - beforeTargetViewTop)
                }

            findHorizontalScrollParent(targetView)
                ?.setOnScrollChangeListener { _, _, _, _, _ ->
                    val beforeTargetViewLeft = targetViewRect.left
                    dumpTargetViewRect(window.decorView as ViewGroup)

                    translationX += (targetViewRect.left - beforeTargetViewLeft)
                }
        }
    }

    private fun findRecyclerViewParent(view: View): RecyclerView? {
        return if (view.parent == null || view.parent !is View) {
            null
        } else if (view.parent is RecyclerView) {
            view.parent as RecyclerView
        } else {
            findRecyclerViewParent(view.parent as View)
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

    private fun show(targetViewRect: Rect, screenWidth: Int, screenHeight: Int) {
        this.targetViewRect.set(targetViewRect)

        val adjustedTargetViewRect = Rect(targetViewRect)
        val changed = adjustSize(adjustedTargetViewRect, screenWidth)

        if (changed) {
            doOnPreDraw {
                setup(adjustedTargetViewRect, screenWidth)
            }
        } else {
            setup(adjustedTargetViewRect, screenWidth)
        }
    }

    private fun adjustSize(targetViewRect: Rect, screenWidth: Int): Boolean {
        var changed = false
        val layoutParams = layoutParams

        val defaultMargin = dpToPx(MARGIN_SCREEN_BORDER_TOOLTIP_DP, context)

        when (position) {
            Position.START -> {
                if (width > targetViewRect.left) {
                    layoutParams.width =
                        targetViewRect.left - defaultMargin.roundToInt() - tooltipMargin.roundToInt()
                    changed = true
                }
            }

            Position.END -> {
                if (targetViewRect.right + width > screenWidth) {
                    layoutParams.width =
                        screenWidth - targetViewRect.right - defaultMargin.roundToInt() - tooltipMargin.roundToInt()
                    changed = true
                }
            }

            Position.TOP, Position.BOTTOM -> {
                val tooltipWidth: Int

                if (width + defaultMargin * 2 >= screenWidth) {
                    layoutParams.width = screenWidth - (defaultMargin * 2).roundToInt()
                    tooltipWidth = layoutParams.width
                    changed = true
                } else {
                    tooltipWidth = width
                }

                var adjustedLeft = targetViewRect.left
                var adjustedRight = targetViewRect.right

                if (targetViewRect.centerX() + tooltipWidth / 2f > screenWidth) {
                    val diff = targetViewRect.centerX() + tooltipWidth / 2f - screenWidth
                    adjustedLeft -= diff.toInt()
                    adjustedRight -= diff.toInt()
                    changed = true

                } else if (targetViewRect.centerX() - tooltipWidth / 2f < 0) {
                    val diff = -(targetViewRect.centerX() - tooltipWidth / 2f)
                    adjustedLeft += diff.toInt()
                    adjustedRight += diff.toInt()
                    changed = true
                }

                if (adjustedLeft < 0) {
                    adjustedLeft = 0
                }

                if (adjustedRight > screenWidth) {
                    adjustedRight = screenWidth
                }

                targetViewRect.left = adjustedLeft
                targetViewRect.right = adjustedRight
            }
        }

        if (position.isHorizontal() && arrowWidth + cornerRadius * 2 > targetViewRect.height()) {
            targetViewRect.top -= (arrowWidth / 2 + cornerRadius).toInt()
            targetViewRect.bottom += (arrowWidth / 2 + cornerRadius).toInt()
            changed = true
        }

        if (position.isVertical() && arrowWidth + cornerRadius * 2 > targetViewRect.width()) {
            targetViewRect.left -= (arrowWidth / 2 + cornerRadius).toInt()
            targetViewRect.right += (arrowWidth / 2 + cornerRadius).toInt()
            changed = true
        }

        setLayoutParams(layoutParams)
        postInvalidate()

        return changed
    }

    private fun setup(targetViewRect: Rect, screenWidth: Int) {
        positioningDelegate.setupPosition(targetViewRect, screenWidth)

        bubbleRect.set(
            shadowPadding,
            shadowPadding,
            width - shadowPadding,
            height - shadowPadding
        )
        positioningDelegate.updateBubblePath()

        dimView.setTargetViewRect(targetViewRect)

        startEnterAnimation()

        visibility = View.VISIBLE

        handleAutoRemove()
    }

    fun close() {
        onHideListener?.invoke(this@Tooltip)
        startExitAnimation(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                this@Tooltip.closeNow()
            }
        })
    }

    fun closeNow() {
        (parent as? ViewGroup)?.removeView(dimView)
        (parent as? ViewGroup)?.removeView(this)
        afterHideListener?.invoke(this)
        removeScrollingParentListeners()
    }

    private fun removeScrollingParentListeners() {
        findNestedScrollParent(targetView)
            ?.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener)

        recyclerViewOnScrollListener?.let { recyclerViewOnScrollListener ->
            findRecyclerViewParent(targetView)
                ?.removeOnScrollListener(recyclerViewOnScrollListener)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findScrollParent(targetView)
                ?.setOnScrollChangeListener(null)

            findHorizontalScrollParent(targetView)
                ?.setOnScrollChangeListener(null)
        }
    }

    private interface PositioningDelegate {

        fun updateBubblePath()

        fun setupPosition(targetViewRect: Rect, screenWidth: Int)
    }

    private inner class LtrPositioningDelegate : PositioningDelegate {

        override fun updateBubblePath() {
            bubblePath.reset()

            if (targetViewRect.isEmpty) return

            val spacingLeft =
                if (position == Position.END) arrowHeight else 0.toFloat()
            val spacingTop =
                if (position == Position.BOTTOM) arrowHeight else 0.toFloat()
            val spacingRight =
                if (position == Position.START) arrowHeight else 0.toFloat()
            val spacingBottom =
                if (position == Position.TOP) arrowHeight else 0.toFloat()

            val left = spacingLeft + bubbleRect.left
            val top = spacingTop + bubbleRect.top
            val right = bubbleRect.right - spacingRight
            val bottom = bubbleRect.bottom - spacingBottom

            val centerX = targetViewRect.centerX() - x
            val centerY = targetViewRect.centerY() - y

            val arrowSourceX = if (position.isVertical()) centerX + arrowSourceMargin else centerX
            val arrowTargetX = if (position.isVertical()) centerX + arrowTargetMargin else centerX
            val arrowSourceY = if (position.isHorizontal()) centerY - arrowSourceMargin else centerY
            val arrowTargetY = if (position.isHorizontal()) centerY - arrowTargetMargin else centerY

            bubblePath.moveTo(left + cornerRadius / 2f, top)

            if (position == Position.BOTTOM) {
                bubblePath.lineTo(arrowSourceX - arrowWidth / 2, top)
                bubblePath.lineTo(arrowTargetX, bubbleRect.top)
                bubblePath.lineTo(arrowSourceX + arrowWidth / 2, top)
            }

            bubblePath.lineTo(right - cornerRadius / 2f, top)
            bubblePath.quadTo(right, top, right, top + cornerRadius / 2)

            if (position == Position.START) {
                bubblePath.lineTo(right, arrowSourceY - arrowWidth / 2)
                bubblePath.lineTo(bubbleRect.right, arrowTargetY)
                bubblePath.lineTo(right, arrowSourceY + arrowWidth / 2)
            }

            bubblePath.lineTo(right, bottom - cornerRadius / 2)
            bubblePath.quadTo(right, bottom, right - cornerRadius / 2, bottom)

            if (position == Position.TOP) {
                bubblePath.lineTo(arrowSourceX + arrowWidth / 2, bottom)
                bubblePath.lineTo(arrowTargetX, bubbleRect.bottom)
                bubblePath.lineTo(arrowSourceX - arrowWidth / 2, bottom)
            }

            bubblePath.lineTo(left + cornerRadius / 2, bottom)
            bubblePath.quadTo(left, bottom, left, bottom - cornerRadius / 2)

            if (position == Position.END) {
                bubblePath.lineTo(left, arrowSourceY + arrowWidth / 2)
                bubblePath.lineTo(bubbleRect.left, arrowTargetY)
                bubblePath.lineTo(left, arrowSourceY - arrowWidth / 2)
            }

            bubblePath.lineTo(left, top + cornerRadius / 2)
            bubblePath.quadTo(left, top, left + cornerRadius / 2, top)
            bubblePath.close()
        }

        override fun setupPosition(targetViewRect: Rect, screenWidth: Int) {
            val x: Int
            val y: Int

            val defaultMargin = dpToPx(MARGIN_SCREEN_BORDER_TOOLTIP_DP, context).toInt()

            when (position) {
                Position.START -> {
                    x = targetViewRect.left - width - tooltipMargin.roundToInt()
                    y = targetViewRect.top + (targetViewRect.height() - height) / 2
                }

                Position.TOP -> {
                    val xMax = screenWidth - width - defaultMargin

                    y = targetViewRect.top - height - tooltipMargin.roundToInt()
                    x = (targetViewRect.left + (targetViewRect.width() - width) / 2)
                        .coerceIn(defaultMargin..xMax)
                }

                Position.END -> {
                    x = targetViewRect.right + tooltipMargin.roundToInt()
                    y = targetViewRect.top + (targetViewRect.height() - height) / 2
                }

                Position.BOTTOM -> {
                    val xMax = screenWidth - width - defaultMargin

                    y = targetViewRect.bottom + tooltipMargin.roundToInt()
                    x = (targetViewRect.left + (targetViewRect.width() - width) / 2)
                        .coerceIn(defaultMargin..xMax)
                }
            }

            translationX = x.toFloat()
            translationY = y.toFloat()
        }
    }

    private inner class RtlPositioningDelegate : PositioningDelegate {

        override fun updateBubblePath() {
            bubblePath.reset()

            if (targetViewRect.isEmpty) return

            val spacingLeft =
                if (position == Position.START) arrowHeight else 0.toFloat()
            val spacingTop =
                if (position == Position.BOTTOM) arrowHeight else 0.toFloat()
            val spacingRight =
                if (position == Position.END) arrowHeight else 0.toFloat()
            val spacingBottom =
                if (position == Position.TOP) arrowHeight else 0.toFloat()

            val left = spacingLeft + bubbleRect.left
            val top = spacingTop + bubbleRect.top
            val right = bubbleRect.right - spacingRight
            val bottom = bubbleRect.bottom - spacingBottom

            val centerX = targetViewRect.centerX() - x
            val centerY = targetViewRect.centerY() - y

            val arrowSourceX = if (position.isVertical()) centerX + arrowSourceMargin else centerX
            val arrowTargetX = if (position.isVertical()) centerX + arrowTargetMargin else centerX
            val arrowSourceY = if (position.isHorizontal()) centerY - arrowSourceMargin else centerY
            val arrowTargetY = if (position.isHorizontal()) centerY - arrowTargetMargin else centerY

            bubblePath.moveTo(left + cornerRadius / 2f, top)

            if (position == Position.BOTTOM) {
                bubblePath.lineTo(arrowSourceX - arrowWidth / 2, top)
                bubblePath.lineTo(arrowTargetX, bubbleRect.top)
                bubblePath.lineTo(arrowSourceX + arrowWidth / 2, top)
            }

            bubblePath.lineTo(right - cornerRadius / 2f, top)
            bubblePath.quadTo(right, top, right, top + cornerRadius / 2)

            if (position == Position.END) {
                bubblePath.lineTo(right, arrowSourceY - arrowWidth / 2)
                bubblePath.lineTo(bubbleRect.right, arrowTargetY)
                bubblePath.lineTo(right, arrowSourceY + arrowWidth / 2)
            }

            bubblePath.lineTo(right, bottom - cornerRadius / 2)
            bubblePath.quadTo(right, bottom, right - cornerRadius / 2, bottom)

            if (position == Position.TOP) {
                bubblePath.lineTo(arrowSourceX + arrowWidth / 2, bottom)
                bubblePath.lineTo(arrowTargetX, bubbleRect.bottom)
                bubblePath.lineTo(arrowSourceX - arrowWidth / 2, bottom)
            }

            bubblePath.lineTo(left + cornerRadius / 2, bottom)
            bubblePath.quadTo(left, bottom, left, bottom - cornerRadius / 2)

            if (position == Position.START) {
                bubblePath.lineTo(left, arrowSourceY + arrowWidth / 2)
                bubblePath.lineTo(bubbleRect.left, arrowTargetY)
                bubblePath.lineTo(left, arrowSourceY - arrowWidth / 2)
            }

            bubblePath.lineTo(left, top + cornerRadius / 2)
            bubblePath.quadTo(left, top, left + cornerRadius / 2, top)
            bubblePath.close()
        }

        override fun setupPosition(targetViewRect: Rect, screenWidth: Int) {
            val x: Int
            val y: Int

            val defaultMargin = dpToPx(MARGIN_SCREEN_BORDER_TOOLTIP_DP, context).toInt()

            when (position) {
                Position.START -> {
                    x = -(screenWidth - targetViewRect.right - width) + tooltipMargin.roundToInt()
                    y = targetViewRect.top + (targetViewRect.height() - height) / 2
                }

                Position.TOP -> {
                    val xMax = -(screenWidth - width - defaultMargin)

                    y = targetViewRect.top - height - tooltipMargin.roundToInt()
                    x =
                        (-(screenWidth - targetViewRect.right) - (targetViewRect.width() - width) / 2)
                            .coerceIn(xMax, -defaultMargin)
                }

                Position.END -> {
                    x = -(screenWidth - targetViewRect.left) - tooltipMargin.roundToInt()
                    y = targetViewRect.top + (targetViewRect.height() - height) / 2
                }

                Position.BOTTOM -> {
                    val xMax = -(screenWidth - width - defaultMargin)

                    y = targetViewRect.bottom + tooltipMargin.roundToInt()
                    x =
                        (-(screenWidth - targetViewRect.right) - (targetViewRect.width() - width) / 2)
                            .coerceIn(xMax, -defaultMargin)
                }
            }

            translationX = x.toFloat()
            translationY = y.toFloat()
        }
    }

    companion object {
        private const val ARROW_WIDTH_DEFAULT_DP = 16f
        private const val ARROW_HEIGHT_DEFAULT_DP = 8f
        private const val ARROW_SOURCE_MARGIN_DEFAULT_DP = 0f
        private const val ARROW_TARGET_MARGIN_DEFAULT_DP = 0f
        private const val CORNER_RADIUS_DEFAULT_DP = 8f
        private const val SHADOW_PADDING_DEFAULT_DP = 2f
        private const val SHADOW_WIDTH_DEFAULT_DP = 4f
        private const val TOOLTIP_MARGIN_DEFAULT_DP = 0f
        private const val TOOLTIP_PADDING_DEFAULT_DP = 8f

        private const val MARGIN_SCREEN_BORDER_TOOLTIP_DP = 12f

        private const val COLOR_BUBBLE_DEFAULT = 0xFF4CAF50.toInt()
        private const val COLOR_SHADOW_DEFAULT = 0xFF444444.toInt()
    }

    enum class Position {
        START,
        END,
        TOP,
        BOTTOM;

        fun isHorizontal() = this == START || this == END

        fun isVertical() = this == TOP || this == BOTTOM
    }
}