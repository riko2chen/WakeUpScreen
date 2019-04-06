package com.symeonchen.wakeupscreen.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatusViewModel : ViewModel() {

    var customStatus: MutableLiveData<Boolean> = MutableLiveData()

    var statusOfService: MutableLiveData<Boolean> = MutableLiveData()

    var permissionOfReadNotification: MutableLiveData<Boolean> = MutableLiveData()


}