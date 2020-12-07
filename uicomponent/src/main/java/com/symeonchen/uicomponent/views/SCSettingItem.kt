package com.symeonchen.uicomponent.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.symeonchen.uicomponent.R
import com.symeonchen.uicomponent.databinding.ViewItemSettingDoubleLineBinding

class SCSettingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var v: View? = null
    private var titleMain = ""
    private var titleSecond = ""
    private var singleLine = false
    private var _binding: ViewItemSettingDoubleLineBinding? = null
    private val binding get() = _binding!!

    var listener: OnItemClickListener? = null

    init {
        _binding = ViewItemSettingDoubleLineBinding.inflate(LayoutInflater.from(context), this)
        initBackground()
        initCustomValue(attrs)
        setListener()
        refresh()
    }


    private fun initBackground() {
        this.setBackgroundResource(R.drawable.list_selected)
        this.isClickable = true
        this.isFocusable = true
    }

    private fun initCustomValue(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SCSettingItem)

        titleMain = a.getString(R.styleable.SCSettingItem_sc_setting_item_title_main) ?: titleMain
        titleSecond =
            a.getString(R.styleable.SCSettingItem_sc_setting_item_title_second) ?: titleSecond
        singleLine = a.getBoolean(R.styleable.SCSettingItem_sc_setting_single_line, false)

        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setListener()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

    }

    private fun setListener() {
        this.setOnClickListener {
            listener?.onItemCLick()
        }
    }

    fun bindData(titleMain: String? = "", titleSecond: String? = "") {
        this.titleMain = titleMain ?: this.titleMain
        this.titleSecond = titleSecond ?: this.titleSecond
        refresh()
    }

    private fun refresh() {
        binding.tvTitleMain.text = titleMain
        binding.tvTitleSecond.text = titleSecond
        binding.tvTitleSecond.visibility = if (singleLine) View.GONE else View.VISIBLE
    }

    interface OnItemClickListener {
        fun onItemCLick() {}
    }
}



