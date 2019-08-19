package com.symeonchen.uicomponent.views

import android.content.Context
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.symeonchen.uicomponent.R

class SCLoadingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var pbLoading: ProgressBar? = null

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.view_progress_loading, this, true)
        pbLoading = v.findViewById(R.id.pb_loading)
    }

    fun startLoadingAnimation() {
        pbLoading?.indeterminateDrawable?.let {
            (it as RotateDrawable).toDegrees = 1440f
        }
    }

    fun stopoLoadingAnimation() {
        pbLoading?.indeterminateDrawable?.let {
            (it as RotateDrawable).toDegrees = 0f
        }
    }


}



