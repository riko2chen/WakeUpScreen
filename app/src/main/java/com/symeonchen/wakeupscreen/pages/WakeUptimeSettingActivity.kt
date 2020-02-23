package com.symeonchen.wakeupscreen.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.model.WakeUpTimeViewModel
import kotlinx.android.synthetic.main.activity_wake_up_time.*


/**
 * Created by SymeonChen on 2020-02-20.
 */
class WakeUptimeSettingActivity : ScBaseActivity() {

    private var viewModel: WakeUpTimeViewModel? = null

    companion object {
        fun actionStart(context: Context) {
            context.startActivity(Intent(context, WakeUptimeSettingActivity::class.java))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        btn_time_second_1.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(1000L)
        }
        btn_time_second_2.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(2000L)
        }
        btn_time_second_3.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(3000L)
        }
        btn_time_second_4.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(4000L)
        }
        btn_time_second_5.setOnClickListener {
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(5000L)
        }

        iv_back.setOnClickListener {
            finish()
        }

        tv_save.setOnClickListener {
            tryToSaveWakeUpTime()
        }

    }

    private fun setDataObserver() {
        viewModel?.temporaryTimeOfWakeUpScreen?.observe(this, Observer {
            val second = it / 1000
            et_wake_time.setText("$second")
            refreshBtnView(second)
        })

    }

    private fun refreshBtnView(second: Long) {
        btn_time_second_1.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        btn_time_second_2.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        btn_time_second_3.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        btn_time_second_4.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        btn_time_second_5.setBackgroundColor(ContextCompat.getColor(this, R.color.lite_gray))
        when (second) {
            1L -> btn_time_second_1.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            2L -> btn_time_second_2.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            3L -> btn_time_second_3.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            4L -> btn_time_second_4.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            5L -> btn_time_second_5.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
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
                et_wake_time.text.toString().toLong()
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