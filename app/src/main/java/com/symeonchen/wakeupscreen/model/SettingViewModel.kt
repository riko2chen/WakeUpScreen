package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import com.symeonchen.wakeupscreen.ScLiveData
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.DarkModeInfo
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.SleepSegment
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * Created by SymeonChen on 2019-10-27.
 */
class SettingViewModel : ViewModel() {

    var switchOfApp: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.switchOfApp)
        }

    var switchOfProximity: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.switchOfProximity)
        }

    var fakeSwitchOfBatterySaver: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.fakeSwitchOfBatterySaver)
        }

    var permissionOfSendNotification: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.permissionOfSendNotification)
        }

    @Deprecated("Remove debugMode and database")
    var switchOfDebugMode: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.switchOfDebugMode)
        }

    var modeOfCurrent: ScLiveData<CurrentMode> = ScLiveData<CurrentMode>()
        .apply {
            setValue(DataInjection.modeOfCurrent)
        }

    var ongoingOptimize: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.ongoingOptimize)
        }

    var radicalOngoingOptimize: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.radicalOngoingOptimize)
        }

    var ignoreSilentNotificationSwitch: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.ignoreSilentNotificationSwitch)
        }

    var languageSelected: ScLiveData<LanguageInfo> = ScLiveData<LanguageInfo>()
        .apply {
            setValue(DataInjection.languageSelected)
        }

    var darkModeSelected: ScLiveData<DarkModeInfo> = ScLiveData<DarkModeInfo>()
        .apply {
            setValue(DataInjection.darkModeSelected)
        }

    var sleepModeBoolean: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.sleepModeBoolean)
        }

    var sleepModeSegments: ScLiveData<List<SleepSegment>> = ScLiveData<List<SleepSegment>>()
        .apply {
            setValue(DataInjection.sleepModeSegments)
        }

    var dndDetectBoolean: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.dndDetectSwitch)
        }

    var chargingOnlySwitch: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.chargingOnlySwitch)
        }

    var repeatReminderSwitch: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.repeatReminderSwitch)
        }

    var repeatReminderIntervalMinutes: ScLiveData<Int> = ScLiveData<Int>()
        .apply {
            setValue(DataInjection.repeatReminderIntervalMinutes)
        }

    var repeatReminderMaxRounds: ScLiveData<Int> = ScLiveData<Int>()
        .apply {
            setValue(DataInjection.repeatReminderMaxRounds)
        }

    init {

        switchOfApp.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.switchOfApp = value
            }
        }

        switchOfProximity.listener = object :
            ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.switchOfProximity = value
            }
        }

        fakeSwitchOfBatterySaver.listener = object :
            ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.fakeSwitchOfBatterySaver = value
            }
        }

        modeOfCurrent.listener = object : ScLiveData.OnLiveDataValueInput<CurrentMode> {
            override fun onValueInput(value: CurrentMode) {
                DataInjection.modeOfCurrent = value
            }
        }

        ongoingOptimize.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.ongoingOptimize = value
            }
        }

        radicalOngoingOptimize.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.radicalOngoingOptimize = value
            }
        }

        ignoreSilentNotificationSwitch.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.ignoreSilentNotificationSwitch = value
            }
        }

        languageSelected.listener = object : ScLiveData.OnLiveDataValueInput<LanguageInfo> {
            override fun onValueInput(value: LanguageInfo) {
                DataInjection.languageSelected = value
            }
        }

        darkModeSelected.listener = object : ScLiveData.OnLiveDataValueInput<DarkModeInfo> {
            override fun onValueInput(value: DarkModeInfo) {
                DataInjection.darkModeSelected = value
            }
        }

        sleepModeBoolean.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.sleepModeBoolean = value
            }
        }


        sleepModeSegments.listener = object : ScLiveData.OnLiveDataValueInput<List<SleepSegment>> {
            override fun onValueInput(value: List<SleepSegment>) {
                DataInjection.sleepModeSegments = value
            }
        }

        dndDetectBoolean.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.dndDetectSwitch = value
            }
        }

        chargingOnlySwitch.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.chargingOnlySwitch = value
            }
        }

        repeatReminderSwitch.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.repeatReminderSwitch = value
            }
        }

        repeatReminderIntervalMinutes.listener = object : ScLiveData.OnLiveDataValueInput<Int> {
            override fun onValueInput(value: Int) {
                DataInjection.repeatReminderIntervalMinutes = value
            }
        }

        repeatReminderMaxRounds.listener = object : ScLiveData.OnLiveDataValueInput<Int> {
            override fun onValueInput(value: Int) {
                DataInjection.repeatReminderMaxRounds = value
            }
        }
    }

}