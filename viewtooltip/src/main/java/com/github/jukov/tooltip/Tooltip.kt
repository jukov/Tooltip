package com.github.jukov.tooltip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.jukov.tooltip.TooltipBuilder.TooltipAnimation
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
@Suppress("unused")
class Tooltip(
    context: Context,
    view: View,
) : FrameLayout(context) {

    var arrowWidth: Float = dpToPx(ARROW_WIDTH_DEFAULT_DP, context)
        set(value) {
            field = value
            postInvalidate()
        }

    var arrowHeight: Float = dpToPx(ARROW_HEIGHT_DEFAULT_DP, context)
        set(value) {
            field = value
            postInvalidate()
        }

    var arrowSourceMargin: Float = dpToPx(ARROW_SOURCE_MARGIN_DEFAULT_DP, context)
        set(value) {
            field = value
            postInvalidate()
        }

    var arrowTargetMargin: Float = dpToPx(ARROW_TARGET_MARGIN_DEFAULT_DP, context)
        set(value) {
            field = value
            postInvalidate()
        }

    var cornerRadius: Float = dpToPx(CORNER_RADIUS_DEFAULT_DP, context)
        set(value) {
            field = value
            postInvalidate()
        }

    var shadowPadding: Float = dpToPx(SHADOW_PADDING_DEFAULT_DP, context)
    var shadowWidth: Float = dpToPx(SHADOW_WIDTH_DEFAULT_DP, context)

    var tooltipMargin: Float = dpToPx(TOOLTIP_MARGIN_DEFAULT_DP, context)

    var tooltipPaddingStart: Float = dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
    var tooltipPaddingTop: Float = dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
    var tooltipPaddingEnd: Float = dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)
    var tooltipPaddingBottom: Float = dpToPx(TOOLTIP_PADDING_DEFAULT_DP, context)

    var color: Int = COLOR_BUBBLE_DEFAULT
        set(value) {
            field = value
            bubblePaint.color = color
            postInvalidate()
        }

    var shadowColor: Int = COLOR_SHADOW_DEFAULT
        set(value) {
            field = value
            postInvalidate()
        }

    var bubblePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_BUBBLE_DEFAULT
        style = Paint.Style.FILL
    }
        set(value) {
            field = value
            setLayerType(LAYER_TYPE_SOFTWARE, value)
            postInvalidate()
        }

    var borderPaint: Paint? = null
        set(value) {
            field = value
            postInvalidate()
        }

    var align = Align.CENTER
        set(value) {
            field = value
            postInvalidate()
        }

    var clickToHide = false

    var autoHide = false
    var durationMillis: Long = 0

    var onDisplayListener: ((View) -> Unit)? = null
    var onHideListener: ((View) -> Unit)? = null

    var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()

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
    private val bubblePath = Path()
    private val bubbleRect = RectF()

    private val positioningDelegate: PositioningDelegate =
        if (layoutDirection == LAYOUT_DIRECTION_RTL) {
            RtlPositioningDelegate()
        } else {
            LtrPositioningDelegate()
        }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, bubblePaint)
        setWithShadow(true)
        addView(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bubblePath = bubblePath
        val bubblePaint = bubblePaint

        canvas.drawPath(bubblePath, bubblePaint)

        val borderPaint = borderPaint ?: return

        canvas.drawPath(bubblePath, borderPaint)
    }

    fun setWithShadow(withShadow: Boolean) {
        if (withShadow) {
            bubblePaint.setShadowLayer(shadowWidth, 0f, 0f, shadowColor)
        } else {
            bubblePaint.setShadowLayer(0f, 0f, 0f, COLOR_TRANSPARENT)
        }
    }

    private fun startEnterAnimation() {
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
        tooltipAnimation.animateExit(this, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animatorListener.onAnimationEnd(animation)
                if (onHideListener != null) {
                    onHideListener?.invoke(this@Tooltip)
                }
            }
        })
    }

    private fun handleAutoRemove() {
        if (clickToHide) {
            setOnClickListener {
                if (clickToHide) {
                    remove()
                }
            }
        }
        if (autoHide) {
            postDelayed({ remove() }, durationMillis)
        }
    }

    fun show(targetViewRect: Rect, screenWidth: Int, screenHeight: Int) {
        this.targetViewRect.set(targetViewRect)

        val adjustedTargetViewRect = Rect(targetViewRect)
        val changed = adjustSize(adjustedTargetViewRect, screenWidth, screenHeight)

        if (changed) {
            doOnPreDraw {
                setup(adjustedTargetViewRect, screenWidth)
            }
        } else {
            setup(adjustedTargetViewRect, screenWidth)
        }
    }

    private fun adjustSize(targetViewRect: Rect, screenWidth: Int, screenHeight: Int): Boolean {
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

                if (targetViewRect.centerY() + height / 2f > screenHeight) {
                    align = Align.END

                } else if (targetViewRect.centerY() - height / 2f < 0) {
                    align = Align.START

                }
            }

            Position.END -> {
                if (targetViewRect.right + width > screenWidth) {
                    layoutParams.width =
                        screenWidth - targetViewRect.right - defaultMargin.roundToInt() - tooltipMargin.roundToInt()
                    changed = true
                }

                if (targetViewRect.centerY() + height / 2f > screenHeight) {
                    align = Align.END

                } else if (targetViewRect.centerY() - height / 2f < 0) {
                    align = Align.START

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
                    align = Align.CENTER
                    changed = true

                } else if (targetViewRect.centerX() - tooltipWidth / 2f < 0) {
                    val diff = -(targetViewRect.centerX() - tooltipWidth / 2f)
                    adjustedLeft += diff.toInt()
                    adjustedRight += diff.toInt()
                    align = Align.CENTER
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

        startEnterAnimation()
        handleAutoRemove()
    }

    private fun getAlignOffset(tooltipLength: Int, targetLength: Int): Int =
        when (align) {
            Align.END -> targetLength - tooltipLength
            Align.CENTER -> (targetLength - tooltipLength) / 2
            Align.START -> 0
        }

    fun close() {
        remove()
    }

    fun remove() {
        startExitAnimation(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                removeNow()
            }
        })
    }

    fun removeNow() {
        if (parent != null) {
            val parent = parent as ViewGroup
            parent.removeView(this@Tooltip)
        }
    }

    fun closeNow() {
        removeNow()
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

            val arrowSourceX =
                if (position.isVertical()) centerX + arrowSourceMargin else centerX
            val arrowTargetX =
                if (position.isVertical()) centerX + arrowTargetMargin else centerX
            val arrowSourceY =
                if (position.isHorizontal()) centerY - arrowSourceMargin else centerY
            val arrowTargetY =
                if (position.isHorizontal()) centerY - arrowTargetMargin else centerY

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

            when (position) {
                Position.START -> {
                    x = targetViewRect.left - width - tooltipMargin.roundToInt()
                    y = targetViewRect.top + getAlignOffset(height, targetViewRect.height())
                }

                Position.TOP -> {
                    y = targetViewRect.top - height - tooltipMargin.roundToInt()
                    x = targetViewRect.left + getAlignOffset(width, targetViewRect.width())
                }

                Position.END -> {
                    x = targetViewRect.right + tooltipMargin.roundToInt()
                    y = targetViewRect.top + getAlignOffset(height, targetViewRect.height())
                }

                Position.BOTTOM -> {
                    y = targetViewRect.bottom + tooltipMargin.roundToInt()
                    x = targetViewRect.left + getAlignOffset(width, targetViewRect.width())
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

            val arrowSourceX =
                if (position.isVertical()) centerX + arrowSourceMargin else centerX
            val arrowTargetX =
                if (position.isVertical()) centerX + arrowTargetMargin else centerX
            val arrowSourceY =
                if (position.isHorizontal()) bottom / 2f - arrowSourceMargin else bottom / 2f
            val arrowTargetY =
                if (position.isHorizontal()) bottom / 2f - arrowTargetMargin else bottom / 2f

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

            when (position) {
                Position.START -> {
                    x = -(screenWidth - targetViewRect.right - width) + tooltipMargin.roundToInt()
                    y = targetViewRect.top + getAlignOffset(height, targetViewRect.height())
                }

                Position.TOP -> {
                    y = targetViewRect.top - height - tooltipMargin.roundToInt()
                    x = -(screenWidth - targetViewRect.right) - getAlignOffset(
                        width,
                        targetViewRect.width()
                    )
                }

                Position.END -> {
                    x = -(screenWidth - targetViewRect.left) - tooltipMargin.roundToInt()
                    y = targetViewRect.top + getAlignOffset(height, targetViewRect.height())
                }

                Position.BOTTOM -> {
                    y = targetViewRect.bottom + tooltipMargin.roundToInt()
                    x = -(screenWidth - targetViewRect.right) - getAlignOffset(
                        width,
                        targetViewRect.width()
                    )
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

        private const val COLOR_BUBBLE_DEFAULT = 0xFF197327.toInt()
        private const val COLOR_SHADOW_DEFAULT = 0xFF333338.toInt()//TODO default shadow color
        private const val COLOR_TRANSPARENT = 0x0
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
}