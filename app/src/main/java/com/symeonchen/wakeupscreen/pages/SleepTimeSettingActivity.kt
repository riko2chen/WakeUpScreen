package com.symeonchen.wakeupscreen.pages

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.databinding.ActivitySleepTimeSettingBinding
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection


/**
 * Created by SymeonChen on 2019-10-27.
 */
class SleepTimeSettingActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel
    private lateinit var binding: ActivitySleepTimeSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepTimeSettingBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_sleep_time_setting)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
        initData()
    }

    @SuppressLint("SetTextI18n")
    private fun setListener() {

        binding.ivBack.setOnClickListener { finish() }

        settingModel.sleepModeTimeRange.observe(this, Observer {
            binding.tvBeginTimeHour.text = "${it.first}:00"
            binding.tvSleepTimeDescBeginHour.text = "${it.first}:00"

            binding.tvEndTimeHour.text = "${it.second}:00"
            binding.tvSleepTimeDescEndHour.text = "${it.second}:00"

        })

        binding.sbBeginTimeHour.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    settingModel.sleepModeTimeRange.postValue(
                        Pair(
                            progress,
                            settingModel.sleepModeTimeRange.value?.second
                                ?: ScConstant.DEFAULT_SLEEP_MODE_TIME_END_HOUR
                        )
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.sbEndTimeHour.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    settingModel.sleepModeTimeRange.postValue(
                        Pair(
                            settingModel.sleepModeTimeRange.value?.first
                                ?: ScConstant.DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR,
                            progress
                        )
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }


    private fun initData() {
        binding.sbBeginTimeHour.progress = settingModel.sleepModeTimeRange.value?.first
            ?: ScConstant.DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR
        binding.sbEndTimeHour.progress = settingModel.sleepModeTimeRange.value?.second
            ?: ScConstant.DEFAULT_SLEEP_MODE_TIME_END_HOUR
    }
}