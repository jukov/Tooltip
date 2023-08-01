package info.jukov.tooltip.sample

import android.os.Parcel
import android.os.Parcelable
import info.jukov.tooltip.Tooltip

data class TooltipConfig(
    val viewType: ViewType,
    val targetViewPosition: TargetViewPosition,
    val position: Tooltip.Position
): Parcelable {
    constructor(parcel: Parcel) : this(
        ViewType.values()[parcel.readInt()],
        TargetViewPosition.values()[parcel.readInt()],
        Tooltip.Position.values()[parcel.readInt()]
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(viewType.ordinal)
        parcel.writeInt(targetViewPosition.ordinal)
        parcel.writeInt(position.ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TooltipConfig> {
        override fun createFromParcel(parcel: Parcel): TooltipConfig {
            return TooltipConfig(parcel)
        }

        override fun newArray(size: Int): Array<TooltipConfig?> {
            return arrayOfNulls(size)
        }
    }
}

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