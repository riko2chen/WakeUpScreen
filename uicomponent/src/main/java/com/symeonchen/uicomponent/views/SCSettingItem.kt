package com.symeonchen.uicomponent.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.symeonchen.uicomponent.R
import kotlinx.android.synthetic.main.view_item_setting_double_line.view.*

class SCSettingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var v: View? = null
    private var titleMain = ""
    private var titleSecond = ""

    var listener: OnItemClickListener? = null

    init {
        v = LayoutInflater.from(context).inflate(R.layout.view_item_setting_double_line, this, true)

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
        titleSecond = a.getString(R.styleable.SCSettingItem_sc_setting_item_title_second) ?: titleSecond

        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setListener()
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
        tv_title_main.text = titleMain
        tv_title_second.text = titleSecond
    }

    interface OnItemClickListener {
        fun onItemCLick() {}
    }
}



