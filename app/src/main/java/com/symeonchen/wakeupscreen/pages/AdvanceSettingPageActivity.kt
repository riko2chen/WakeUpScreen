package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.AdvanceSettingScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class AdvanceSettingPageActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        setContent {
            WakeUpScreenTheme {
                val proximity by settingModel.switchOfProximity.observeAsState(false)
                val ongoing by settingModel.ongoingOptimize.observeAsState(false)
                val radicalOngoing by settingModel.radicalOngoingOptimize.observeAsState(false)
                val persistent by settingModel.persistentSwitch.observeAsState(false)
                val dnd by settingModel.dndDetectBoolean.observeAsState(false)
                val sleep by settingModel.sleepModeBoolean.observeAsState(false)
                val sleepRange by settingModel.sleepModeTimeRange.observeAsState(Pair(2, 4))

                fun statusText(on: Boolean) =
                    getString(if (on) R.string.already_open else R.string.already_close)

                AdvanceSettingScreen(
                    onBack = { finish() },
                    proximityChecked = proximity,
                    proximitySubtitle = statusText(proximity),
                    onProximityToggle = {
                        settingModel.switchOfProximity.postValue(!proximity)
                        if (!proximity) {
                            if (!ProximitySensorState.isRegistered()) ProximitySensorState.registerListener(this)
                        } else {
                            if (ProximitySensorState.isRegistered()) ProximitySensorState.unRegisterListener(this)
                        }
                    },
                    ongoingChecked = ongoing,
                    ongoingSubtitle = statusText(ongoing),
                    onOngoingToggle = { settingModel.ongoingOptimize.postValue(!ongoing) },
                    radicalOngoingChecked = radicalOngoing,
                    radicalOngoingSubtitle = statusText(radicalOngoing),
                    onRadicalOngoingToggle = { settingModel.radicalOngoingOptimize.postValue(!radicalOngoing) },
                    persistentChecked = persistent,
                    persistentSubtitle = statusText(persistent),
                    onPersistentToggle = { settingModel.persistentSwitch.postValue(!persistent) },
                    dndChecked = dnd,
                    onDndToggle = { settingModel.dndDetectBoolean.postValue(!dnd) },
                    sleepChecked = sleep,
                    sleepSubtitle = statusText(sleep),
                    onSleepToggle = { settingModel.sleepModeBoolean.postValue(!sleep) },
                    showSleepDetail = sleep,
                    sleepDetailSubtitle = "${getString(R.string.sleep_mode_open_desc)} ${sleepRange.first}:00→${sleepRange.second}:00",
                    onSleepDetailClick = { quickStartActivity<SleepTimeSettingActivity>() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingModel.sleepModeTimeRange.postValue(
            Pair(DataInjection.sleepModeTimeBeginHour, DataInjection.sleepModeTimeEndHour)
        )
    }
}
