package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.NotifyItem
import com.symeonchen.wakeupscreen.databinding.ActivityDebugPageBinding
import com.symeonchen.wakeupscreen.model.NotifyItemViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
class DebugPageActivity : ScBaseActivity() {

    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var viewModel: NotifyItemViewModel? = null
    private lateinit var binding: ActivityDebugPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        setListener()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[NotifyItemViewModel::class.java]
    }

    private fun setListener() {
        viewModel?.allNotifyItem?.observe(this, androidx.lifecycle.Observer {
            updateView(it)
        })
        binding.ivBack.setOnClickListener { finish() }
        binding.tvClearAll.setOnClickListener {
            viewModel?.clearAll()
        }
    }

    private fun updateView(it: List<NotifyItem>?) {
        if (it.isNullOrEmpty()) {
            binding.tvLog.text = ""
        } else {
            var str = ""
            for (item in it) {
                val itemStr = "\n ${sdf.format(item.time)} : ${item.appName}"
                str += itemStr
            }
            binding.tvLog.text = str
        }
    }


}
