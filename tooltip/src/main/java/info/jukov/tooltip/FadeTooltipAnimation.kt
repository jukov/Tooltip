package info.jukov.tooltip

import android.animation.Animator
import android.view.View
import android.view.animation.Interpolator

@Suppress("unused")
class FadeTooltipAnimation(
    private var fadeDuration: Long = 400,
    private var interpolator: Interpolator? = null
) : TooltipAnimation {

    override fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(fadeDuration)
            .setListener(animatorListener)
            .apply {
                this@FadeTooltipAnimation.interpolator?.let(::setInterpolator)
            }
    }

    override fun animateExit(view: View, animatorListener: Animator.AnimatorListener?) {
        view.animate()
            .alpha(0f)
            .setDuration(fadeDuration)
            .setListener(animatorListener)
            .apply {
                this@FadeTooltipAnimation.interpolator?.let(::setInterpolator)
            }
    }
}