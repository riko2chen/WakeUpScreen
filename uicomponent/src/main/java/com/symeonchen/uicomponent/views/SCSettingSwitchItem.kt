package com.symeonchen.uicomponent.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.symeonchen.uicomponent.R
import com.symeonchen.uicomponent.databinding.ViewItemSettingSwitchBinding

class SCSettingSwitchItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var titleMain = ""
    private var titleSecond = ""
    private var checked = false

    var listener: OnItemClickListener? = null
    private val binding: ViewItemSettingSwitchBinding by lazy {
        ViewItemSettingSwitchBinding.inflate(LayoutInflater.from(context), this)
    }

    init {
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
        val a = context.obtainStyledAttributes(attrs, R.styleable.SCSettingSwitchItem)

        titleMain = a.getString(R.styleable.SCSettingSwitchItem_sc_setting_switch_item_title_main) ?: titleMain
        titleSecond = a.getString(R.styleable.SCSettingSwitchItem_sc_setting_switch_item_title_second) ?: titleSecond
        checked = a.getBoolean(R.styleable.SCSettingSwitchItem_sc_setting_switch_item_checked, checked)

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
        binding.switchStatus.setOnClickListener {
            listener?.onSwitchClick()
        }
    }

    fun bindData(titleMain: String? = "", titleSecond: String? = "", checked: Boolean?) {
        this.titleMain = titleMain ?: this.titleMain
        this.titleSecond = titleSecond ?: this.titleSecond
        this.checked = checked ?: this.checked
        refresh()
    }

    private fun refresh() {
        binding.tvTitleMain.text = titleMain
        binding.tvTitleSecond.text = titleSecond
        binding.switchStatus.isChecked = checked
    }

    interface OnItemClickListener {
        fun onItemCLick() {}
        fun onSwitchClick() {}
    }
}
