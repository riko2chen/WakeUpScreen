package com.symeonchen.wakeupscreen.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var statusOfService: MutableLiveData<Boolean> = MutableLiveData()

    var permissionOfReadNotification: MutableLiveData<Boolean> = MutableLiveData()


}