package com.symeonchen.wakeupscreen.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var customStatus: MutableLiveData<Boolean> = MutableLiveData()

    var statusOfService: MutableLiveData<Boolean> = MutableLiveData()

    var permissionOfReadNotification: MutableLiveData<Boolean> = MutableLiveData()


}