package info.jukov.tooltip_sample

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import info.jukov.tooltip.Tooltip
import kotlinx.parcelize.Parcelize

@Parcelize
data class TooltipConfig(
    val viewType: ViewType,
    val targetViewPosition: TargetViewPosition,
    val exactPosition: PointF?,
    val position: Tooltip.Position
): Parcelable

enum class ViewType {
    SMALL_TEXT,
    MEDIUM_TEXT,
    LARGE_TEXT,
    ICON
}

enum class TargetViewPosition {
    CENTER,
    TOP_START,
    TOP_END,
    BOTTOM_START,
    BOTTOM_END,
    VERTICAL_SCROLL,
    VERTICAL_NESTED_SCROLL,
    HORIZONTAL_SCROLL
}