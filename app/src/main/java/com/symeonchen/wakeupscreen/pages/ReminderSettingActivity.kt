package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.ReminderSettingScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine

class ReminderSettingActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        setContent {
            WakeUpScreenTheme {
                val interval by settingModel.repeatReminderIntervalMinutes.observeAsState(
                    ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES
                )
                val maxRounds by settingModel.repeatReminderMaxRounds.observeAsState(
                    ScConstant.DEFAULT_REPEAT_REMINDER_MAX_ROUNDS
                )

                ReminderSettingScreen(
                    onBack = { finish() },
                    intervalMinutes = interval,
                    intervalOptions = ScConstant.REPEAT_REMINDER_INTERVAL_OPTIONS,
                    onIntervalChange = { minutes ->
                        if (minutes != interval) {
                            settingModel.repeatReminderIntervalMinutes.postValue(minutes)
                            // Re-arm so the change takes effect now rather than
                            // after the reminder already in flight has fired.
                            ReminderEngine.onIntervalChanged(applicationContext)
                        }
                    },
                    maxRounds = maxRounds,
                    maxRoundsOptions = ScConstant.REPEAT_REMINDER_MAX_ROUNDS_OPTIONS,
                    unlimitedRoundsValue = ScConstant.REPEAT_REMINDER_ROUNDS_UNLIMITED,
                    onMaxRoundsChange = { rounds ->
                        settingModel.repeatReminderMaxRounds.postValue(rounds)
                    },
                )
            }
        }
    }
}
