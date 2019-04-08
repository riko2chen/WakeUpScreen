package com.symeonchen.wakeupscreen.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.symeonchen.wakeupscreen.utils.DataInjection

class SettingViewModel : ViewModel() {

    var switchOfApp: MutableLiveData<Boolean> = MutableLiveData()

    var switchOfProximity: MutableLiveData<Boolean> = MutableLiveData()

    var timeOfWakeUpScreen: MutableLiveData<Long> = MutableLiveData()

    init {
        switchOfApp.postValue(DataInjection.getSwitchOfApp())
        switchOfProximity.postValue(DataInjection.getSwitchOfProximity())
        timeOfWakeUpScreen.postValue(DataInjection.getMilliSecondOfWakeUpScreen())
    }


}