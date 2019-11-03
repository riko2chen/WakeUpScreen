package com.symeonchen.wakeupscreen.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import kotlinx.android.synthetic.main.activity_sleep_time_setting.*


/**
 * Created by SymeonChen on 2019-10-27.
 */
class SleepTimeSettingActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_time_setting)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
        initData()
    }

    companion object {
        fun actionStart(context: Context) {
            context.startActivity(Intent(context, SleepTimeSettingActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setListener() {

        iv_back.setOnClickListener { finish() }

        settingModel.sleepModeTimeRange.observe(this, Observer {
            tv_begin_time_hour.text = "${it.first}:00"
            tv_sleep_time_desc_begin_hour.text = "${it.first}:00"

            tv_end_time_hour.text = "${it.second}:00"
            tv_sleep_time_desc_end_hour.text = "${it.second}:00"

        })

        sb_begin_time_hour.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        sb_end_time_hour.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        sb_begin_time_hour.progress = settingModel.sleepModeTimeRange.value?.first
            ?: ScConstant.DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR
        sb_end_time_hour.progress = settingModel.sleepModeTimeRange.value?.second
            ?: ScConstant.DEFAULT_SLEEP_MODE_TIME_END_HOUR
    }
}