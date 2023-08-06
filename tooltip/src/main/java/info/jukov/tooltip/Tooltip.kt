package info.jukov.tooltip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.annotation.StyleRes
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

@SuppressLint("ClickableViewAccessibility", "ViewConstructor")
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Tooltip(
    context: Context,
    @StyleRes themeRes: Int,
    tooltipView: View,
    private val targetView: View,
    private val touchTargetView: View = targetView,
    private val window: Window
) : FrameLayout(context) {

    private var arrowWidth: Float = 0f
    private var arrowHeight: Float = 0f
    private var arrowSourceMargin: Float = 0f
    private var arrowTargetMargin: Float = 0f

    private var cornerRadius: Float = 0f

    private var shadowColor: Int = COLOR_SHADOW_DEFAULT
    private var shadowPadding: Float = 0f
    private var shadowWidth: Float = 0f

    private var targetViewMargin: Int = 0
    private var viewPortMargin: Int = 0

    private var paddingStart: Float = 0f
    private var paddingTop: Float = 0f
    private var paddingEnd: Float = 0f
    private var paddingBottom: Float = 0f

    private var color: Int
        get() = bubblePaint.color
        set(value) {
            bubblePaint.color = value
        }

    private var borderColor: Int
        get() = borderPaint.color
        set(value) {
            borderPaint.color = value
        }

    private var borderWidth: Float
        get() = borderPaint.strokeWidth
        set(value) {
            borderPaint.strokeWidth = value
        }

    private var borderEnabled: Boolean = false

    private var clickToHide = false
    private var clickOutsideToHide = false

    var cancelable = false
        private set

    var isClosed = false
        private set

    private var autoHide = false
    private var autoHideAfterMillis: Long = 0

    internal var onDisplayListener: ((View) -> Unit)? = null
    internal var onHideListener: ((View) -> Unit)? = null
    internal var afterHideListener: ((View) -> Unit)? = null

    internal var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()
    private val dimAnimation: TooltipAnimation = FadeTooltipAnimation()

    internal var position = Position.BOTTOM
        set(value) {
            field = value
            when (value) {
                Position.TOP -> setPaddingRelative(
                    paddingStart.roundToInt(),
                    paddingTop.roundToInt(),
                    paddingEnd.roundToInt(),
                    paddingBottom.roundToInt() + arrowHeight.roundToInt()
                )

                Position.BOTTOM -> setPaddingRelative(
                    paddingStart.roundToInt(),
                    paddingTop.roundToInt() + arrowHeight.roundToInt(),
                    paddingEnd.roundToInt(),
                    paddingBottom.roundToInt()
                )

                Position.START -> setPaddingRelative(
                    paddingStart.roundToInt(),
                    paddingTop.roundToInt(),
                    paddingEnd.roundToInt() + arrowHeight.roundToInt(),
                    paddingBottom.roundToInt()
                )

                Position.END -> setPaddingRelative(
                    paddingStart.roundToInt() + arrowHeight.roundToInt(),
                    paddingTop.roundToInt(),
                    paddingEnd.roundToInt(),
                    paddingBottom.roundToInt()
                )
            }
            postInvalidate()
        }

    private val targetViewRect = Rect()
    private val touchTargetViewRect = Rect()
    private val rootGlobalRect = Rect()
    private val rootGlobalOffset = Point()

    private val bubblePath = Path()
    private val bubbleRect = RectF()

    private val removingHandler = Handler(Looper.getMainLooper())

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
        setWithShadow(true)
        addView(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        visibility = View.INVISIBLE

        dimView.setOnTouchListener { _, event ->
            if (targetViewRect.contains(event.x.toInt(), event.y.toInt())) {
                touchTargetView.getGlobalVisibleRect(touchTargetViewRect)

                val patchedEvent = MotionEvent.obtain(event)
                patchedEvent.offsetLocation(
                    -touchTargetViewRect.left.toFloat(),
                    -touchTargetViewRect.top.toFloat()
                )
                touchTargetView.dispatchTouchEvent(patchedEvent)
                patchedEvent.recycle()
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
        targetViewMargin = typedArray.getDimension(
            R.styleable.Tooltip_tooltipTargetViewMargin,
            dpToPx(TOOLTIP_TARGET_VIEW_MARGIN_DEFAULT_DP, context)
        ).roundToInt()
        viewPortMargin = typedArray.getDimension(
            R.styleable.Tooltip_tooltipViewPortMargin,
            dpToPx(TOOLTIP_VIEW_PORT_MARGIN_DEFAULT_DP, context)
        ).roundToInt()
        paddingStart = typedArray.getDimension(
            R.styleable.Tooltip_paddingStart,
            dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
        )
        paddingTop = typedArray.getDimension(
            R.styleable.Tooltip_paddingTop,
            dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
        )
        paddingEnd = typedArray.getDimension(
            R.styleable.Tooltip_paddingEnd,
            dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
        )
        paddingBottom = typedArray.getDimension(
            R.styleable.Tooltip_paddingBottom,
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
            removingHandler.postDelayed({ this.close() }, autoHideAfterMillis)
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
                    show(decorView.width, decorView.height)
                }
            },
            50L
        )
    }

    private fun dumpTargetViewRect(decorView: ViewGroup) {
        targetView.getGlobalVisibleRect(targetViewRect)

        decorView.getGlobalVisibleRect(rootGlobalRect, rootGlobalOffset)

        targetViewRect.top -= rootGlobalOffset.y
        targetViewRect.bottom -= rootGlobalOffset.y
        targetViewRect.left -= rootGlobalOffset.x
        targetViewRect.right -= rootGlobalOffset.x
    }

    private fun handleScrollingParent() {
        targetView.findParent<NestedScrollView>()
            ?.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
                val beforeTargetViewTop = targetViewRect.top
                dumpTargetViewRect(window.decorView as ViewGroup)

                translationY += (targetViewRect.top - beforeTargetViewTop)
            }

        targetView.findParent<RecyclerView>()
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
            targetView.findParent<ScrollView>()
                ?.setOnScrollChangeListener { _, _, _, _, _ ->
                    val beforeTargetViewTop = targetViewRect.top
                    dumpTargetViewRect(window.decorView as ViewGroup)

                    translationY += (targetViewRect.top - beforeTargetViewTop)
                }

            targetView.findParent<HorizontalScrollView>()
                ?.setOnScrollChangeListener { _, _, _, _, _ ->
                    val beforeTargetViewLeft = targetViewRect.left
                    dumpTargetViewRect(window.decorView as ViewGroup)

                    translationX += (targetViewRect.left - beforeTargetViewLeft)
                }
        }
    }

    private fun show(screenWidth: Int, screenHeight: Int) {
        val adjustedTargetViewRect = Rect(targetViewRect)
        val changed = positioningDelegate.adjustSize(adjustedTargetViewRect, screenWidth)

        if (changed) {
            doOnPreDraw {
                setup(adjustedTargetViewRect, screenWidth, screenHeight)
            }
        } else {
            setup(adjustedTargetViewRect, screenWidth, screenHeight)
        }
    }

    private fun setup(targetViewRect: Rect, screenWidth: Int, screenHeight: Int) {
        positioningDelegate.setupPosition(targetViewRect, screenWidth, screenHeight)

        bubbleRect.set(
            shadowPadding,
            shadowPadding,
            width - shadowPadding,
            height - shadowPadding
        )
        positioningDelegate.updateBubblePath()

        dimView.setTargetViewRect(this.targetViewRect)

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
        removingHandler.removeCallbacksAndMessages(null)
        isClosed = true
    }

    private fun removeScrollingParentListeners() {
        targetView.findParent<NestedScrollView>()
            ?.setOnScrollChangeListener(null as NestedScrollView.OnScrollChangeListener?)

        recyclerViewOnScrollListener?.let { recyclerViewOnScrollListener ->
            targetView.findParent<RecyclerView>()
                ?.removeOnScrollListener(recyclerViewOnScrollListener)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            targetView.findParent<ScrollView>()
                ?.setOnScrollChangeListener(null)

            targetView.findParent<HorizontalScrollView>()
                ?.setOnScrollChangeListener(null)
        }
    }

    private abstract inner class PositioningDelegate {

        abstract fun updateBubblePath()

        abstract fun setupPosition(targetViewRect: Rect, screenWidth: Int, screenHeight: Int)

        abstract fun adjustSizeStart(targetViewRect: Rect, screenWidth: Int): Boolean

        abstract fun adjustSizeEnd(targetViewRect: Rect, screenWidth: Int): Boolean

        fun adjustSize(targetViewRect: Rect, screenWidth: Int): Boolean {
            var changed = false
            val layoutParams = layoutParams

            when (position) {
                Position.END -> {
                    changed = adjustSizeEnd(targetViewRect, screenWidth)
                }

                Position.START -> {
                    changed = adjustSizeStart(targetViewRect, screenWidth)
                }

                Position.TOP, Position.BOTTOM -> {
                    val tooltipWidth: Int

                    if (width + viewPortMargin * 2 >= screenWidth) {
                        layoutParams.width = screenWidth - (viewPortMargin * 2)
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
    }

    private inner class LtrPositioningDelegate : PositioningDelegate() {

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

        override fun setupPosition(targetViewRect: Rect, screenWidth: Int, screenHeight: Int) {
            val x: Int
            val y: Int

            when (position) {
                Position.START -> {
                    x = targetViewRect.left - width - targetViewMargin
                    y = (targetViewRect.top + (targetViewRect.height() - height) / 2)
                        .coerceIn(viewPortMargin, screenHeight - viewPortMargin)
                }

                Position.TOP -> {
                    val xMax = screenWidth - width - viewPortMargin

                    y = targetViewRect.top - height - targetViewMargin
                    x = (targetViewRect.left + (targetViewRect.width() - width) / 2)
                        .coerceIn(viewPortMargin, xMax)
                }

                Position.END -> {
                    x = targetViewRect.right + targetViewMargin
                    y = (targetViewRect.top + (targetViewRect.height() - height) / 2)
                        .coerceIn(viewPortMargin, screenHeight - viewPortMargin)
                }

                Position.BOTTOM -> {
                    val xMax = screenWidth - width - viewPortMargin

                    y = targetViewRect.bottom + targetViewMargin
                    x = (targetViewRect.left + (targetViewRect.width() - width) / 2)
                        .coerceIn(viewPortMargin, xMax)
                }
            }

            translationX = x.toFloat()
            translationY = y.toFloat()
        }

        override fun adjustSizeStart(targetViewRect: Rect, screenWidth: Int): Boolean {
            if (width > targetViewRect.left) {
                layoutParams.width = targetViewRect.left - viewPortMargin -
                        targetViewMargin
                return true
            }
            return false
        }

        override fun adjustSizeEnd(targetViewRect: Rect, screenWidth: Int): Boolean {
            if (targetViewRect.right + width > screenWidth) {
                layoutParams.width = screenWidth - targetViewRect.right -
                        viewPortMargin - targetViewMargin
                return true
            }
            return false
        }
    }

    private inner class RtlPositioningDelegate : PositioningDelegate() {

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

        override fun setupPosition(targetViewRect: Rect, screenWidth: Int, screenHeight: Int) {
            val x: Int
            val y: Int

            when (position) {
                Position.START -> {
                    x = -(screenWidth - targetViewRect.right - width) + targetViewMargin
                    y = (targetViewRect.top + (targetViewRect.height() - height) / 2)
                        .coerceIn(viewPortMargin, screenHeight - viewPortMargin)
                }

                Position.TOP -> {
                    val xMax = -(screenWidth - width - viewPortMargin)

                    y = targetViewRect.top - height - targetViewMargin
                    x =
                        (-(screenWidth - targetViewRect.right) - (targetViewRect.width() - width) / 2)
                            .coerceIn(xMax, -viewPortMargin)
                }

                Position.END -> {
                    x = -(screenWidth - targetViewRect.left) - targetViewMargin
                    y = (targetViewRect.top + (targetViewRect.height() - height) / 2)
                        .coerceIn(viewPortMargin, screenHeight - viewPortMargin)
                }

                Position.BOTTOM -> {
                    val xMax = -(screenWidth - width - viewPortMargin)

                    y = targetViewRect.bottom + targetViewMargin
                    x =
                        (-(screenWidth - targetViewRect.right) - (targetViewRect.width() - width) / 2)
                            .coerceIn(xMax, -viewPortMargin)
                }
            }

            translationX = x.toFloat()
            translationY = y.toFloat()
        }

        override fun adjustSizeStart(targetViewRect: Rect, screenWidth: Int): Boolean {
            if (targetViewRect.right + width > screenWidth) {
                layoutParams.width = screenWidth - targetViewRect.right -
                        viewPortMargin - targetViewMargin
                return true
            }
            return false
        }

        override fun adjustSizeEnd(targetViewRect: Rect, screenWidth: Int): Boolean {
            if (width > targetViewRect.left) {
                layoutParams.width = targetViewRect.left - viewPortMargin -
                        targetViewMargin
                return true
            }
            return false
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
        private const val TOOLTIP_TARGET_VIEW_MARGIN_DEFAULT_DP = 0f
        private const val TOOLTIP_VIEW_PORT_MARGIN_DEFAULT_DP = 0f
        private const val TOOLTIP_PADDING_DEFAULT_DP = 32f

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