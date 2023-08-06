package info.jukov.tooltip_sample

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

inline fun <reified T : Parcelable> Bundle.requireParcelable(name: String): T =
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        @Suppress("DEPRECATION")
        requireNotNull(getParcelable(name))
    } else {
        getParcelable(name, T::class.java) as T
    }
