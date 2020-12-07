package com.symeonchen.uicomponent.views

import android.content.Context
import android.graphics.drawable.RotateDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.symeonchen.uicomponent.databinding.ViewProgressLoadingBinding

class SCLoadingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var _binding: ViewProgressLoadingBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = ViewProgressLoadingBinding.inflate(LayoutInflater.from(context), this)
    }

    fun showLoading(container: ViewGroup? = null) {
        if (this.parent != null && container == this.parent) {
            return
        }
        if (this.parent != null) {
            (this.parent as? ViewGroup)?.removeView(this)
        }
        container ?: return
        container.addView(this)
        this.startLoadingAnimation()
    }

    fun hideLoading() {
        this.stopLoadingAnimation()
        if (this.parent != null) {
            (this.parent as? ViewGroup)?.removeView(this)
        }
    }

    private fun startLoadingAnimation() {
        binding.pbLoading.indeterminateDrawable?.let {
            (it as RotateDrawable).toDegrees = 1440f
        }
    }

    private fun stopLoadingAnimation() {
        binding.pbLoading.indeterminateDrawable?.let {
            (it as RotateDrawable).toDegrees = 0f
        }
    }


}



