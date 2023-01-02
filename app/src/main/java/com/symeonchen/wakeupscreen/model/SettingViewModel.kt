package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import com.symeonchen.wakeupscreen.ScLiveData
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
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

    var radicalOngoingNotificationSwitch: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.radicalOngoingNotificationSwitch)
        }

    var languageSelected: ScLiveData<LanguageInfo> = ScLiveData<LanguageInfo>()
        .apply {
            setValue(DataInjection.languageSelected)
        }

    var sleepModeBoolean: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.sleepModeBoolean)
        }

    var sleepModeTimeRange: ScLiveData<Pair<Int, Int>> = ScLiveData<Pair<Int, Int>>()
        .apply {
            setValue(Pair(DataInjection.sleepModeTimeBeginHour, DataInjection.sleepModeTimeEndHour))
        }

    var dndDetectBoolean: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.dndDetectSwitch)
        }

    var persistentSwitch: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.persistentNotification)
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

        radicalOngoingNotificationSwitch.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.radicalOngoingNotificationSwitch = value
            }
        }

        languageSelected.listener = object : ScLiveData.OnLiveDataValueInput<LanguageInfo> {
            override fun onValueInput(value: LanguageInfo) {
                DataInjection.languageSelected = value
            }
        }

        sleepModeBoolean.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.sleepModeBoolean = value
            }
        }


        sleepModeTimeRange.listener = object : ScLiveData.OnLiveDataValueInput<Pair<Int, Int>> {
            override fun onValueInput(value: Pair<Int, Int>) {
                DataInjection.sleepModeTimeBeginHour = value.first
                DataInjection.sleepModeTimeEndHour = value.second
            }
        }

        dndDetectBoolean.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.dndDetectSwitch = value
            }
        }

        persistentSwitch.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.persistentNotification = value
            }
        }
    }

}