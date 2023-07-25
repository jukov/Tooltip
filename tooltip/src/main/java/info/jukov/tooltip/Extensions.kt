package info.jukov.tooltip

import android.view.View
import androidx.core.view.OneShotPreDrawListener

/**
 * Performs the given action when the view tree is about to be drawn.
 *
 * The action will only be invoked once prior to the next draw and then removed.
 */
internal inline fun View.doOnPreDraw(crossinline action: (view: View) -> Unit): OneShotPreDrawListener {
    return OneShotPreDrawListener.add(this) { action(this) }
}