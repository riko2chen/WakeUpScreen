package com.symeonchen.wakeupscreen.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.uicomponent.views.SCSettingSwitchItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import com.symeonchen.wakeupscreen.utils.DataInjection
import kotlinx.android.synthetic.main.activity_custom_page.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
class AdvanceSettingPageActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_page)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    companion object {
        fun actionStart(context: Context) {
            context.startActivity(Intent(context, AdvanceSettingPageActivity::class.java))
        }
    }


    private fun setListener() {

        iv_back.setOnClickListener { finish() }

        item_setting_proximity_detect.listener = object : SCSettingSwitchItem.OnItemClickListener {
            override fun onItemCLick() {
            }

            override fun onSwitchClick() {
                val switchCurr = settingModel.switchOfProximity.value ?: false
                settingModel.switchOfProximity.postValue(!switchCurr)
            }
        }

        item_setting_ongoing_detect.listener = object : SCSettingSwitchItem.OnItemClickListener {
            override fun onItemCLick() {
            }

            override fun onSwitchClick() {
                val switchCurr = settingModel.ongoingOptimize.value ?: false
                settingModel.ongoingOptimize.postValue(!switchCurr)
            }
        }

        item_setting_sleep_ignore.listener = object : SCSettingSwitchItem.OnItemClickListener {
            override fun onItemCLick() {
            }

            override fun onSwitchClick() {
                val switchCurr = settingModel.sleepModeBoolean.value ?: false
                settingModel.sleepModeBoolean.postValue(!switchCurr)
            }
        }

        item_setting_sleep_ignore_detail_time.listener =
            object : SCSettingItem.OnItemClickListener {
                override fun onItemCLick() {
                    SleepTimeSettingActivity.actionStart(this@AdvanceSettingPageActivity)
                }
            }

        settingModel.switchOfProximity.observe(this, Observer {
            item_setting_proximity_detect.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close),
                it
            )
            if (it) {
                if (!ProximitySensorState.isRegistered()) {
                    ProximitySensorState.registerListener(this)
                }
            } else {
                if (ProximitySensorState.isRegistered())
                    ProximitySensorState.unRegisterListener(this)
            }
        })

        settingModel.ongoingOptimize.observe(this, Observer {
            item_setting_ongoing_detect.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close),
                it
            )
        })

        settingModel.sleepModeBoolean.observe(this, Observer {
            item_setting_sleep_ignore.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close),
                it
            )
            item_setting_sleep_ignore_detail_time.visibility = if (it) View.VISIBLE else View.GONE
        })

        settingModel.sleepModeTimeRange.observe(this, Observer {
            item_setting_sleep_ignore_detail_time.bindData(
                null,
                "${resources.getString(R.string.sleep_mode_open_desc)} ${it.first}:00➡️${it.second}:00"
            )
        })
    }

    override fun onResume() {
        super.onResume()
        settingModel.sleepModeTimeRange.postValue(
            Pair(
                DataInjection.sleepModeTimeBeginHour,
                DataInjection.sleepModeTimeEndHour
            )
        )
    }
}
