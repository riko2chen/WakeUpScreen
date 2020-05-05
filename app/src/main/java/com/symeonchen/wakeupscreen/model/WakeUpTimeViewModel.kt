package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import com.symeonchen.wakeupscreen.ScLiveData
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

    init {
        timeOfWakeUpScreen.listener = object : ScLiveData.OnLiveDataValueInput<Long> {
            override fun onValueInput(value: Long) {
                DataInjection.milliSecondOfWakeUpScreen = value
            }
        }
    }
}