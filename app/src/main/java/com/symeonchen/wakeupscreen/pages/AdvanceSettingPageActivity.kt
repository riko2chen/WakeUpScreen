package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.AdvanceSettingScreen
import com.symeonchen.wakeupscreen.compose.components.SelectionDialog
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.data.SleepSegment
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.TimeOfDayFormatter
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class AdvanceSettingPageActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel

    /**
     * Mirrors the precise screen-on setting for the row subtitle. Refreshed on
     * resume rather than observed: the setting lives in another screen's view
     * model, and coming back from that screen is the only way it can change.
     */
    private val preciseWakeState = mutableStateOf(preciseWakeSnapshot())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        setContent {
            WakeUpScreenTheme {
                val currentMode by settingModel.modeOfCurrent.observeAsState(CurrentMode.MODE_ALL_NOTIFY)
                val proximity by settingModel.switchOfProximity.observeAsState(false)
                val ongoing by settingModel.ongoingOptimize.observeAsState(false)
                val radicalOngoing by settingModel.radicalOngoingOptimize.observeAsState(false)
                val ignoreSilent by settingModel.ignoreSilentNotificationSwitch.observeAsState(false)
                val dnd by settingModel.dndDetectBoolean.observeAsState(false)
                val chargingOnly by settingModel.chargingOnlySwitch.observeAsState(false)
                val batteryLevel by settingModel.batteryLevelSwitch.observeAsState(false)
                val batteryThreshold by settingModel.batteryLevelThreshold.observeAsState(
                    ScConstant.DEFAULT_BATTERY_LEVEL_THRESHOLD
                )
                val sleep by settingModel.sleepModeBoolean.observeAsState(false)
                val sleepSegments by settingModel.sleepModeSegments.observeAsState(emptyList())
                val repeatReminder by settingModel.repeatReminderSwitch.observeAsState(false)
                val reminderInterval by settingModel.repeatReminderIntervalMinutes.observeAsState(
                    ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES
                )

                fun statusText(on: Boolean) =
                    getString(if (on) R.string.already_open else R.string.already_close)

                var showModeDialog by remember { mutableStateOf(false) }
                var showBatteryThresholdDialog by remember { mutableStateOf(false) }

                val currentModeText = stringResource(
                    when (currentMode) {
                        CurrentMode.MODE_BLACK_LIST -> R.string.black_list
                        CurrentMode.MODE_WHITE_LIST -> R.string.white_list
                        else -> R.string.all_pass
                    }
                )

                val (preciseEnabled, preciseSeconds) = preciseWakeState.value
                val wakeTimeText = if (preciseEnabled) {
                    stringResource(R.string.precise_wake_summary_on, preciseSeconds)
                } else {
                    stringResource(R.string.precise_wake_summary_off)
                }

                AdvanceSettingScreen(
                    onBack = { finish() },
                    wakeTimeText = wakeTimeText,
                    onWakeTimeClick = { quickStartActivity<WakeUptimeSettingActivity>() },
                    currentModeText = currentModeText,
                    onCurrentModeClick = { showModeDialog = true },
                    showWhiteListEntry = currentMode == CurrentMode.MODE_WHITE_LIST,
                    onWhiteListClick = { FilterListActivity.actionStartWithMode(this, CurrentMode.MODE_WHITE_LIST) },
                    showBlackListEntry = currentMode == CurrentMode.MODE_BLACK_LIST,
                    onBlackListClick = { FilterListActivity.actionStartWithMode(this, CurrentMode.MODE_BLACK_LIST) },
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
                    batteryLevelChecked = batteryLevel,
                    onBatteryLevelToggle = { settingModel.batteryLevelSwitch.postValue(!batteryLevel) },
                    showBatteryLevelDetail = batteryLevel,
                    batteryLevelDetailSubtitle = getString(
                        R.string.battery_level_threshold_value, batteryThreshold
                    ),
                    onBatteryLevelDetailClick = { showBatteryThresholdDialog = true },
                    sleepChecked = sleep,
                    sleepSubtitle = statusText(sleep),
                    onSleepToggle = { settingModel.sleepModeBoolean.postValue(!sleep) },
                    showSleepDetail = sleep,
                    sleepDetailSubtitle = sleepSummary(sleepSegments),
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

                // Battery threshold dialog
                if (showBatteryThresholdDialog) {
                    val options = ScConstant.BATTERY_LEVEL_THRESHOLD_OPTIONS
                    val currentIdx = options.indexOf(batteryThreshold).let { if (it >= 0) it else 0 }
                    SelectionDialog(
                        title = stringResource(R.string.battery_level_threshold_title),
                        options = options.map { getString(R.string.battery_level_threshold_value, it) },
                        selectedIndex = currentIdx,
                        confirmText = stringResource(R.string.ok),
                        onSelect = { idx ->
                            showBatteryThresholdDialog = false
                            settingModel.batteryLevelThreshold.postValue(options[idx])
                        },
                        onDismiss = { showBatteryThresholdDialog = false },
                    )
                }

                // Mode dialog
                if (showModeDialog) {
                    val modeLabels = listOf(
                        stringResource(R.string.all_pass),
                        stringResource(R.string.white_list),
                        stringResource(R.string.black_list),
                    )
                    val currentIdx = when (currentMode) {
                        CurrentMode.MODE_ALL_NOTIFY -> 0
                        CurrentMode.MODE_WHITE_LIST -> 1
                        else -> 2
                    }
                    SelectionDialog(
                        title = stringResource(R.string.current_mode),
                        options = modeLabels,
                        selectedIndex = currentIdx,
                        confirmText = stringResource(R.string.ok),
                        onSelect = { idx ->
                            showModeDialog = false
                            settingModel.modeOfCurrent.postValue(
                                CurrentMode.getModeFromValue(idx)
                            )
                        },
                        onDismiss = { showModeDialog = false },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preciseWakeState.value = preciseWakeSnapshot()
        settingModel.sleepModeSegments.postValue(DataInjection.sleepModeSegments)
        settingModel.repeatReminderIntervalMinutes.postValue(
            DataInjection.repeatReminderIntervalMinutes
        )
    }

    private fun preciseWakeSnapshot(): Pair<Boolean, Long> =
        DataInjection.preciseScreenOnSwitch to DataInjection.preciseScreenOnSecond

    /** One line for the row: the window itself when there is only one, a count otherwise. */
    private fun sleepSummary(segments: List<SleepSegment>): String = when (segments.size) {
        0 -> getString(R.string.sleep_segments_empty)
        1 -> TimeOfDayFormatter.formatSegment(segments[0])
        else -> getString(R.string.sleep_segments_count, segments.size)
    }

    private fun reminderIntervalText(minutes: Int): String =
        if (minutes >= 60 && minutes % 60 == 0) {
            getString(R.string.reminder_interval_hours_value, minutes / 60)
        } else {
            getString(R.string.reminder_interval_minutes_value, minutes)
        }
}
