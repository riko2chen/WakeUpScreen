package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.uicomponent.views.SCSettingSwitchItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.databinding.ActivityCustomPageBinding
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.quickStartActivity

/**
 * Created by SymeonChen on 2019-10-27.
 */
class AdvanceSettingPageActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel
    private lateinit var binding: ActivityCustomPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    private fun setListener() {

        binding.ivBack.setOnClickListener { finish() }

        binding.itemSettingProximityDetect.listener =
            object : SCSettingSwitchItem.OnItemClickListener {
                override fun onItemCLick() {
                }

                override fun onSwitchClick() {
                    val switchCurr = settingModel.switchOfProximity.value ?: false
                    settingModel.switchOfProximity.postValue(!switchCurr)
                }
            }

        binding.itemSettingOngoingDetect.listener =
            object : SCSettingSwitchItem.OnItemClickListener {
                override fun onItemCLick() {
                }

                override fun onSwitchClick() {
                    val switchCurr = settingModel.ongoingOptimize.value ?: false
                    settingModel.ongoingOptimize.postValue(!switchCurr)
                }
            }

        binding.itemSettingRadicalOngoingDetect.listener =
            object : SCSettingSwitchItem.OnItemClickListener {
                override fun onSwitchClick() {
                    val switchCurr = settingModel.radicalOngoingOptimize.value ?: false
                    settingModel.radicalOngoingOptimize.postValue(!switchCurr)
                }
            }

        binding.itemSettingDndDetect.listener = object : SCSettingSwitchItem.OnItemClickListener {
            override fun onSwitchClick() {
                val switchCurr = settingModel.dndDetectBoolean.value ?: false
                settingModel.dndDetectBoolean.postValue(!switchCurr)
            }
        }

        binding.itemSettingSleepIgnore.listener = object : SCSettingSwitchItem.OnItemClickListener {
            override fun onItemCLick() {
            }

            override fun onSwitchClick() {
                val switchCurr = settingModel.sleepModeBoolean.value ?: false
                settingModel.sleepModeBoolean.postValue(!switchCurr)
            }
        }

        binding.itemSettingSleepIgnoreDetailTime.listener =
            object : SCSettingItem.OnItemClickListener {
                override fun onItemCLick() {
                    quickStartActivity<SleepTimeSettingActivity>()
                }
            }

        settingModel.switchOfProximity.observe(this, Observer {
            binding.itemSettingProximityDetect.bindData(
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
            binding.itemSettingOngoingDetect.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close),
                it
            )
        })

        settingModel.radicalOngoingOptimize.observe(this, Observer {
            binding.itemSettingRadicalOngoingDetect.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close),
                it
            )
        })

        settingModel.dndDetectBoolean.observe(this, Observer {
            binding.itemSettingDndDetect.bindData(
                null,
                null,
                it
            )
        })

        settingModel.sleepModeBoolean.observe(this, Observer {
            binding.itemSettingSleepIgnore.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close),
                it
            )
            binding.itemSettingSleepIgnoreDetailTime.visibility =
                if (it) View.VISIBLE else View.GONE
        })

        settingModel.sleepModeTimeRange.observe(this, Observer {
            binding.itemSettingSleepIgnoreDetailTime.bindData(
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
