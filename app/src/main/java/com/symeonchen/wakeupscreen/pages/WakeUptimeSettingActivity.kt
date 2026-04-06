package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.databinding.ActivityWakeUpTimeBinding
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.model.WakeUpTimeViewModel


/**
 * Created by SymeonChen on 2020-02-20.
 */
class WakeUptimeSettingActivity : ScBaseActivity() {

    private var viewModel: WakeUpTimeViewModel? = null

    private val binding by lazy { ActivityWakeUpTimeBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        initViewModel()
        setListener()
    }

    private fun initViewModel() {
        val wakeUpTimeViewModelFactory = ViewModelInjection.provideWakeUpTimeViewModelFactory()
        viewModel =
            ViewModelProvider(this, wakeUpTimeViewModelFactory).get(WakeUpTimeViewModel::class.java)
    }

    private fun setListener() {
        setViewListener()
        setDataObserver()
    }

    private fun setViewListener() {
        binding.btnTimeSecond1.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(1000L)
        }
        binding.btnTimeSecond2.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(2000L)
        }
        binding.btnTimeSecond3.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(3000L)
        }
        binding.btnTimeSecond4.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(4000L)
        }
        binding.btnTimeSecond5.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(5000L)
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvSave.setOnClickListener {
            tryToSaveWakeUpTime()
        }

    }

    private fun setDataObserver() {
        viewModel?.temporaryTimeOfWakeUpScreen?.observe(this, Observer {
            val second = it / 1000
            binding.etWakeTime.setText("$second")
            refreshBtnView(second)
        })

    }

    private fun refreshBtnView(second: Long) {
        val buttons = listOf(
            binding.btnTimeSecond1, binding.btnTimeSecond2, binding.btnTimeSecond3,
            binding.btnTimeSecond4, binding.btnTimeSecond5
        )
        buttons.forEach { it.setBackgroundResource(R.drawable.bg_time_button) }
        buttons.forEachIndexed { index, button ->
            if (second == (index + 1).toLong()) {
                button.setBackgroundResource(R.drawable.bg_time_button_selected)
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                button.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }
        }
    }


    private fun tryToSaveWakeUpTime() {
        val temporaryTimeOfWakeUpTime = viewModel?.temporaryTimeOfWakeUpScreen?.value
        val currentTimeOfWakeUpTime = viewModel?.timeOfWakeUpScreen?.value
        temporaryTimeOfWakeUpTime ?: return
        currentTimeOfWakeUpTime ?: return

        val etNum =
            try {
                binding.etWakeTime.text.toString().toLong()
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
                -1L
            }
        if (etNum <= 0) {
            ToastUtils.showLong(resources.getString(R.string.invalid_number))
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(3000L)
            return
        }

        viewModel?.timeOfWakeUpScreen?.postValue(etNum * 1000)
        ToastUtils.showLong(resources.getString(R.string.saved_successfully))
        this.finish()
    }

}