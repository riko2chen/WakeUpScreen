package com.symeonchen.wakeupscreen.data

import androidx.lifecycle.ViewModel
import com.symeonchen.wakeupscreen.utils.DataInjection

class SettingViewModel : ViewModel() {

    var switchOfApp: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
        setValue(DataInjection.getSwitchOfApp())
    }

    var switchOfProximity: ScLiveData<Boolean> = ScLiveData<Boolean>()
        .apply {
        setValue(DataInjection.getSwitchOfProximity())
    }

    var timeOfWakeUpScreen: ScLiveData<Long> = ScLiveData<Long>()
        .apply {
        setValue(DataInjection.getMilliSecondOfWakeUpScreen())
    }

    init {

        switchOfApp.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.setSwitchOfApp(value)
            }
        }

        switchOfProximity.listener = object : ScLiveData.OnLiveDataValueInput<Boolean> {
            override fun onValueInput(value: Boolean) {
                DataInjection.setSwitchOfProximity(value)
            }
        }

        timeOfWakeUpScreen.listener = object : ScLiveData.OnLiveDataValueInput<Long> {
            override fun onValueInput(value: Long) {
                DataInjection.setSecondOfWakeUpScreen(value)
            }
        }

    }

}