package info.jukov.tooltip

import android.content.Context
import android.util.TypedValue

internal fun dpToPx(dp: Float, context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)