package com.github.hadywalied.ahramlockcontrolapp

import android.view.View
import android.view.ViewAnimationUtils
import kotlin.math.hypot

fun View.circularRevealAnimation() {
    val cx = this.width / 2
    val cy = this.height / 2
    val finalRadius = hypot(cx.toDouble(), cy.toDouble())

    val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius.toFloat())

    this.visibility = View.VISIBLE
    anim.start()
}
