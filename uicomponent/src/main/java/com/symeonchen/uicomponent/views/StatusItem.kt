package com.symeonchen.uicomponent.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.symeonchen.uicomponent.R
import com.symeonchen.uicomponent.databinding.ViewItemListMainBinding

class StatusItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var name: String = ""
    private var status: Boolean = false
    private var btnStr: String = ""

    var listener: OnItemClickListener? = null

    private val binding: ViewItemListMainBinding by lazy {
        ViewItemListMainBinding.inflate(LayoutInflater.from(context), this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initBackground()
        setListener()
    }

    private fun initBackground() {
        this.setBackgroundResource(R.drawable.list_selected)
        this.isClickable = true
        this.isFocusable = true
    }

    private fun setListener() {
        this.isClickable = true
        this.isFocusable = true
        this.setOnClickListener {
            listener?.onItemClick()
        }
        binding.mainMbItemNav.setOnClickListener {
            listener?.onBtnClick()
        }
    }

    private fun refresh() {
        binding.mainTvItemName.text = name
        binding.mainIvItemStatus.setImageResource(if (status) R.drawable.ic_check_green_24dp else R.drawable.ic_close_red_24dp)
        binding.mainMbItemNav.text = btnStr
        binding.mainMbItemNav.visibility = if (status) View.INVISIBLE else View.VISIBLE
    }

    fun bindData(name: String?, status: Boolean = false, btnStr: String?) {
        this.name = name ?: ""
        this.status = status
        this.btnStr = btnStr ?: ""
        refresh()
    }

    fun setState(status: Boolean) {
        this.status = status
        refresh()
    }

    fun setBtnText(str: String?) {
        this.btnStr = str ?: ""
        refresh()
    }

    interface OnItemClickListener {
        fun onItemClick()
        fun onBtnClick()
    }
}



