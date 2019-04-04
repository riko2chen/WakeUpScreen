package com.symeonchen.wakeupscreen.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingViewModel : ViewModel() {

    var switchOfApp: MutableLiveData<Boolean> = MutableLiveData()

    var switchOfProximity: MutableLiveData<Boolean> = MutableLiveData()

    var timeOfWakeUpScreen: MutableLiveData<Long> = MutableLiveData()

    init {
        switchOfApp.postValue(false)
        switchOfProximity.postValue(false)
        timeOfWakeUpScreen.postValue(2000)
    }


}