package com.github.jukov.tooltip

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

fun dpToPx(dp: Float, context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

fun spToPx(sp: Float, context: Context): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)

fun pxToDp(px: Float, context: Context): Float =
    px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)