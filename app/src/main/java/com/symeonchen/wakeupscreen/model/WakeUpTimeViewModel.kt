package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import com.symeonchen.wakeupscreen.ScLiveData
import com.symeonchen.wakeupscreen.services.screen.PreciseScreenOnManager
import com.symeonchen.wakeupscreen.utils.DataInjection


/**
 * Created by SymeonChen on 2020-02-22.
 */
class WakeUpTimeViewModel : ViewModel() {

    var timeOfWakeUpScreen: ScLiveData<Long> = ScLiveData<Long>()
        .apply {
            setValue(DataInjection.milliSecondOfWakeUpScreen)
        }

    var temporaryTimeOfWakeUpScreen: ScLiveData<Long> = ScLiveData<Long>()
        .apply {
            setValue(DataInjection.milliSecondOfWakeUpScreen)
        }

    var preciseScreenOnSwitch: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
            setValue(DataInjection.preciseScreenOnSwitch)
        }

    init {
        timeOfWakeUpScreen.listener = object : ScLiveData.OnLiveDataValueInput<Long> {
            override fun onValueInput(value: Long) {
                DataInjection.milliSecondOfWakeUpScreen = value
            }
        }

        preciseScreenOnSwitch.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.preciseScreenOnSwitch = value
                if (!value) {
                    // A window already counting down would otherwise run to its
                    // end and blank the screen after the feature was turned off.
                    PreciseScreenOnManager.onSwitchDisabled()
                }
            }
        }
    }
}