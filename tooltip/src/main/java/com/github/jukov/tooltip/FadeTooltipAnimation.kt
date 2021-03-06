package com.github.jukov.tooltip

import android.animation.Animator
import android.view.View

class FadeTooltipAnimation : TooltipBuilder.TooltipAnimation {

    private var fadeDuration: Long = 400

    constructor()

    constructor(fadeDuration: Long) {
        this.fadeDuration = fadeDuration
    }

    override fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?) {
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(fadeDuration).setListener(animatorListener)
    }

    override fun animateExit(view: View, animatorListener: Animator.AnimatorListener?) {
        view.animate().alpha(0f).setDuration(fadeDuration).setListener(animatorListener)
    }
}