package com.symeonchen.wakeupscreen.pages

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.ReminderSettingScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.services.ScLockScreenAccessibilityService
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine
import com.symeonchen.wakeupscreen.utils.ReminderDurationPolicy
import com.symeonchen.wakeupscreen.utils.ScLog

class ReminderSettingActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel
    private val accessibilityGranted = mutableStateOf(false)

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

                val duration by settingModel.repeatReminderScreenOnSeconds.observeAsState(ReminderDurationPolicy.INHERIT)

                ReminderSettingScreen(
                    onBack = { finish() },
                    onAppsClick = { startActivity(android.content.Intent(this, ReminderAppsActivity::class.java)) },
                    intervalMinutes = interval,
                    intervalOptions = ScConstant.REPEAT_REMINDER_INTERVAL_OPTIONS,
                    onIntervalChange = { minutes ->
                        if (minutes != interval) {
                            settingModel.repeatReminderIntervalMinutes.setValue(minutes)
                            // Re-arm so the change takes effect now rather than
                            // after the reminder already in flight has fired.
                            ReminderEngine.onIntervalChanged(applicationContext, minutes)
                        }
                    },
                    reminderScreenOnSeconds = duration,
                    onReminderDurationChange = { settingModel.repeatReminderScreenOnSeconds.value = it },
                    accessibilitySupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
                    accessibilityGranted = accessibilityGranted.value,
                    onGrantAccessibilityClick = { openAccessibilitySettings() },
                    maxRounds = maxRounds,
                    maxRoundsOptions = ScConstant.REPEAT_REMINDER_MAX_ROUNDS_OPTIONS,
                    unlimitedRoundsValue = ScConstant.REPEAT_REMINDER_ROUNDS_UNLIMITED,
                    onMaxRoundsChange = { rounds ->
                        settingModel.repeatReminderMaxRounds.postValue(rounds)
                        ReminderEngine.onMaxRoundsChanged(applicationContext, rounds)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accessibilityGranted.value = ScLockScreenAccessibilityService.isEnabledInSettings(applicationContext)
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Exception) {
            ScLog.w("ReminderSetting", "cannot open accessibility settings", e)
            ToastUtils.showLong(getString(R.string.accessibility_open_settings_failed))
        }
    }
}
