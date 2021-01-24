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
    private lateinit var binding: ActivityWakeUpTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWakeUpTimeBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_wake_up_time)
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
        binding.btnTimeSecond1.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        binding.btnTimeSecond2.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        binding.btnTimeSecond3.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        binding.btnTimeSecond4.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        binding.btnTimeSecond5.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        when (second) {
            1L -> binding.btnTimeSecond1.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )
            2L -> binding.btnTimeSecond2.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )
            3L -> binding.btnTimeSecond3.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )
            4L -> binding.btnTimeSecond4.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )
            5L -> binding.btnTimeSecond5.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.green
                )
            )
            else -> {
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