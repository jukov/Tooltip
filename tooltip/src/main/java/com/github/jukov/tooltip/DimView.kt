package com.github.jukov.tooltip

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.annotation.ColorInt

class DimView internal constructor(context: Context): View(context) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            style = Paint.Style.FILL
        }

    private val targetViewRect: RectF = RectF()
    private val targetViewPath: Path = Path()

    var cornerRadius: Float = 0f
    var padding: Float = 0f

    init {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        if (targetViewRect.isEmpty) return
        if (paint.colorFilter == null) return

        canvas.drawPath(targetViewPath, paint)
    }

    fun setTargetViewRect(targetViewRect: Rect) {
        this.targetViewRect.set(targetViewRect)
        this.targetViewRect.inset(-padding, -padding)
        setupPath()
        invalidate()
    }

    private fun setupPath() {
        targetViewPath.reset()

        targetViewPath.moveTo(0f, 0f)
        targetViewPath.lineTo(width.toFloat(), 0f)
        targetViewPath.lineTo(width.toFloat(), height.toFloat())
        targetViewPath.lineTo(0f, height.toFloat())
        targetViewPath.lineTo(0f, 0f)
        targetViewPath.close()

        targetViewPath.addRoundRect(this.targetViewRect, cornerRadius, cornerRadius, Path.Direction.CCW)
        targetViewPath.fillType = Path.FillType.EVEN_ODD
    }

    fun setDimColor(@ColorInt color: Int) {
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.DST_OUT)
    }
}