package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.ScLiveData
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

    var timeOfWakeUpScreen: ScLiveData<Long> = ScLiveData<Long>()
        .apply {
            setValue(DataInjection.milliSecondOfWakeUpScreen)
        }

    var fakeSwitchOfBatterySaver: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.fakeSwitchOfBatterySaver)
        }

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


    var languageSelected: ScLiveData<LanguageInfo> = ScLiveData<LanguageInfo>()
        .apply {
            setValue(DataInjection.languageSelected)
        }

    var sleepModeBoolean: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.sleepModeBoolean)
        }

    var sleepModeTimeRange: ScLiveData<Pair<Int, Int>> = ScLiveData<Pair<Int, Int>>().apply {
        setValue(Pair(DataInjection.sleepModeTimeBeginHour, DataInjection.sleepModeTimeEndHour))
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

        timeOfWakeUpScreen.listener = object : ScLiveData.OnLiveDataValueInput<Long> {
            override fun onValueInput(value: Long) {
                DataInjection.milliSecondOfWakeUpScreen = value
            }
        }

        fakeSwitchOfBatterySaver.listener = object :
            ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.fakeSwitchOfBatterySaver = value
            }
        }

        switchOfDebugMode.listener = object :
            ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.switchOfDebugMode = value
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
    }

}