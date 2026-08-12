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
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine
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
                val ignoreSilent by settingModel.ignoreSilentNotificationSwitch.observeAsState(false)
                val dnd by settingModel.dndDetectBoolean.observeAsState(false)
                val chargingOnly by settingModel.chargingOnlySwitch.observeAsState(false)
                val sleep by settingModel.sleepModeBoolean.observeAsState(false)
                val sleepRange by settingModel.sleepModeTimeRange.observeAsState(Pair(2, 4))
                val repeatReminder by settingModel.repeatReminderSwitch.observeAsState(false)
                val reminderInterval by settingModel.repeatReminderIntervalMinutes.observeAsState(
                    ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES
                )

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
                    ignoreSilentChecked = ignoreSilent,
                    onIgnoreSilentToggle = {
                        settingModel.ignoreSilentNotificationSwitch.postValue(!ignoreSilent)
                    },
                    dndChecked = dnd,
                    onDndToggle = { settingModel.dndDetectBoolean.postValue(!dnd) },
                    chargingOnlyChecked = chargingOnly,
                    chargingOnlySubtitle = statusText(chargingOnly),
                    onChargingOnlyToggle = { settingModel.chargingOnlySwitch.postValue(!chargingOnly) },
                    sleepChecked = sleep,
                    sleepSubtitle = statusText(sleep),
                    onSleepToggle = { settingModel.sleepModeBoolean.postValue(!sleep) },
                    showSleepDetail = sleep,
                    sleepDetailSubtitle = "${getString(R.string.sleep_mode_open_desc)} ${sleepRange.first}:00→${sleepRange.second}:00",
                    onSleepDetailClick = { quickStartActivity<SleepTimeSettingActivity>() },
                    repeatReminderChecked = repeatReminder,
                    repeatReminderSubtitle = getString(R.string.repeat_reminder_desc),
                    onRepeatReminderToggle = {
                        val enabled = !repeatReminder
                        settingModel.repeatReminderSwitch.postValue(enabled)
                        ReminderEngine.onSwitchChanged(applicationContext, enabled)
                    },
                    showRepeatReminderDetail = repeatReminder,
                    repeatReminderDetailSubtitle = reminderIntervalText(reminderInterval),
                    onRepeatReminderDetailClick = { quickStartActivity<ReminderSettingActivity>() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingModel.sleepModeTimeRange.postValue(
            Pair(DataInjection.sleepModeTimeBeginHour, DataInjection.sleepModeTimeEndHour)
        )
        settingModel.repeatReminderIntervalMinutes.postValue(
            DataInjection.repeatReminderIntervalMinutes
        )
    }

    private fun reminderIntervalText(minutes: Int): String =
        if (minutes >= 60 && minutes % 60 == 0) {
            getString(R.string.reminder_interval_hours_value, minutes / 60)
        } else {
            getString(R.string.reminder_interval_minutes_value, minutes)
        }
}
