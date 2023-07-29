package info.jukov.tooltip

import android.animation.Animator
import android.view.View

interface TooltipAnimation {
    fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?)
    fun animateExit(view: View, animatorListener: Animator.AnimatorListener?)
}