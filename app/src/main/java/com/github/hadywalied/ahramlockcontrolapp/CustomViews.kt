package com.github.hadywalied.ahramlockcontrolapp

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton


enum class State {
    PROGRESS, IDLE
}

class LoadingButton : MaterialButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(
        context,
        attr,
        defStyleAttr
    )

    private fun init(context: Context) {
        val gragientDrawable =
            ContextCompat.getDrawable(context, R.drawable.shape_default) as GradientDrawable?
        if (gragientDrawable != null) {
            background = gragientDrawable
        }
    }
}
