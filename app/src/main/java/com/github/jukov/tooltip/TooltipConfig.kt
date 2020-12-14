package com.github.jukov.tooltip

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TooltipConfig(
    val viewType: ViewType,
    val targetViewPosition: TargetViewPosition,
    val position: Tooltip.Position
): Parcelable

enum class ViewType {
    LARGE_TEXT,
    SMALL
}

enum class TargetViewPosition {
    CENTER,
    TOP_START,
    TOP_END,
    BOTTOM_START,
    BOTTOM_END
}